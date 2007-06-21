// motor.c - Motor controller functions for Swarm Orb-Bot
//
// Version: 15.0
// Date: 12-5-2007
// Switched to VNH H-Bridge Hardware 
// Now saving PID gain factors to EEPROM

// Output 2 channels of PWM on PortB Pins 1&2
// Ouput 2 sets of control lines (Dir/Disable) on Port D 4:5 & 6:7
// Main Drive motor is setup as PID controlled continuous rotation (main drive)
// Steering Motor is setup as a Servo with a position sensing feedback pot.

#include <avr/io.h>
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
//		PortB:1 = PWM Pin for Motor1 (DIP pin 15 -- VNH PWM pin) 
//		PortD:4 = A side enable (DIP pin 6, VNH pin INA)
//		PortD:5 = B side enable (DIP pin 11, VNH pin INB) 
//                      Set both low to brake
//
//		PortB:2 = PWM Pin for Motor2
//		PortD:6 = Direction Pin for Motor2
//		PortD:7 = Disable Pin for Motor2 - Set High to disable H-Bridge

// ------- New H-Bridge Hardware - VNH
// No PWM inversion needed on direction change

// MOTOR1_FORWARD: clear PD:4 set PD:5; 
//#define MOTOR1_FORWARD()  PORTD &= ~_BV(PD4); TCCR1A &= ~_BV(COM1A0); TCCR1A |= _BV(COM1A1)
#define MOTOR1_FORWARD()  PORTD &= ~_BV(PD4); PORTD |= _BV(PD5);
// MOTOR1_REVERSE: SET PD:4 (direction); 
//#define MOTOR1_REVERSE()  PORTD |= _BV(PD4); TCCR1A |= (_BV(COM1A0) | _BV(COM1A1))
// reverse: set PD:4 clear PD:5; 
#define MOTOR1_REVERSE()  PORTD |= _BV(PD4); PORTD &= ~_BV(PD5); 

// DISABLE: SET PD:5
#define MOTOR1_DISABLE() PORTD |= _BV(PD5);
// ENABLE: CLEAR PD:5
#define MOTOR1_ENABLE()  PORTD &= ~_BV(PD5);

#define MOTOR2_FORWARD() PORTD &= ~_BV(PD6); 
#define MOTOR2_REVERSE() PORTD |= _BV(PD6); 
#define MOTOR2_DISABLE() PORTD |= _BV(PD7)
#define MOTOR2_ENABLE()  PORTD &= ~_BV(PD7)


// ------------------------------------------------------------------------------------------------------------------------------------------------------
// Motor Control Block for keeping track of Motor PID function variables

typedef struct {
	char	Kp;
	char	Ki;
	char	Kd;
	char	dir;
	char	PID_state;
	char	doing_Velocity_PID;
	unsigned short	target_value;	
	unsigned short	last_set_point;	
	unsigned short	prev_value;
	unsigned short	crnt_value;
	short	power_setting;
	short	PWM_Set;	
	short	I_State;
	short	D_State;
	short	initialError;
} motor_control_block;

/* Static Vars */
static motor_control_block motor1;

/* Prototype */
void Motor_clear_mcb( motor_control_block *m );
short Motor_read_feedback_data(void);

void Motor_do_iTerm_PID(void);
void Motor_do_Std_PID(void);

// ------------------------------------------------------------------------------------------------------------------------------------------------------
// Init Pulse Width Modulation hardware for Speed Controlers.
// Output 2 channels of PWM on PortB Pins 1&2
// Ouput 2 sets of control lines (Fwd/Rev) on Port D 4:5 & 6:7
// Main Drive motor is setup as PID controlled continous rotation (drive)
// Steering Motor is setup as a Servo with a position sensing feedback pot.

void Motor_PWM_Init(void)
{
  Motor_clear_mcb( &motor1 );		// Only motor1 uses Motor Control Block
  
  // Set Port Direction bits (1=Output) and enable PWM pins and timers
  
  DDRB |= (_BV(PB1) | _BV(PB2));							// PWM Pins
  DDRD |= (_BV(PD4) | _BV(PD5) | _BV(PD6) | _BV(PD7));	// Direction control pins
  
  //MOTOR1_DISABLE();	// Disable before starting PWM
  //MOTOR2_DISABLE();	// Set Disable Pins High to turn OFF H-Bridge
  
  TCCR1A |= _BV(COM1A1);	
  TCCR1A |= _BV(COM1B1);	
  TCCR1A &= ~_BV(COM1A0);	// Clear OC1A on compare match
  TCCR1A &= ~_BV(COM1B0);	// Clear OC1B on compare match

	
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
	m->target_value = 0;
	m->prev_value = 0;

	m->PWM_Set = 0;
	m->I_State = 0;
	m->D_State = 0;

// iTerm PID
	m->Kp = 8;
	m->Ki = 10;
	m->Kd = 4;
	
/* normal PID	
	m->Kp = 4;
	m->Ki = 1;
	m->Kd = 4;
*/
	m->PID_state = 0;
	m->doing_Velocity_PID = 1;
}

