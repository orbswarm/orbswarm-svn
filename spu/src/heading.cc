// ---------------------------------------------------------------------
// 
//	heading.c 
//      SWARM Orb 
//      heading feedback PID loop  for SWARM Orb http://www.orbswarm.com
//
//	Refactored by Jonathan (Head Rotor at rotorbrain.com)
//      Original Version by Petey the Programmer  Date: 30-April-2007
// -----------------------------------------------------------------------

#include  <stdio.h>    /* Standard input/output definitions */
#include  <unistd.h>
#include  <sys/ioctl.h>
#include  <sys/types.h>
#include  <sys/time.h>
#include  <sys/select.h>
#include  <math.h>
#include "../include/swarmserial.h"
#include "../include/swarmdefines.h"
#include "../include/swarmspuutils.h"

// Static vars for heading Motor Control

static double targetDistance;
static double last_pos_error;
static double dead_band;
static double Kp;
static double Ki;
static double Kd;
static double minDrive;
static double maxDrive;
static double maxAccel;
static double crntPWM;
static double iSum = 0;
static double iLimit = 0;

static int headingDebugOutput;	// flag for outputing PID tuning info

double limit( double *v, double minVal, double maxVal);

// ------------------------------------------------------------------------

void headingInit(void)
{
	targetDistance = 0.0;		// start with heading centered
	Kp = 8.0;	// this will be divided by 10 for fractional gain values
	Ki = 0.0;
	Kd = 0.0;
	dead_band = 10.0;	// Set high to stop chatter, decrease for precision
	minDrive = 60.0;
	maxDrive = 200.0;
	maxAccel = 255.0;
	crntPWM = 0.0;
	iSum = 0.0;
	iLimit = 500.0;
	last_pos_error = 0.0;
	headingDebugOutput = 1;
}
	

// -----------------------------------------------------------------------

void headingSetTargetDistance(double v)
{
	targetDistance = v;
}

// -----------------------------------------------------------------------
// Set params for heading Servo PID controller via Command Line

void heading_set_dead_band(double db)
{
	dead_band = db;
}

void heading_set_iLimit(double db)
{
	iLimit = db;
}

void heading_set_Kp(double v)
{
	Kp = v;
}

void heading_set_Ki(double v)
{
	Ki = v;
}

void heading_set_Kd(double v)
{
	Kd = v;
}

void heading_set_min(double v)
{
	minDrive = v;
}

void heading_set_max(double v)
{
	maxDrive = v;
}

void heading_set_accel(double v)
{
	maxAccel = v;
}

void headingSetDebugOutput(int v) {
  	headingDebugOutput = v;
}

// -----------------------------------------------------------------------

double headingServoTask(double distance, int debugFileDescriptor)
{
  double headingError;
  double steeringAngle;
  double p_term, d_term, i_term;
  char debugBuffer[MAX_BUFF_SZ + 1];

  // heading error calculation
  headingError = targetDistance - distance;

  // dead band	
  if (fabs(headingError) < dead_band) {	// we are where we want to be - no motion required
    return(0.00);
  }
  
  // calculate p term
  p_term = headingError * Kp;

  // calculate d term
  d_term = Kd * (last_pos_error - headingError);
  last_pos_error = headingError;
  

  // sum to integrate heading error and limit runaway
  iSum += headingError;
  limit(&iSum,-iLimit, iLimit);

  i_term = Ki*iSum;
  i_term = i_term / 4.0; // divide by 4 to scale
  steeringAngle = (p_term + d_term + i_term);	
  steeringAngle = steeringAngle / 8.0; // divide by 8 to scale

  // If Debug Log is turned on, output PID data until position is stable
  if ((headingDebugOutput == 1) && debugFileDescriptor != 0) {
    sprintf(debugBuffer, "STEER PID targ curr: %g %g Drive: %g \n\r STEER P,I,D: %g, %g, %g Integrator: %g \n\r", 	     
	targetDistance, distance, steeringAngle, p_term, i_term, d_term, iSum );
		
    writeCharsToSerialPort(debugFileDescriptor, debugBuffer, strlen(debugBuffer));  	

  }
  
  return(steeringAngle);	

}

// make sure minVal < maxVal (!)
double limit( double *v, double minVal, double maxVal)
{
	if (*v < minVal) *v = minVal;
	if (*v > maxVal) *v = maxVal;
	return *v;
}


// ------------------------------------------------------------

void Get_heading_Status(void){
  /*	
  putstr("SteerTarget: ");
  putS16(target_Pos);
  putstr("\n SteerActual: ");
  putS16(current_Pos);
  putstr("\n PWM ");
  putS16(crntPWM);
  putstr("\n int ");
  putS16(iSum);
  putstr("\n ");
  */
}


void heading_dump_data(void)
{
	/*
	putstr("Steer: target current");
	putS16(target_Pos);
	putS16(current_Pos);
	putstr(" Gain: Kp Kd Ki ");
	putS16(Kp);
	putS16(Kd);
	putS16(Ki);
	putstr("\n mindr maxdr maxa dead: ");
	putS16(minDrive);
	putS16(maxDrive);
	putS16(maxAccel);
	putS16(dead_band);
	putstr("\n\r");
 	*/
}

