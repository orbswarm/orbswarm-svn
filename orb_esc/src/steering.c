// steering.c - Steering motor control functions for Swarm Orb-Bot
//
// Version 14.0
// Last Update: 23-May-07
// Petey the Programmer
 
#include <avr/io.h>
#include <stdlib.h>
#include "eprom.h"
#include "UART.h"
#include "putstr.h"
#include "a2d.h"
#include "motor.h"
#include "steering.h"

// Static vars for Steering Motor Control

static short current_Pos;
static short target_Pos;
static short last_pos_error;
static short dead_band;
static short Kp;
static short Ki;
static short Kd;
static short minDrive;
static short maxDrive;
static short maxAccel;
static short crntPWM;
static short iSum;

extern volatile uint8_t Debug_Output;	// flag for outputing PID tuning info

// ------------------------------------------------------------------------------------------------------------------------------------------------------
// New inital values provided by Jon & Michael 20-May-07

void Steering_Init(void)
{
	target_Pos = 0;		// start with steering centered
	Kp = 16;			// this will be divided by 10 for fractional gain values
	Ki = 20;
	Kd = 10;
	dead_band = 10;		// Set high to stop chatter, decrease for precision
	minDrive = 60;
	maxDrive = 200;
	maxAccel = 255;
	crntPWM = 0;
	iSum = 0;
	last_pos_error = 0;
}
	
// ------------------------------------------------------------------------------------------------------------------------------------------------------
// Note: steering values are typed as shorts for PID calcs
// We only save byte values to eeprom.

void Steering_save_PID_settings(void)
{
	uint8_t checksum;
	checksum = 0 - (Kp + Ki + Kd + dead_band + minDrive + maxDrive + maxAccel);	// generate checksum
	
	eeprom_Write( STEER_EEPROM,   Kp );
	eeprom_Write( STEER_EEPROM+1, Ki );
	eeprom_Write( STEER_EEPROM+2, Kd );
	eeprom_Write( STEER_EEPROM+3, dead_band );
	eeprom_Write( STEER_EEPROM+4, minDrive );
	eeprom_Write( STEER_EEPROM+5, maxDrive );
	eeprom_Write( STEER_EEPROM+6, maxAccel );
	eeprom_Write( STEER_EEPROM+7, checksum );
}

// ------------------------------------------------------------------------------------------------------------------------------------------------------

void Steering_read_PID_settings(void)
{
	uint8_t v[7];
	uint8_t n, checksum, cs = 0;

	for (n=0; n<7; n++) {
		v[n] = eeprom_Read( STEER_EEPROM + n );
		cs += v[n];
		}
	checksum = eeprom_Read( STEER_EEPROM + 7 );

	if (!((cs + checksum) & 0xFF))
		{	// checksum is OK - load values into motor control block
		Kp = v[0];
		Ki = v[1];
		Kd = v[2];
		dead_band = v[3];
		minDrive = v[4];
		maxDrive = v[5];
		maxAccel = v[6];
		}
	else
		putstr("Init Steering PIDs\r\n");
}

// ------------------------------------------------------------------------------------------------------------------------------------------------------

void Steering_Set_Target_Pos(short targetPos)
{
	target_Pos = targetPos;
	Steering_do_Servo_Task();
}

// ------------------------------------------------------------------------------------------------------------------------------------------------------
// Set params for Steering Servo PID controller via Command Line

void Steering_set_dead_band(short db)
{
	dead_band = db;
}

void Steering_set_Kp(short v)
{
	Kp = v;
}

void Steering_set_Ki(short v)
{
	Ki = v;
}

void Steering_set_Kd(short v)
{
	Kd = v;
}

void Steering_set_min(short v)
{
	minDrive = v;
}

void Steering_set_max(short v)
{
	maxDrive = v;
}

void Steering_set_accel(short v)
{
	maxAccel = v;
}

// ------------------------------------------------------------------------------------------------------------------------------------------------------

short limit( short *v, short minVal, short maxVal);
short limit( short *v, short minVal, short maxVal)
{
	if (*v < minVal) *v = minVal;
	if (*v > maxVal) *v = maxVal;
	return *v;
}

// ------------------------------------------------------------------------------------------------------------------------------------------------------
// This is the Steering Servo PID function
// Called 10 times per second from main loop
// PID params can be set from the command line

void Steering_do_Servo_Task(void)
{
	short steeringError, D_Factor, I_Factor;
	short motor_Drive;
	
	Steering_Read_Position();			// read Steering Feedback Pot
	
	if (abs(current_Pos - target_Pos) < dead_band) {	// we are where we want to be - no motion required
		Set_Motor2_PWM( 0, FORWARD );
		crntPWM = 0;
/*
	putstr("StrStop: ");
	putS16(target_Pos);
	putS16(current_Pos);
	putS16(dead_band);
	putstr("ABS: ");
	putS16(abs(current_Pos - target_Pos));
	putstr("\n\r");
*/	
		return;
		}
	

	// derivative term calculation
	steeringError = abs(target_Pos - current_Pos);
	D_Factor = steeringError - last_pos_error;
//	limit(pos_derivative, -255, 255);
	last_pos_error = steeringError;

	// Integral term
	iSum += (steeringError / 10);
	I_Factor = limit( &iSum, -400, 400);
	
	motor_Drive = (I_Factor * Ki) + (D_Factor * Kd) + abs(steeringError * Kp);	
	motor_Drive = motor_Drive / 10;		// scale
	
	limit( &motor_Drive, 0, crntPWM + maxAccel);
	limit( &motor_Drive, minDrive, maxDrive);

	if (current_Pos < target_Pos) {		// need to move steering arm to desired position
		Set_Motor2_PWM( motor_Drive, FORWARD );
		}
	
	if (current_Pos > target_Pos) {
		Set_Motor2_PWM( motor_Drive, REVERSE );
		}

	crntPWM = motor_Drive;

	// If Debug Log is turned on, output PID data until position is stable
	if (Debug_Output == 1) {
		putstr("StPID: ");
		putS16(target_Pos);
		putS16(current_Pos);
		putstr(" Drive: ");
		putS16(motor_Drive);
		putS16(I_Factor);
		putS16(D_Factor);
		putstr("\n\r");
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

void Steering_dump_data(void)
{
	putstr("Steer: ");
	putS16(target_Pos);
	putS16(current_Pos);
	putstr(" Gain: ");
	putS16(Kp);
	putS16(Kd);
	putS16(minDrive);
	putS16(maxDrive);
	putS16(maxAccel);
	putS16(dead_band);
	putstr("\n\r");
}

// ------------------------------------------------------------------------------------------------------------------------------------------------------
// End of File
