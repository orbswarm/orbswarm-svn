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

  /* steering motor data */
  double x;	    
  double y;
  double psi;
  double v;
};

struct swarmCircle
{
  struct swarmCoord center;
  struct swarmCoord current;
  double radius;
  double direction;
  double carrotDistance;
};

double headingError(struct swarmStateEstimate * stateEstimate, struct swarmCoord * carrot);

void circleInit( struct swarmStateEstimate * stateEstimate, struct swarmCircle * circle );

void circlePath( struct swarmCircle * circle, struct swarmStateEstimate * stateEstimate, 
		 struct swarmCoord * carrot );




