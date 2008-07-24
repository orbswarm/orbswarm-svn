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
  errorValue -= 2*PI*rint(error/(2*PI));
  return(errorValue);
}

// ------------------------------------------------------------------------
// circleInit
// 
// Initializes a circle by finding the circle center based on current position
// and heading.
//
// ------------------------------------------------------------------------
void circleInit( struct swarmStateEstimate * stateEstimate, struct circle * thisCircle )
{
  thisCircle->center.psi = -(stateEstimate->psi + thisCircle->direction * (PI/2));
  thisCircle->center.x   = stateEstimate.x - thisCircle->radius * cos(thisCircle->center.psi);
  thisCircle->center.y   = stateEstimate.y - thisCircle->radius * sin(thisCircle->center.psi);
}


// ------------------------------------------------------------------------
// circlePath
// 
// Finds current location on the circular path,
// and figures out the carrot position
// ------------------------------------------------------------------------
void circlePath( struct circle * thisCircle, struct swarmStateEstimate * stateEstimate, 
		 struct swarmCoord * carrot )
{ 
  thisCircle->center.psi = atan2((stateEstimate->x - center->x), (stateEstimate->y - center->y));

  thisCircle->current.x   = thisCircle->radius * cos( thisCircle->center.psi ) + thisCircle->center.x;
  thisCircle->current.y   = thisCircle->radius * sin( thisCircle->center.psi ) + thisCircle->center.y;
  thisCircle->current.psi = thisCircle->center.psi + thisCircle->direction * (PI/2);
  
  carrot->x = thisCircle->current.x + thisCircle->carrotDistance * cos(thisCircle->current.psi);
  carrot->y = thisCircle->current.y + thisCircle->carrotDistance * sin(thisCircle->current.psi);
}


  

