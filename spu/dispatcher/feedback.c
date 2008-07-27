// ---------------------------------------------------------------------
//
//	feedback.c
//      SWARM Orb
//      feedback PID loop  for SWARM Orb http://www.orbswarm.com
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
#include "swarmdefines.h"


// Static vars for control
static struct swarmPID lateralPID;
static struct swarmPID velocityPID;

double limit( double *v, double minVal, double maxVal);

// ------------------------------------------------------------------------

void swarmFeedbackInit(void)
{
	lateralPID.Kp 		= 8.0;
	lateralPID.Ki 		= 0.0;
	lateralPID.Kd 		= 0.0;
	lateralPID.deadBand 	= 10.0;	// Set high to stop chatter, decrease for precision
	lateralPID.minDrive 	= 60.0;
	lateralPID.maxDrive 	= 200.0;
	lateralPID.iSum 	= 0.0;
	lateralPID.iLimit 	= 500.0;
	lateralPID.lastError 	= 0.0;

	velocityPID.Kp 		= 8.0;
	velocityPID.Ki 		= 0.0;
	velocityPID.Kd 		= 0.0;
	velocityPID.deadBand 	= 10.0;	// Set high to stop chatter, decrease for precision
	velocityPID.minDrive 	= 60.0;
	velocityPID.maxDrive 	= 200.0;
	velocityPID.iSum 	= 0.0;
	velocityPID.iLimit 	= 500.0;
	velocityPID.lastError 	= 0.0;

}

void swarmFeedbackProcess(struct swarmStateEstimate * stateEstimate,
		struct swarmGPSData * target, struct swarmFeedback * feedback )
{
   double xError;
   double yError;

   xError = stateEstimate->x - target->metFromMshipNorth;
   yError = stateEstimate->y - target->metFromMshipEast;

   lateralPID.error  =  xError * cos( target->nmea_course ) + yError * sin( target->nmea_course );
   velocityPID.error = -xError * sin( target->nmea_course ) + yError * cos( target->nmea_course );

   swarmFeedback->deltaDes     = processPID( &lateralPID );
   swarmFeedback->vDes = processPID( &velocityPID ) + target->speed;

}


// -----------------------------------------------------------------------





// -----------------------------------------------------------------------

double processPID( struct swarmPID * PID )
{
  double output;
  double p_term, d_term, i_term;
  char debugBuffer[MAX_BUFF_SZ + 1];

  // dead band
  if (fabs(PID->error) < PID->deadBand) {	// we are where we want to be - no motion required
    PID->lastError = PID->error;
    return(0.00);
  }

  // calculate p term
  p_term = PID->error * PID->Kp;

  // calculate d term
  d_term = PID->Kd * (PID->lastError - PID->error);
  PID->lastError = PID->error;

  // sum to integrate error and limit runaway
  PID->iSum += PID->error;
  limit(&(PID->iSum),-iLimit, iLimit);

  i_term = PID->Ki * PID->iSum;
  output = (p_term + d_term + i_term);

  // If Debug Log is turned on, output PID data until position is stable
  if ((PID->debugOutput == 1) && debugFileDescriptor != 0) {
    sprintf(debugBuffer, "PID error: %g %g Drive: %g \n\r STEER P,I,D: %g, %g, %g Integrator: %g \n\r",
	PID->error, output, p_term, i_term, d_term, PID->iSum );

    writeCharsToSerialPort(debugFileDescriptor, debugBuffer, strlen(debugBuffer));

  }

  return(output);

}

// make sure minVal < maxVal (!)
double limit( double *v, double minVal, double maxVal)
{
	if (*v < minVal) *v = minVal;
	if (*v > maxVal) *v = maxVal;
	return *v;
}




