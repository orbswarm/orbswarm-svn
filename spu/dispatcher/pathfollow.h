// ---------------------------------------------------------------------
// 
//	pathfollow.h
//      SWARM 
//      path following for SWARM http://www.orbswarm.com
//
//	MAP, 7/23/08
// -----------------------------------------------------------------------

#include "swarmdefines.h"

struct	swarmCoord
{
  // an x,y coordinate and some related attributes

  double x;		// x position (meters East)	    		
  double y;		// y position (meters North)
  double psi;		// heading (radians counterclockwise from East)
  double v;		// velocity 	
};

struct swarmCircle
{
  struct swarmCoord center;	// location of circle center
  struct swarmCoord current;	// current position along circle circumference
  double radius;		
  double direction;		// right hand rule: +1 for counterclockwise, -1 for clockwise
  double carrotDistance;	// length to place carrot in front of current
};

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




