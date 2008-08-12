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
#include "pathfollow.h"


// Static vars for control
static struct swarmPID lateralPID;
static struct swarmPID velocityPID;

double limit( double *v, double minVal, double maxVal);
double processPID( struct swarmPID * PID );
void debugPID( struct swarmPID * PID, FILE * fD);
void debugPIDString( struct swarmPID * PID, char * buffer);

// ------------------------------------------------------------------------

void swarmFeedbackInit(void)
{
	lateralPID.Kp 		= 0.1;
	lateralPID.Ki 		= 0.001;
	lateralPID.Kd 		= 0.0;
	lateralPID.deadBand 	= 0.0;	// Set high to stop chatter, decrease for precision
	lateralPID.minDrive 	= 0.0;
	lateralPID.maxDrive 	= 0.0;
	lateralPID.iSum 	= 0.0;
	lateralPID.iLimit 	= 1.0;
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
		struct swarmCoord * carrot, struct swarmFeedback * feedback, char * buffer )
{
   lateralPID.error  =  headingError(stateEstimate, carrot);
   // velocityPID.error = -xError * sin( target->nmea_course ) + yError * cos( target->nmea_course );

   feedback->deltaDes     = processPID( &lateralPID );
   // swarmFeedback->vDes = processPID( &velocityPID ) + target->speed;

   // debugPID( &lateralPID, stdout );
   debugPIDString( &lateralPID, buffer );

}


// -----------------------------------------------------------------------





// -----------------------------------------------------------------------

double processPID( struct swarmPID * PID )
{
  double output;
  // char debugBuffer[MAX_BUFF_SZ + 1];

  // dead band
  if (fabs(PID->error) < PID->deadBand) {	// we are where we want to be - no motion required
    PID->lastError = PID->error;
    return(0.00);
  }

  // calculate p term
  PID->pTerm = PID->error * PID->Kp;

  // calculate d term
  PID->dTerm = PID->Kd * (PID->lastError - PID->error);
  PID->lastError = PID->error;

  // sum to integrate error and limit runaway
  PID->iSum += PID->error;
  limit(&(PID->iSum),-PID->iLimit, PID->iLimit);

  PID->iTerm = PID->Ki * PID->iSum;
  output = (PID->pTerm + PID->dTerm + PID->iTerm);

  return(output);

}


// make sure minVal < maxVal (!)
double limit( double *v, double minVal, double maxVal)
{
	if (*v < minVal) *v = minVal;
	if (*v > maxVal) *v = maxVal;
	return *v;
}

void debugPID( struct swarmPID * PID, FILE * fD)
{
	fprintf( fD, "\n error:%f pTerm:%f iTerm:%f dTerm:%f iSum:%f",PID->error, PID->pTerm, PID->iTerm, PID->dTerm, PID->iSum);
}

void debugPIDString( struct swarmPID * PID, char * buffer)
{
	sprintf( buffer, "\n error:%f pTerm:%f iTerm:%f dTerm:%f iSum:%f",PID->error, PID->pTerm, PID->iTerm, PID->dTerm, PID->iSum);
}


