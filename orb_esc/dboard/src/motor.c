// ---------------------------------------------------------------------
// 
//	File: motor.c
//      SWARM Orb 
//      Motor controller functions for SWARM Orb http://www.orbswarm.com
//
//	Refactored by Jonathan (Head Rotor at rotorbrain.com)
//      Original Version by Petey the Programmer  Date: 30-April-2007
// -----------------------------------------------------------------------




// Output 2 channels of PWM on PortB Pins 1&2
// Ouput 2 sets of control lines (Dir/Disable) on Port D 4:5 & 6:7
// Main Drive motor is setup as PID controlled continuous rotation (main drive)
// Steering Motor is setup as a Servo with a position sensing feedback pot.

#include <avr/io.h>
#include <stdlib.h>
#include "eprom.h"
#include "UART.h"
#include "a2d.h"
#include "putstr.h"
#include "encoder.h"
#include "motor.h"

// Hardware connections to H-Bridge controllers
// These are chip dependant port pins
// For ATMega8 OC1A is PortB:1, OC1B is PortB:2
//
// Speed Control Direction/Disable Pins are Port D4:5 & D6:7
//
//		PortB:1 = PWM Pin for Motor1
//		PortD:4 = Direction Pin for Motor1
//		PortD:5 = Disable Pin for Motor1 - Set High to disable H-Bridge
//
//		PortB:2 = PWM Pin for Motor2
//		PortD:6 = Direction Pin for Motor2
//		PortD:7 = Disable Pin for Motor2 - Set High to disable H-Bridge

// ------- New H-Bridge Hardware - OSSC --
// Switches PWM from non-inverted to inverted output when reversing directions.

#define MOTOR1_FORWARD()  PORTD &= ~_BV(PD4); TCCR1A &= ~_BV(COM1A0); TCCR1A |= _BV(COM1A1)
#define MOTOR1_REVERSE()  PORTD |= _BV(PD4); TCCR1A |= (_BV(COM1A0) | _BV(COM1A1))
#define MOTOR1_DISABLE() PORTD |= _BV(PD5)
#define MOTOR1_ENABLE()  PORTD &= ~_BV(PD5)
#define MOTOR1_BRAKE()   TCCR1A &= ~_BV(COM1A1)

#define MOTOR2_FORWARD() PORTD &= ~_BV(PD6); TCCR1A &= ~_BV(COM1B0); TCCR1A |= _BV(COM1B1)
#define MOTOR2_REVERSE() PORTD |= _BV(PD6); TCCR1A |= (_BV(COM1B0) | _BV(COM1B1))
#define MOTOR2_DISABLE() PORTD |= _BV(PD7)
#define MOTOR2_ENABLE()  PORTD &= ~_BV(PD7)
#define MOTOR2_BRAKE()   TCCR1A &= ~_BV(COM1B1)

// ----------------------------------------------------------------------
// Motor Control Block for keeping track of Motor PID function variables

typedef struct {
  char	Kp;			/* proportional PID term */
  char	Ki;			/* integral PID term */
  char	Kd;			/* derivative PID term */
  short targetSpeed;		/* the speed we want to go */
  short currentSpeed;		/* the speed we are going now */
  char targetDirection;		/* the direction we want to go */
  char currentDirection;	/* the direction we are going now */
  unsigned char	currentPWM;	/* the pwer we are giving it now */
  short	dead_band;	/* close enough to desired speed if within */
  short lastSpeedError;		/* the error we had last iteration */
  unsigned char lastPWM;	/* the power we gave it last iteration */
  unsigned char deltaAccel;	/* biggest change in accel per inter */
  char intLimit;  		/* integrator limit */
  unsigned char minPWM;
  unsigned char maxPWM;
} motor_control_block;


static short iSum = 0;
static short iLimit = 400;

extern volatile uint8_t Drive_Debug_Output;	
extern volatile unsigned char doing_Speed_control;

/* Static Vars */
static motor_control_block drive;

