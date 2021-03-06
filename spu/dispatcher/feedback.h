// ---------------------------------------------------------------------
//
//	feedback.h
//      SWARM Orb
//      feedback PID loop  for SWARM Orb http://www.orbswarm.com
//
//	Reapplied to heading control by Michael michaelprados.com
//	Refactored by Jonathan (Head Rotor at rotorbrain.com)
//      Original Version by Petey the Programmer  Date: 30-April-2007
// -----------------------------------------------------------------------


#include "swarmdefines.h"


void swarmFeedbackInit(void);

void swarmFeedbackProcess(struct swarmStateEstimate * stateEstimate,
		struct swarmCoord * carrot, struct swarmFeedback * feedback,  char * buffer  );

int potFeedForward ( struct swarmStateEstimate * stateEstimate, struct swarmCoord * carrot );

int propFeedForward( struct swarmStateEstimate * stateEstimate, struct swarmCoord * carrot );

void peekLateralPID( char * buffer );

void peekVelocityPID( char * buffer );

void pokeLateralPID( char * buffer );

void pokeVelocityPID( char * buffer );

