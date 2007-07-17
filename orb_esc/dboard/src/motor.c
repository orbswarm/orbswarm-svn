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
  char	Kp;
  char	Ki;
  char	Kd;
  char	dir;
  unsigned short targetSpeed;	
  unsigned short currentSpeed;
  short	currentPWM;
  short	dead_band;
  short lastSpeedError;
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

  
  // derivative term calculation
  drive.currentSpeed = EncoderReadSpeed();
  speedError = drive.currentSpeed - drive.targetSpeed; 

  //  if (abs(speedError) < drive.dead_band) {	// we are at desired speed
    // don't change anything
    //return;
  //}
  
  // check overcurrent here?

  // calculate p term
  p_term = speedError * drive.Kp;

  // calculate d term
  d_term = drive.Kd * (drive.lastSpeedError - speedError);
  drive.lastSpeedError = speedError;
  
  // sum to integrate steering error and limit runaway
  iSum += speedError;

  limit(&iSum,iLimit,-iLimit);

  i_term = drive.Ki*iSum;
  i_term = i_term >> 2; // shift right (divide by 4) to scale

  drivePWM = (p_term + d_term + i_term);	
  drivePWM = drivePWM >> 3; // shift right (divide by 8) to scale
  
  drive.currentPWM = drivePWM;

  // If Debug Log is turned on, output PID data until position is stable
  if (Drive_Debug_Output == 1) {
    putstr("DRIVE PID targ curr: ");
    putS16(drive.targetSpeed);
    putS16(drive.currentSpeed);
    putstr(" DrivePWM: ");
    putS16(drivePWM);
    putstr("\n\r DRIVE P, I, D: ");
    putS16(p_term);
    putS16(i_term);
    putS16(d_term);
    putstr(" integrator ");
    putS16(iSum);
    putstr("\n\r");
  }
  
  // use computed PWM to drive the motor
  // check current limit here -- reduce power if so? 

  if (drivePWM > 0) { 
    limit( &drivePWM, drive.dead_band, 100);
    Set_Motor1_PWM(drivePWM, FORWARD );
  }
  else {
    drivePWM = abs(drivePWM);
    limit( &drivePWM, drive.dead_band, 100);
    Set_Motor1_PWM( drivePWM, REVERSE );
  }
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
	m->dir = FORWARD;
	m->targetSpeed = 0;
	m->currentSpeed = 0;
	m->currentPWM = 0;
	m->lastSpeedError = 0;

// iTerm PID
	m->Kp = 5;
	m->Ki = 0;
	m->Kd = 0;
	m->dead_band = 10;
	
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

void Set_Drive_Speed(unsigned char t, signed char direction)
{
	drive.targetSpeed = t;
	drive.dir = direction;
}


// ----------------------------------------------------------------------
// Sets the Duty Cycle and direction of motor 1 - Main Drive Motor
// This routine is tied to specific Speed Control Hardware - OSSC
// Input pwm = 0..255

void Set_Motor1_PWM(unsigned char pwm, signed char direction)
{	

  pwm = pwm & 0xFF; 		/* limit to 0 - 255  */
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
	}
      else // direction == REVERSE
	{
	  MOTOR1_REVERSE();		// setup direction pin & inverted PWM
	  write_OCR1A( pwm );		// Set PWM as 16 bit value
	}
      MOTOR1_ENABLE();
    }
  
  drive.dir = direction;
  drive.currentPWM = pwm;

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

void Set_Drive_Deadband(short s)
{
	drive.dead_band = s;
}

void Motor_set_Kp(char c)
{
	drive.Kp = c;
}

void Motor_set_Ki(char c)
{
	drive.Ki = c;
}

void Motor_set_Kd(char c)
{
	drive.Kd = c;
}



// return motor status
void Get_Drive_Status(void)
{
  short theData;
  putstr("TargetSpeed: ");
  putS16(drive.targetSpeed);
  putstr("\n\r currentSpeed:");
  putS16(drive.currentSpeed);
  putstr("\n\r PWM ");
  putS16(drive.currentPWM);
  putstr("\n\r Direction ");
  putS16(drive.dir);
  putstr("\n\rCurrentSense:");
  theData = A2D_read_channel(CURRENT_SENSE_CHANNEL);
  putS16(theData);	
  putstr("\n\r");
}
// ------------------------------------------------------------------------
// Dump motor data to serial port for feedback / debug / testing / tuning

void Motor_dump_data(void)
{
  short theData;
  putstr("Drive: target current PWM ");
  putS16(drive.targetSpeed);
  putS16(drive.currentSpeed);
  putS16(drive.currentPWM);
  putstr(" Dir ");
  putS16(drive.dir);
  putstr("  PID:");
  putS16(drive.Kp);
  putS16(drive.Ki);
  putS16(drive.Kd);
  putstr("  currentSense:");
  theData = A2D_read_channel(CURRENT_SENSE_CHANNEL) - 512;
  putS16(theData);	
	
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
  
  if (!((v1 + v2 + v3 + checksum) & 0xFF))
    {	// checksum is OK - load values into motor control block
      drive.Kp = v1;
      drive.Ki = v2;
      drive.Kd = v3;
      putstr("Init Motor PIDs\r\n");
    }
  else
    putstr("Init Motor PIDs: no checksum, default values\r\n");
}

short limit( short *v, short minVal, short maxVal)
{
	if (*v < minVal) *v = minVal;
	if (*v > maxVal) *v = maxVal;
	return *v;
}