// ------------------------------------------------------------------------------------------------------------------------------------------------------
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

// ------------------------------------------------------------------------------------------------------------------------------------------------------
// Setup torque control - turn on PID
// This is also used to setup velocity control

void Set_Motor1_Torque(unsigned char t, signed char direction)
{
	motor1.target_value = t;
	motor1.dir = direction;
}

// ------------------------------------------------------------------------------------------------------------------------------------------------------
// Set power -100..0..100 as percent of PWM (from cmd line)

void Set_Motor1_Power(unsigned char power, signed char direction)
{
	short tmpData;

	tmpData = (power * 255) / 100;
	if (tmpData > 255) tmpData = 255;

	Set_Motor1_PWM( tmpData, direction);

	motor1.power_setting = power;
}

// ------------------------------------------------------------------------------------------------------------------------------------------------------
// Sets the Duty Cycle and direction of motor 1 - Main Drive Motor
// This routine is tied to specific Speed Control Hardware - OSSC
// Input pwm = 0..255

void Set_Motor1_PWM(unsigned char pwm, signed char direction)
{	
  if (pwm == 0)		// STOP - Turn off PWM
    {
      //MOTOR1_DISABLE();
      write_OCR1A( 0 );
    }
  else
    {		
      if (direction == FORWARD)
	{
	  MOTOR1_FORWARD();		// setup direction pin & non-inverted PWM
	  write_OCR1A( pwm );		// Set PWM as 16 bit value
	}
      else // direction == REVERSE
	{
	  MOTOR1_REVERSE();		// setup direction pin & inverted PWM
	  write_OCR1A( pwm );		// Set PWM as 16 bit value
	}
      //MOTOR1_ENABLE();
    }
  
  motor1.dir = direction;
  motor1.PWM_Set = pwm;
}

// ------------------------------------------------------------------------------------------------------------------------------------------------------
// Sets the Duty Cycle and direction of motor 2 - Steering Motor
// Input pwm = 0..255

void Set_Motor2_PWM(unsigned char pwm, signed char direction)
{
  if (pwm == 0)		// STOP - Turn off PWM
    {
      //MOTOR2_DISABLE();
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
      //MOTOR2_ENABLE();
    }
}

// ------------------------------------------------------------------------------------------------------------------------------------------------------

char Motor_Read_Drive_Direction(void)
{
	return motor1.dir;
}

char Motor_Read_Drive_PWM(void)
{
	return motor1.PWM_Set;
}

// ------------------------------------------------------------------------------------------------------------------------------------------------------
// Setup the Gain factors via the Cmd line for testing & tuning.

void Motor_set_Kp(char c)
{
	motor1.Kp = c;
}

void Motor_set_Ki(char c)
{
	motor1.Ki = c;
}

void Motor_set_Kd(char c)
{
	motor1.Kd = c;
}

void Motor_set_PID_feedback(char c)
{
	motor1.doing_Velocity_PID = c;		// 1 = Velocity, 0 = Torque
}

// ------------------------------------------------------------------------------------------------------------------------------------------------------
// Dump motor data to serial port for feedback / debug / testing / tuning

void Motor_dump_data(void)
{
  short theData;

  putstr("Motor1: ");
  putS16(motor1.target_value);
  putS16(motor1.crnt_value);
  putS16(motor1.PWM_Set);
  putstr("  PID:");
  putS16(motor1.Kp);
  putS16(motor1.Ki);
  putS16(motor1.Kd);
  putstr("  ISav:");
  putS16(motor1.I_State);	
  putstr("\n  encoder speed:");
  theData = Encoder_read_speed(MOTOR1_SHAFT_ENCODER);
  putS16(theData);	

  putstr("\n  current sense:");
  theData = A2D_read_channel(CURRENT_SENSE_CHANNEL) - 512;
  putS16(theData);	

  if (motor1.doing_Velocity_PID)
    putstr("  Velocity");
  else
    putstr("  Torque");
  
  putstr("\r\n");
}

