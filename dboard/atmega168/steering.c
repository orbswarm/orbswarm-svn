// ---------------------------------------------------------------------
//
//	steering.c
//      SWARM Orb
//      Steering feedback PID loop  for SWARM Orb http://www.orbswarm.com
//
//	Refactored by Jonathan (Head Rotor at rotorbrain.com)
//      Original Version by Petey the Programmer  Date: 30-April-2007
// -----------------------------------------------------------------------

// This is the EEPROM address to store the motor PID coefficients
#define STEER_EEPROM	10

#include <avr/io.h>
#include <stdlib.h>
#include <avr/eeprom.h>
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
static short iSum = 0;
static short iLimit = 0;
static short steer_max=0;

extern volatile uint8_t Steer_Debug_Output;	// flag for outputing PID tuning info

// ------------------------------------------------------------------------

void Steering_Init(void)
{
	target_Pos = 0;		// start with steering centered
	Kp = 8;	// this will be divided by 10 for fractional gain values
	Ki = 0;
	Kd = 0;
	dead_band = 10;	// Set high to stop chatter, decrease for precision
	minDrive = 60;
	maxDrive = 200;
	maxAccel = 255;
	crntPWM = 0;
	iSum = 0;
	iLimit = 500;
	last_pos_error = 0;
	steer_max = 100;	/* maximum extent from zero */
}


// -------------------------------------------------------------------------
// Note: steering values are typed as shorts for PID calcs
// We only save byte values to eeprom.
void Steering_save_PID_settings(void)
{
	char checksum;
	checksum = (Kp + Ki + Kd + dead_band + minDrive + maxDrive + maxAccel);	// generate checksum

	eeprom_write_byte( STEER_EEPROM,   (char)Kp );
	eeprom_write_byte( STEER_EEPROM+1, (char)Ki );
	eeprom_write_byte( STEER_EEPROM+2, (char)Kd );
	eeprom_write_byte( STEER_EEPROM+3, (char)dead_band );
	eeprom_write_byte( STEER_EEPROM+4, (char)minDrive );
	eeprom_write_byte( STEER_EEPROM+5, (char)maxDrive );
	eeprom_write_byte( STEER_EEPROM+6, (char)maxAccel );
	eeprom_write_byte( STEER_EEPROM+7, checksum );
}

// ------------------------------------------------------------------------------------------------------------------------------------------------------

void Steering_read_PID_settings(void)
{
  uint8_t v[8];
  uint8_t n;
  char checksum, cs = 0;

  for (n=0; n<7; n++) {
    v[n] = eeprom_read_byte( STEER_EEPROM + n );
    cs += v[n];
  }
  checksum = eeprom_read_byte( STEER_EEPROM + 7 );

  putstr("Init Steering PID");
  if (checksum == cs)
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
    putstr(" no cksum, defaults");
  putstr("\r\n");

}



// -----------------------------------------------------------------------

void Steering_Set_Target_Pos(short desiredPos)
{
	limit(&desiredPos,-steer_max, steer_max);
	target_Pos = desiredPos;
}

// -----------------------------------------------------------------------
// Set params for Steering Servo PID controller via Command Line

void Steering_set_dead_band(short db)
{
	dead_band = db;
}

void Steering_set_iLimit(short db)
{
	iLimit = db;
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


void Steering_set_integrator(short v) {
  iSum = v;
}

// -----------------------------------------------------------------------


void Steering_Servo_Task(void)
{
  short steeringError;
  short motor_Drive;
  int16_t p_term, d_term, i_term;

  Steering_Read_Position();			// read Steering Feedback Pot

  // derivative term calculation
  steeringError = target_Pos - current_Pos;
  if (abs(steeringError) < dead_band) {	// we are where we want to be - no motion required
    Set_Motor2_PWM( 0, FORWARD );
    crntPWM = 0;
    iSum = 0;
    return;
  }

  // calculate p term
  p_term = steeringError * Kp;

  // calculate d term
  d_term = Kd * (last_pos_error - steeringError);
  last_pos_error = steeringError;


  // sum to integrate steering error  and limit runaway
  iSum += steeringError;

  limit(&iSum,-iLimit, iLimit);

  i_term = Ki*iSum;
  i_term = i_term >> 2; // shift right (divide by 4) to scale

  motor_Drive = (p_term + d_term + i_term);
  motor_Drive = motor_Drive /8; // shift right (divide by 8) to scale

  //limit( &motor_Drive, 0, crntPWM + maxAccel);
  crntPWM = motor_Drive;


  // If Debug Log is turned on, output PID data until position is stable
  if (Steer_Debug_Output == 1) {
    putstr("STEER PID targ curr: ");
    putS16(target_Pos);
    putS16(current_Pos);
    putstr(" Drive: ");
    putS16(motor_Drive);
    putstr("\r\n STEER P, I, D: ");
    putS16(p_term);
    putS16(i_term);
    putS16(d_term);
    putstr(" integrator ");
    putS16(iSum);
    putstr("\r\n");
  }


  if (motor_Drive > 0) {
    limit( &motor_Drive, minDrive, maxDrive);
    Set_Motor2_PWM( motor_Drive, FORWARD );
  }
  else {
    motor_Drive = abs(motor_Drive);
    limit( &motor_Drive, minDrive, maxDrive);
    Set_Motor2_PWM( motor_Drive, REVERSE );
  }

}


// --------------------------------------------------------------------
// A2D converer comes back with 10-Bit number between 0..1023
// Center of steering is 512, so it swings -512..0..512

short Steering_Read_Position(void)
{
	current_Pos = 512 - A2D_read_channel( STEERING_FEEDBACK_POT );
	return current_Pos;
}

// ------------------------------------------------------------

void Get_Steering_Status(void){
  putstr("SteerTarget: ");
  putS16(target_Pos);
  putstr("\n SteerActual: ");
  putS16(current_Pos);
  putstr("\n PWM ");
  putS16(crntPWM);
  putstr("\n int ");
  putS16(iSum);
  putstr("\n ");
}


void Steering_dump_data(void){
  putstr("Steer: target current");
  putS16(target_Pos);
  putS16(current_Pos);
  putstr(" Gain: Kp Kd Ki ");
  putS16(Kp);
  putS16(Kd);
  putS16(Ki);
  putstr("\r\n mindr maxdr maxa dead: ");
  putS16(minDrive);
  putS16(maxDrive);
  putS16(maxAccel);
  putS16(dead_band);
  putstr("\r\n");
}

