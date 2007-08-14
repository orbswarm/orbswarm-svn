// ---------------------------------------------------------------------
// 
//	swarmfeedback.h
//      SWARM Orb 
//      feedback PID loop  for SWARM Orb http://www.orbswarm.com
//
//	Reapplied to heading control by Michael michaelprados.com
//	Refactored by Jonathan (Head Rotor at rotorbrain.com)
//      Original Version by Petey the Programmer  Date: 30-April-2007
// -----------------------------------------------------------------------


#include "../include/swarmdefines.h"


void swarmFeedbackInit(void);

swarmFeedbackProcess(struct swarmStateEstimate * stateEstimate, struct swarmGPSData * target, struct swarmFeedback * feedback );

