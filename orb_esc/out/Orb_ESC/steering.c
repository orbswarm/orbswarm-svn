// motor.c - Motor controller functions for Swarm Orb-Bot
 
#include <avr/io.h>
#include "a2d.h"
#include "motor.h"
#include "steering.h"

static short current_Pos;
static short target_Pos;

// ------------------------------------------------------------------------------------------------------------------------------------------------------

void Steering_Init(void)
{
	Steering_Read_Position();		// read Steering Feedback Pot
	target_Pos = current_Pos;
}
	
// ------------------------------------------------------------------------------------------------------------------------------------------------------

void Steering_Set_Target_Pos(short targetPos)
{
	target_Pos = targetPos;
	Steering_do_Servo_Task();
}

// ------------------------------------------------------------------------------------------------------------------------------------------------------
// This will become the Steering Servo PID function

void Steering_do_Servo_Task(void)
{
	short steeringError;
	short motor_Drive;
	short Kp = 2;
	
	Steering_Read_Position();			// read Steering Feedback Pot
	
	if (current_Pos == target_Pos) {	// we are where we want to be - no motion required
		Set_Motor2_PWM( 0, FORWARD );
		}
	
	if (current_Pos < target_Pos) {		// need to move steering arm to desired position
		steeringError = target_Pos - current_Pos;
		motor_Drive = steeringError * Kp;
		Set_Motor2_PWM( motor_Drive, FORWARD );
		}
	
	if (current_Pos > target_Pos) {
		steeringError = current_Pos - target_Pos;
		motor_Drive = steeringError * Kp;
		Set_Motor2_PWM( motor_Drive, REVERSE );
		}
}

// ------------------------------------------------------------------------------------------------------------------------------------------------------
// A2D converer comes back with 10-Bit number between 0..1023
// Center of steering is 512, so it swings -512..0..512

short Steering_Read_Position(void)
{
	current_Pos = 512 - A2D_read_channel( STEERING_FEEDBACK_POT );
	return current_Pos;
}

// ------------------------------------------------------------------------------------------------------------------------------------------------------
// End of File