/* Prototype */
void Motor_clear_mcb( motor_control_block *m );


// main feedback loop for speed control

void Drive_Servo_Task(void)
{
  short speedError;
  short drivePWM;
  int16_t p_term, d_term, i_term;
  
  
  // get current speed in RPM
  
  // Here we have a little problem. Encoder only returns positive values.
  // Take absolute value. 

  drive.currentSpeed = EncoderReadSpeed();
  
  // calculate error term
  speedError = abs(drive.targetSpeed) - drive.currentSpeed; 
  // if currentSpeed is less than target, speed up:  positive PWM
  // if currentSpeed is more than target, slow down: negative PWM
  // we may not be going the right direction, but at least we are 
  // going the right speed ;(

  if (abs(speedError) < drive.dead_band) {   
    // we are close enough to desired speed
    // don't change anything
    
    if (Drive_Debug_Output == 1) 
      putstr("DRIVE speed OK ");
    return;
  }
  
  //if here, error is significant; change PWM to decrease it
  
  // calculate proportional term
  p_term = speedError * drive.Kp;
  
  // calculate derivative term
  d_term = drive.Kd * (drive.lastSpeedError - speedError);
  drive.lastSpeedError = speedError;
  
  // sum to integrate steering error
  iSum += speedError;
  
  //  and limit runaway
  limit(&iSum,-iLimit,iLimit);
  
  i_term = drive.Ki*iSum;
  i_term = i_term /8; //  (divide by 4) to scale
  
  drivePWM = p_term + d_term + i_term;	
  drivePWM = drivePWM / 64; //  (divide to scale)
  
  drive.currentPWM = drivePWM;
  
  
  
  // If Debug Log is turned on, output PID data until position is stable
  if (Drive_Debug_Output == 1) {
    putstr("DRIVE spd targ curr dir: ");
    putS16(drive.targetSpeed);
    putS16(drive.currentSpeed);
    putstr(" DIR targ cur ");
    putS16(drive.targetDirection);
    putS16(drive.currentDirection);
    putstr(" DrivePWM: ");
    putS16(drivePWM);
    putstr("\r\n   P, I, D: ");
    putS16(p_term);
    putS16(i_term);
    putS16(d_term);
    putstr(" int ");
    putS16(iSum);
    putstr("\r\n");
  }  
  
  
  // use computed PWM to drive the motor
  // check current limit here -- reduce power if so? 
  
  drive.currentDirection = FORWARD;
  if (drivePWM < 0) {
    drive.currentDirection = REVERSE;
    drivePWM = abs(drivePWM);
  }  
  
  if(drivePWM < drive.minPWM) {
    if (Drive_Debug_Output == 1) 
      putstr("DRIVE under min\n");
    
    return;
  }
  
  // take care of absolute min and max
  // make sure arg2 < arg3  
  limit( &drivePWM, drive.minPWM, drive.maxPWM);
  
  // limit change of drive: make sure arg2 < arg3  
  limit( &drivePWM, drive.lastPWM - drive.deltaAccel,
	 drive.lastPWM + drive.deltaAccel);

  if(drive.currentDirection == FORWARD)
    Set_Motor1_PWM( drivePWM, drive.currentDirection );
  else
    Set_Motor1_PWM(0, FORWARD );

  drive.lastPWM = (unsigned char)drivePWM;
  
}

  
// ----------------------------------------------------------------------
// Init Pulse Width Modulation hardware for Speed Controlers.
// Output 2 channels of PWM on PortB Pins 1&2
// Ouput 2 sets of control lines (Fwd/Rev) on Port D 4:5 & 6:7
// Main Drive motor is setup as PID controlled continous rotation (drive)
// Steering Motor is setup as a Servo with a position sensing feedback pot.