// ------------------------------------------------------------------------------------------------------------------------------------------------------

void Motor_save_PID_settings(uint8_t iTermFlag)
{
	uint8_t checksum;
	checksum = 0 - (motor1.Kp + motor1.Ki + motor1.Kd + motor1.doing_Velocity_PID + iTermFlag);
	
	eeprom_Write( MOTOR_EEPROM, motor1.Kp );
	eeprom_Write( MOTOR_EEPROM+1, motor1.Ki );
	eeprom_Write( MOTOR_EEPROM+2, motor1.Kd );
	eeprom_Write( MOTOR_EEPROM+3, motor1.doing_Velocity_PID );
	eeprom_Write( MOTOR_EEPROM+4, iTermFlag );
	eeprom_Write( MOTOR_EEPROM+5, checksum );
}

// ------------------------------------------------------------------------------------------------------------------------------------------------------

void Motor_read_PID_settings(uint8_t *iTermFlag)
{
	uint8_t v1,v2,v3,v4,v5,checksum;

	v1 = eeprom_Read( MOTOR_EEPROM );
	v2 = eeprom_Read( MOTOR_EEPROM+1 );
	v3 = eeprom_Read( MOTOR_EEPROM+2 );
	v4 = eeprom_Read( MOTOR_EEPROM+3 );
	v5 = eeprom_Read( MOTOR_EEPROM+4 );
	checksum = eeprom_Read( MOTOR_EEPROM+5 );

	if (!((v1 + v2 + v3 + v4 + v5 + checksum) & 0xFF))
		{	// checksum is OK - load values into motor control block
		motor1.Kp = v1;
		motor1.Ki = v2;
		motor1.Kd = v3;
		motor1.doing_Velocity_PID = v4;
		*iTermFlag = v5;
		}
	else
		putstr("Init Motor PIDs\r\n");
}

// ------------------------------------------------------------------------------------------------------------------------------------------------------
// Basic PID routine without limiters.

/*
void Motor_do_motor_control_PID(motor_control_block *motor);
void Motor_do_motor_control_PID(motor_control_block *motor)
{
	short Error_Term, P_Term, D_Term, I_Term, motor_drive;
	
	motor->crnt_value = Encoder_read_speed(MOTOR1_SHAFT_ENCODER);	// or current sensor
	Error_Term = motor->target_value - motor->crnt_value;
	
	P_Term = motor->Kp * Error_Term;
	D_Term = motor->Kd * (Error_Term - motor->D_State);
	motor->D_State = Error_Term;
	
	motor->I_State += Error_Term;	
	I_Term = (motor->Ki * motor->I_State) / 100;	// Ki is in-effect 0.01
	
	motor_drive = motor->PWM_Set + P_Term + I_Term + D_Term;
			
	Set_Motor_PWM( motor, motor_drive, motor->dir );		// PWM input is 0..255
}
*/

// ------------------------------------------------------------------------------------------------------------------------------------------------------
// Read either the velocity, or the Hall-effect current sensor.
// Depends on whether we're doing velocity, or torque control.

short Motor_read_feedback_data(void)
	{
	short theData;
	
	if (motor1.doing_Velocity_PID)
		theData = Encoder_read_speed(MOTOR1_SHAFT_ENCODER);
	else
		theData = A2D_read_channel(CURRENT_SENSE_CHANNEL) - 512;
		
	return theData;
	}

// ------------------------------------------------------------------------------------------------------------------------------------------------------

void Motor_do_motor_control(char use_iTerm_PID)
{
	if (use_iTerm_PID)
		Motor_do_iTerm_PID();
	else
		Motor_do_Std_PID();
}

// ------------------------------------------------------------------------------------------------------------------------------------------------------
// Run PID function for Main Drive Motor.
// This can be used for speed, or current control.
// Many different variations of PID functions can be run in here.
// Called 10 times per second.  
// Over oscillates small test rig w/ Kp = 4, Ki = 0.01, Kd = 4

