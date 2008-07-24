// ---------------------------------------------------------------------
// 
//	pathfollow.h
//      SWARM 
//      path following for SWARM http://www.orbswarm.com
//
//	MAP, 7/23/08
// -----------------------------------------------------------------------

struct	swarmCoord
{

  /* steering motor data */
  double x;	    
  double y;
  double psi;
  double v;
}

struct swarmCircle
{
  struct swarmCoord circleCenter;
  struct swarmCoord circleCurrent;
  double radius;
  double direction;
  double carrotDistance;
}

double headingError(struct swarmStateEstimate * stateEstimate, struct swarmCoord * carrot);

void circleInit( struct swarmStateEstimate * stateEstimate, struct circle * thisCircle );

void circlePath( struct circle * thisCircle, struct swarmStateEstimate * stateEstimate, 
		 struct swarmCoord * carrot );