void Motor_PWM_Init(void)
{
  Motor_clear_mcb( &drive );		// Only drive uses Motor Control Block
  
  // Set Port Direction bits (1=Output) and enable PWM pins and timers
  
  DDRB |= (_BV(PB1) | _BV(PB2));							// PWM Pins
  DDRD |= (_BV(PD4) | _BV(PD5) | _BV(PD6) | _BV(PD7));	// Direction control pins
  
  MOTOR1_DISABLE();	// Disable before starting PWM
  MOTOR2_DISABLE();	// Set Disable Pins High to turn OFF H-Bridge
  
  TCCR1A |= (_BV(COM1A0) | _BV(COM1A1));	// Set OC1A on compare match
  TCCR1A |= (_BV(COM1B0) | _BV(COM1B1));	// Set OC1B on compare match
  
  TCCR1A |= _BV(WGM10);		// Fast PWM Mode 5 ==> 8 Bit
  TCCR1B |= _BV(WGM12);		// both channels use same PWM mode
  
  TCCR1B |= _BV(CS10);		// 1 prescale = 31.25K Hz PWM @ 8 MHz
  
  // Make sure motors are stopped
  Set_Motor1_PWM(0, FORWARD);
  Set_Motor2_PWM(0, FORWARD);
}

// ------------------------------------------------------------------------------------------------------------------------------------------------------
// Initialize the motor control blocks

void Motor_clear_mcb( motor_control_block *m )
{
	m->targetDirection = FORWARD;
	m->currentDirection = FORWARD;
	m->targetSpeed = 0;
	m->currentSpeed = 0;
	m->currentPWM = 0;
	m->lastSpeedError = 0;

// iTerm PID
	m->Kp = 10;
	m->Ki = 0;
	m->Kd = 0;
	m->dead_band = 5;
	m->deltaAccel = 15;
	m->minPWM = 10;
	m->maxPWM = 100;
}

// ---------------------------------------------------------------------
// Special care must be taken writing to these 16 bit PWM registers.
// High byte must be written first.

void write_OCR1A( unsigned char value )
{
	OCR1AH = 0;
	OCR1AL = value;
}

void write_OCR1B( unsigned char value )
{
	OCR1BH = 0;
	OCR1BL = value;
}

// Setup torque control - turn on PID
// This is also used to setup velocity control

void Set_Drive_Speed(short t)
{
	drive.targetSpeed = t;
	if(t < 0)
	  drive.targetDirection = REVERSE;
	else
	  drive.targetDirection = FORWARD;
}


// ----------------------------------------------------------------------
// Sets the Duty Cycle and direction of motor 1 - Main Drive Motor
// This routine is tied to specific Speed Control Hardware - OSSC
// Input pwm = 0..255

void Set_Motor1_PWM(unsigned char pwm, signed char direction)
{	


  drive.currentDirection = direction;
  drive.currentPWM = pwm;

  if (pwm == 0)			// STOP - Turn off PWM
    {
      MOTOR1_DISABLE();
      write_OCR1A( 0 );
 
    }
  else
    {		
      if (direction == FORWARD)
	{
	  MOTOR1_FORWARD();  // setup direction pin & non-inverted PWM
	  write_OCR1A( pwm );	// Set PWM as 16 bit value

	  //	  putstr("\n SforwardPWM: ");
	  //	  putS16((unsigned short)pwm); 

	}
      else // direction == REVERSE
	{
	  MOTOR1_REVERSE();		// setup direction pin & inverted PWM
	  write_OCR1A( pwm );		// Set PWM as 16 bit value
	  //putstr("\n SreversePWM: ");
	  //putS16((unsigned short)pwm); 
	}
      MOTOR1_ENABLE();
    }
  

}

// ----------------------------------------------------------------------
// Sets the Duty Cycle and direction of motor 2 - Steering Motor
// Input pwm = 0..255