void Motor_do_Std_PID(void)
{
	short Error_Term, P_Term, D_Term, I_Term, motor_drive;
	
	motor1.crnt_value = Motor_read_feedback_data();		// either velocity or torque
	Error_Term = motor1.target_value - motor1.crnt_value;
	
	P_Term = motor1.Kp * Error_Term;
	D_Term = motor1.Kd * (Error_Term - motor1.D_State);
	motor1.D_State = Error_Term;
	
	motor1.I_State += Error_Term;
	if (motor1.I_State > 2500)		// prevent I_Term run-away
		motor1.I_State = 2500;		
	if (motor1.I_State < -2500) 
		motor1.I_State = -2500;
	
	I_Term = (motor1.Ki * motor1.I_State) / 100;	// Ki is in-effect 0.01
	
	motor_drive = motor1.PWM_Set + P_Term + I_Term + D_Term;

	if (motor_drive > 255)		// limit PWM value to 0..255
		motor_drive = 255;		// don't allow reversing directions from here
	if (motor_drive < 0)
		motor_drive = 0;
	
	// limit to current_value + xx - be nice to motor during spin up.
	
	if (motor_drive > (motor1.PWM_Set + 10))
		motor_drive = motor1.PWM_Set + 10;
		
	Set_Motor1_PWM( motor_drive, motor1.dir );		// PWM input is 0..255
}


// ------------------------------------------------------------------------------------------------------------------------------------------------------
// PID #3 - iTerm PID
// Uses I_Term to adjust and store neutral 'bias' value.
//
// Works great with small test motor & speed sensor setup - 
// deals with time lag very smoothly.  Kp = 8, Ki = 10, Kd = 4;

void Motor_do_iTerm_PID(void)
{
	short Error_Term, P_Term=0, D_Term=0, I_Term, motor_drive;
	
	motor1.crnt_value = Motor_read_feedback_data();		// either velocity or torque
	Error_Term = motor1.target_value - motor1.crnt_value;

	motor1.I_State += Error_Term;
	if (motor1.I_State > 2550) motor1.I_State = 2550;	// prevent run-away
	if (motor1.I_State < -2550) motor1.I_State = -2550;
	
	I_Term = (motor1.Ki * motor1.I_State) / 10;	// Ki is in-effect 0.1

	if (motor1.PID_state < 2) {	// wait for iTerm to spin up...

		if (motor1.PID_state == 0) {
			motor1.PID_state = 1;
			motor1.initialError = Error_Term;
			}
			
		// wait for error to over-correct via iTerm before entering normal operation
		if (((motor1.initialError > 0) && (Error_Term < 0)) 
		 || ((motor1.initialError < 0) && (Error_Term > 0)) ){
			motor1.PID_state = 2;	// begin normal operation
			motor1.prev_value = motor1.crnt_value;
			}
		
		motor1.last_set_point = motor1.target_value;	// remember current set-point
		}
		
	else	// normal operation
		{
		// calculate D_Term...
		D_Term = motor1.Kd * (motor1.prev_value - motor1.crnt_value);
		motor1.prev_value = motor1.crnt_value;
		
		if (motor1.target_value == motor1.last_set_point) 		// setpoint has not changed
			{
			// calculate P_Term...
			P_Term = motor1.Kp * Error_Term;
						
			}
		else	// setpoint has changed - new target_value
			{
			// reset P&D terms - let I_Term spin up / down
			P_Term = 0;
			D_Term = 0;
			
			if ((motor1.target_value > motor1.last_set_point) 
			 && (motor1.crnt_value > motor1.target_value)) {
				motor1.last_set_point = motor1.target_value;	// return to "normal operation"
				motor1.prev_value = motor1.crnt_value;
				}
			
			if ((motor1.target_value < motor1.last_set_point) 
			 && (motor1.crnt_value < motor1.target_value)) {
				motor1.last_set_point = motor1.target_value;	// return to "normal operation"
				motor1.prev_value = motor1.crnt_value;
				}
			
			} // end of "setpoint has changed"

		} // end of "normal operation"

// ---

	motor_drive = (P_Term + I_Term + D_Term) / 10;

	/*
	putstr("Terms: ");	// output data for tuning / debug -- 
	putS16( P_Term );	// requires Hi-speed debug port (38.4k baud UART)
	putS16( I_Term );
	putS16( D_Term );
	putS16( motor_drive );
	putstr("\r\n");
	*/
	
	if (motor_drive > 255) motor_drive = 255;
	if (motor_drive < 0) motor_drive = 0;
	
//	if (motor_drive > 160) motor_drive = 160;		// limit while testing
	Set_Motor1_PWM( motor_drive, motor1.dir );		// PWM input is 0..255  50% = 128
}

// ------------------------------------------------------------------------------------------------------------------------------------------------------
// End of File
