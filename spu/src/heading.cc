// ---------------------------------------------------------------------
// 
//	heading.c 
//      SWARM Orb 
//      heading feedback PID loop  for SWARM Orb http://www.orbswarm.com
//
//	Reapplied to heading control by Michael michaelprados.com
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
static double lastHeadingError;
static double deadBand;
static double Kp;
static double Ki;
static double Kd;
static double minDrive;
static double maxDrive;
static double maxAccel;
static double iSum = 0.0;
static double iLimit = 0.0;

static int headingDebugOutput;	// flag for outputing PID tuning info

double limit( double *v, double minVal, double maxVal);

// ------------------------------------------------------------------------

void headingInit(void)
{
	targetDistance = 0.0;		// start with heading centered
	Kp = 8.0;	// this will be divided by 10 for fractional gain values
	Ki = 0.0;
	Kd = 0.0;
	deadBand = 10.0;	// Set high to stop chatter, decrease for precision
	minDrive = 60.0;
	maxDrive = 200.0;
	maxAccel = 255.0;
	iSum = 0.0;
	iLimit = 500.0;
	lastHeadingError = 0.0;
	headingDebugOutput = 1;
}
	

// -----------------------------------------------------------------------

void headingSetTargetDistance(double v)
{
	targetDistance = v;
}

// -----------------------------------------------------------------------
// Set params for heading Servo PID controller via Command Line

void headingSetDeadBand(double v)
{
	deadBand = v;
}

void headingSetiLimit(double v)
{
	iLimit = v;
}

void headingSetKp(double v)
{
	Kp = v;
}

void headingSetKi(double v)
{
	Ki = v;
}

void headingSetKd(double v)
{
	Kd = v;
}

void headingSetMin(double v)
{
	minDrive = v;
}

void headingSetMax(double v)
{
	maxDrive = v;
}

void headingSetAccel(double v)
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
  if (fabs(headingError) < deadBand) {	// we are where we want to be - no motion required
    lastHeadingError = headingError;
    return(0.00);
  }
  
  // calculate p term
  p_term = headingError * Kp;

  // calculate d term
  d_term = Kd * (lastHeadingError - headingError);
  lastHeadingError = headingError;
  

  // sum to integrate heading error and limit runaway
  iSum += headingError;
  limit(&iSum,-iLimit, iLimit);

  i_term = Ki*iSum / 4.0;
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