void Set_Motor2_PWM(unsigned char pwm, signed char direction)
{
  if (pwm == 0)		// STOP - Turn off PWM
    {
      MOTOR2_DISABLE();
      write_OCR1B( 0 );
    }
  else
    {		
      if(direction == FORWARD)
	{
	  MOTOR2_FORWARD();
	  write_OCR1B( pwm );	// Set PWM as 16 bit value
	}
      else // direction == REVERSE
	{	  
	  MOTOR2_REVERSE();
	  write_OCR1B( pwm );	// Set PWM as 16 bit value
	}
      MOTOR2_ENABLE();
    }
}


// ----------------------------------------------------------------------
// Setup the Gain factors via the Cmd line for testing & tuning.


void Drive_set_min(unsigned char c)
{
  drive.minPWM = c;
}
void Drive_set_max(unsigned char c)
{
  drive.maxPWM = c;
}
void Drive_set_dead_band(short s)
{
  drive.dead_band = s;
}

void Drive_set_integrator(short s)
{
  iSum = s;
}

void Drive_set_Kp(char c)
{
  drive.Kp = c;
}

void Drive_set_Ki(char c)
{
  drive.Ki = c;
}

void Drive_set_Kd(char c)
{
  drive.Kd = c;
}



// return motor status
void Get_Drive_Status(void)
{
  short theData;
  putstr("DTarget: ");
  putS16(drive.targetSpeed);
  putstr("\r\n Dcurrent:");
  theData = EncoderReadSpeed();
  putS16(theData);
  putstr("\r\n curPWM ");
  putS16(drive.currentPWM);
  putstr("\r\n Dir ");
  putS16(drive.currentDirection);
  putstr("\r\nIsense:");
  theData = A2D_read_channel(CURRENT_SENSE_CHANNEL);
  putS16(theData);	
  putstr("\r\n");
}
// ------------------------------------------------------------------------
// Dump motor data to serial port for feedback / debug / testing / tuning

void Motor_dump_data(void)
{
  short theData;
  putstr("Drive: (targ cur PWM) ");
  putS16(drive.targetSpeed);
  putS16(drive.currentSpeed);
  putS16(drive.currentPWM);
  putstr(" Dir ");
  putS16(drive.currentDirection);
  putstr("  PID:");
  putS16(drive.Kp);
  putS16(drive.Ki);
  putS16(drive.Kd);
  putstr("  ISense: ");
  theData = A2D_read_channel(CURRENT_SENSE_CHANNEL) - 512;
  putS16(theData);	
	
  putstr("\r\n");
  putstr("       dead minP maxP delA");
  putS16(drive.dead_band);
  putS16(drive.minPWM);
  putS16(drive.maxPWM);
  putS16(drive.deltaAccel);
  putstr("\r\n");
}

// -----------------------------------------------------------------------

void Motor_save_PID_settings()
{
  uint8_t checksum;
  checksum = 0 - (drive.Kp + drive.Ki + drive.Kd);
  eeprom_Write( MOTOR_EEPROM, drive.Kp );
  eeprom_Write( MOTOR_EEPROM+1, drive.Ki );
  eeprom_Write( MOTOR_EEPROM+2, drive.Kd );
  eeprom_Write( MOTOR_EEPROM+3, checksum );
}

// ----------------------------------------------------------------------

void Motor_read_PID_settings()
{
  uint8_t v1,v2,v3,v4,v5,checksum;
  
  v1 = eeprom_Read( MOTOR_EEPROM );
  v2 = eeprom_Read( MOTOR_EEPROM+1 );
  v3 = eeprom_Read( MOTOR_EEPROM+2 );
  checksum = eeprom_Read( MOTOR_EEPROM+3 );
  
  putstr("Init Drive PID");
  if (!((v1 + v2 + v3 + checksum) & 0xFF))
    {	// checksum is OK - load values into motor control block
      drive.Kp = v1;
      drive.Ki = v2;
      drive.Kd = v3;
      putstr(" no cksum, defaults");
    }
  else
    putstr("\r\n");
}

short limit( short *v, short minVal, short maxVal)
{
	if (*v < minVal) *v = minVal;
	if (*v > maxVal) *v = maxVal;
	return *v;
}

