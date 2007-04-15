// motor.c - Motor controller functions for Swarm Orb-Bot
//
// Output 2 channels of PWM on PortB Pins 1&2
// Ouput 2 sets of control lines (Fwd/Rev) on Port D 4:5 & 6:7
// Main Drive motor is setup as PID controlled continous rotation (drive)
// Steering Motor is setup as a Servo with a position sensing feedback pot.

#include <avr/io.h>
#include "UART.h"
#include "putstr.h"
#include "motor.h"
#include "encoder.h"


// These are chip dependant port pins
// For ATMega8 OC1A is PortB1, OC1B is PortB2
// Speed Control Direction Bits are Port D4:5 & D6:7

#define MC1_Fwd_Pin 0x80			// b 1000 0000		PortD:7
#define MC1_Rev_Pin 0x40			// b 0100 0000		PortD:6

#define MC2_Fwd_Pin 0x20			// b 0010 0000		PortD:5
#define MC2_Rev_Pin 0x10			// b 0001 0000		PortD:4

#define MC1_PWM_MASK 0x02			// b 0000 0010		PortB1
#define MC2_PWM_MASK 0x04			// b 0000 0100		PortB2

// ------------------------------------------------------------------------------------------------------------------------------------------------------
// Motor Control Blocks for keeping track of Motor PID function variables

typedef struct {
	char	Kp;
	char	Ki;
	char	Kd;
	char	dir;
	char	PID_state;
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
static motor_control_block motor1, motor2;

/* Prototype */
void Motor_clear_mcb( motor_control_block *m );

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
// This routine is tied to specific Speed Control Hardware
// input value is 0..100

void Set_Motor1_PWM(unsigned char pwm, signed char direction)
{	
	if (pwm == 0)		// STOP - Turn off PWM
		{
		write_OCR1A( 0 );
		PORTD &= ~(MC1_Fwd_Pin | MC1_Rev_Pin);		// clear both pins
		}
	else
		{		
		if (direction == FORWARD)
			{
			write_OCR1A( pwm );		// Set PWM as 16 bit value
			PORTD |= MC1_Fwd_Pin;	// Set pin
			PORTD &= ~MC1_Rev_Pin;	// Clear pin
			}
		else // direction == REVERSE
			{
			write_OCR1A( pwm );		// Set PWM as 16 bit value
			PORTD &= ~MC1_Fwd_Pin;	// Clear pin
			PORTD |= MC1_Rev_Pin;	// Set pin
			}
		}
		
	motor1.dir = direction;
	motor1.PWM_Set = pwm;
}

// ------------------------------------------------------------------------------------------------------------------------------------------------------
// Sets the Duty Cycle and direction of motor 2 - Steering Motor
// pwm = 0..255

void Set_Motor2_PWM(unsigned char pwm, signed char direction)
{
	if (pwm == 0)		// STOP - Turn off PWM
		{
		write_OCR1B( 0 );
		PORTD &= ~(MC2_Fwd_Pin | MC2_Rev_Pin);
		}
	else
		{		
		if(direction == FORWARD)
			{
			write_OCR1B( pwm );	// Set PWM as 16 bit value
			PORTD |= MC2_Fwd_Pin;	// Set direction pins
			PORTD &= ~MC2_Rev_Pin;
			}
		else // direction == REVERSE
			{
			write_OCR1B( pwm );	// Set PWM as 16 bit value
			PORTD &= ~MC2_Fwd_Pin;	// Clear pin
			PORTD |= MC2_Rev_Pin;	// Set pin
			}
		}
}

// ------------------------------------------------------------------------------------------------------------------------------------------------------
// Init Pulse Width Modulation hardware for Speed Controlers.
// Output 2 channels of PWM on PortB Pins 1&2
// Ouput 2 sets of control lines (Fwd/Rev) on Port D 4:5 & 6:7
// Main Drive motor is setup as PID controlled continous rotation (drive)
// Steering Motor is setup as a Servo with a position sensing feedback pot.

void Motor_PWM_Init(void)
{
	Motor_clear_mcb( &motor1 );
	Motor_clear_mcb( &motor2 );
	
    // Set Port Direction bits (1=Output) and enable PWM pins and timers
	
    DDRB |= MC2_PWM_MASK | MC1_PWM_MASK;
    DDRD |= MC1_Fwd_Pin | MC1_Rev_Pin;    

	TCCR1A |= (1<<COM1A0) | (1<<COM1A1);	// Set OC1A on compare match
    TCCR1A |= (1<<COM1B0) | (1<<COM1B1);	// Set OC1B on compare match
	
    TCCR1A |= (1<<WGM10);		// Fast PWM Mode 5 ==> 8 Bit
	TCCR1B |= (1<<WGM12);
	
    TCCR1B |= (1<<CS11);		// 8 prescale = 1.2khz PWM @3.6MHz
 
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
}

// ------------------------------------------------------------------------------------------------------------------------------------------------------

char Motor_Read_Drive_Direction(void)
{
	return motor1.dir;
}

// ------------------------------------------------------------------------------------------------------------------------------------------------------

void Motor_Set_Drive_Direction(char theDir)
{
	motor1.dir = theDir;
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

// ------------------------------------------------------------------------------------------------------------------------------------------------------
// Dump motor data to serial port for feedback / debug / testing / tuning

void Motor_dump_data(void)
{
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
	putstr("\r\n");
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
// Run PID function for Main Drive Motor.
// This can be used for speed, or current control.
// Many different variations of PID functions can be run in here.
// Called 10 times per second.  
// Over oscillates small test rig w Kp = 4, Ki = 0.01, Kd = 4

/*
void xMotor_do_motor_control(void);
void xMotor_do_motor_control(void)
{
	short Error_Term, P_Term, D_Term, I_Term, motor_drive;
	
	motor1.crnt_value = Encoder_read_speed(MOTOR1_SHAFT_ENCODER);
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
	
	// limit to current_value + 20 - be nice to motor during spin up.
	
	if (motor_drive > (motor1.PWM_Set + 10))
		motor_drive = motor1.PWM_Set + 10;
		
	Set_Motor1_PWM( motor_drive, motor1.dir );		// PWM input is 0..255
}

*/
// ------------------------------------------------------------------------------------------------------------------------------------------------------
// PID #3 - iTerm PID
// Uses I_Term to adjust and store neutral 'bias' value.
//
// Works great with small test motor & speed sensor setup - 
// deals with time lag very smoothly.  Kp = 8, Ki = 10, Kd = 4;

void Motor_do_motor_control(void)
{
	short Error_Term, P_Term=0, D_Term=0, I_Term, motor_drive;
	
	motor1.crnt_value = Encoder_read_speed(MOTOR1_SHAFT_ENCODER);
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
	
	if (motor_drive > 160) motor_drive = 160;		// limit while testing
	Set_Motor1_PWM( motor_drive, motor1.dir );		// PWM input is 0..255  50% = 128
}

// ------------------------------------------------------------------------------------------------------------------------------------------------------
// End of File
