// ---------------------------------------------------------------------
//
//	pathfollow.h
//      SWARM
//      path following for SWARM http://www.orbswarm.com
//
//	MAP, 7/23/08
// -----------------------------------------------------------------------

#include "swarmdefines.h"

// ------------------------------------------------------------------------
// headingError
//
// given current state and desired carrot coordinates,
// returns heading error to carrot.
// ------------------------------------------------------------------------
double headingError(struct swarmStateEstimate * stateEstimate, struct swarmCoord * carrot);

// ------------------------------------------------------------------------
// circleInit
//
// Initializes a circle by finding the circle center based on current position
// and heading.
//
// ------------------------------------------------------------------------
void circleInit( struct swarmStateEstimate * stateEstimate, struct swarmCircle * circle );

// ------------------------------------------------------------------------
// circlePath
//
// Finds current location on the circular path,
// and figures out the carrot position
// ------------------------------------------------------------------------
void circlePath( struct swarmCircle * circle, struct swarmStateEstimate * stateEstimate,
		 struct swarmCoord * carrot );

// ------------------------------------------------------------------------
// figEightInit
//
// Initializes a figure 8
//
// ------------------------------------------------------------------------
void figEightInit( struct swarmStateEstimate * stateEstimate, struct swarmFigEight * figEight );

// ------------------------------------------------------------------------
// figEightPath
//
// Finds current location on the figure 8 path,
// and figures out the carrot position
// ------------------------------------------------------------------------
void figEightPath( struct swarmFigEight * figEight, struct swarmStateEstimate * stateEstimate,
		 struct swarmCoord * carrot );

double distanceToCoord( struct swarmStateEstimate * stateEstimate, struct swarmCoord * thisCoord );




