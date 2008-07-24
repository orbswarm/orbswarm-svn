// ---------------------------------------------------------------------
// 
//	pathfollow.c
//      SWARM 
//      path following for SWARM Orb http://www.orbswarm.com
//
//	MAP, 7/23/08
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

#define PI 3.14159265359

// ------------------------------------------------------------------------


// ------------------------------------------------------------------------
// headingError
//
// given current state and desired carrot coordinates,
// returns heading error to carrot.
// ------------------------------------------------------------------------
double headingError(struct swarmStateEstimate * stateEstimate, struct swarmCoord * carrot)
{
  double errorValue;

  errorValue = atan2( (carrot->x - stateEstimate->x),(carrot->y - stateEstimate->y) );
  errorValue -= 2*PI*rint(errorValue/(2*PI));
  return(errorValue);
}

// ------------------------------------------------------------------------
// circleInit
// 
// Initializes a circle by finding the circle center based on current position
// and heading.
//
// ------------------------------------------------------------------------
void circleInit( struct swarmStateEstimate * stateEstimate, struct swarmCircle * circle )
{
  circle->center.psi = -(stateEstimate->psi + circle->direction * (PI/2));
  circle->center.x   = stateEstimate->x - circle->radius * cos(circle->center.psi);
  circle->center.y   = stateEstimate->y - circle->radius * sin(circle->center.psi);
}


// ------------------------------------------------------------------------
// circlePath
// 
// Finds current location on the circular path,
// and figures out the carrot position
// ------------------------------------------------------------------------
void circlePath( struct swarmCircle * circle, struct swarmStateEstimate * stateEstimate, 
		 struct swarmCoord * carrot )
{ 
  circle->center.psi = atan2((stateEstimate->x - circle->center.x), (stateEstimate->y - circle->center.y));

  circle->current.x   = circle->radius * cos( circle->center.psi ) + circle->center.x;
  circle->current.y   = circle->radius * sin( circle->center.psi ) + circle->center.y;
  circle->current.psi = circle->center.psi + circle->direction * (PI/2);
  
  carrot->x = circle->current.x + circle->carrotDistance * cos(circle->current.psi);
  carrot->y = circle->current.y + circle->carrotDistance * sin(circle->current.psi);
}


  

