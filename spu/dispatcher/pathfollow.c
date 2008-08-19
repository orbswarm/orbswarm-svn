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

#define PATHCARROTDISTANCE  3.0


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

  errorValue = atan2( (carrot->y - stateEstimate->y),(carrot->x - stateEstimate->x) ); // errorValue = desired heading
  errorValue = stateEstimate->psi - errorValue; // errorValue = heading - desired heading
  errorValue -= 2*PI*rint(errorValue/(2*PI)); 	// unwrap phase of errorValue
  fprintf(stdout, "\nerrorValue=%f", errorValue );
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
  circle->center.psi = stateEstimate->psi + circle->direction * (PI/2) + PI;
  circle->center.x   = stateEstimate->x - circle->radius * cos(circle->center.psi);
  circle->center.y   = stateEstimate->y - circle->radius * sin(circle->center.psi);
  circle->zeroTime	 = stateEstimate->time;
  circle->zeroPsi    = circle->center.psi;
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
  circle->center.psi = atan2( (stateEstimate->y-circle->center.y),(stateEstimate->x - circle->center.x) );

  circle->current.x   = circle->radius * cos( circle->center.psi ) + circle->center.x;
  circle->current.y   = circle->radius * sin( circle->center.psi ) + circle->center.y;
  circle->current.psi = circle->center.psi + circle->direction * (PI/2);

  carrot->x = circle->current.x + circle->carrotDistance * cos(circle->current.psi);
  carrot->y = circle->current.y + circle->carrotDistance * sin(circle->current.psi);
}

// ------------------------------------------------------------------------
// circleSynchro
//
// Finds current location on the circular path,
// and figures out the carrot position
// ------------------------------------------------------------------------
void circleSynchro( struct swarmCircle * circle, struct swarmStateEstimate * stateEstimate,
		 struct swarmCoord * carrot )
{
  circle->center.psi = circle->zeroPsi + circle->rate * (stateEstimate->time - circle->zeroTime);

  circle->current.x   = circle->radius * cos( circle->center.psi ) + circle->center.x;
  circle->current.y   = circle->radius * sin( circle->center.psi ) + circle->center.y;
  circle->current.psi = circle->center.psi + circle->direction * (PI/2);

  carrot->x = circle->current.x + circle->carrotDistance * cos(circle->current.psi);
  carrot->y = circle->current.y + circle->carrotDistance * sin(circle->current.psi);

  carrot->phase = phaseFromCoord( stateEstimate, &(circle->current) );
}

// ------------------------------------------------------------------------
// figEightInit
//
// Initializes figure 8 path.
// ------------------------------------------------------------------------
void figEightInit( struct swarmStateEstimate * stateEstimate, struct swarmFigEight * figEight )
{
	struct swarmStateEstimate circleState;

	figEight->point0.x 	 = stateEstimate->x;
	figEight->point0.y 	 = stateEstimate->y;
	figEight->point0.psi = stateEstimate->psi;

	figEight->point1.x = figEight->point0.x + figEight->radius * cos(figEight->point0.psi);
	figEight->point1.y = figEight->point0.y + figEight->radius * sin(figEight->point0.psi);

	figEight->point2.x = figEight->point0.x + figEight->radius * cos(figEight->point0.psi - PI/2.0 );
	figEight->point2.y = figEight->point0.y + figEight->radius * sin(figEight->point0.psi - PI/2.0 );

	figEight->point3.x = figEight->point0.x + figEight->radius * cos(figEight->point0.psi + PI/2.0 );
	figEight->point3.y = figEight->point0.y + figEight->radius * sin(figEight->point0.psi + PI/2.0 );

	figEight->point4.x = figEight->point0.x + figEight->radius * cos(figEight->point0.psi + PI );
	figEight->point4.y = figEight->point0.y + figEight->radius * sin(figEight->point0.psi + PI );

	circleState.x  	= figEight->point1.x;
	circleState.y   = figEight->point1.y;
	circleState.psi = figEight->point0.psi;

	figEight->circle1.radius    = figEight->radius;
	figEight->circle1.direction = -1.0;
	figEight->circle1.carrotDistance = figEight->carrotDistance;

	circleInit(&circleState, &(figEight->circle1));

	circleState.x  	= figEight->point3.x;
	circleState.y   = figEight->point3.y;
	circleState.psi = figEight->point0.psi + PI;

	figEight->circle2.radius    = figEight->radius;
	figEight->circle2.direction = 1.0;
	figEight->circle2.carrotDistance = figEight->carrotDistance;

	circleInit(&circleState, &(figEight->circle2));

	figEight->mode = 0;

}

// ------------------------------------------------------------------------
// figEightPath
//
// Generates figure 8 path.
// ------------------------------------------------------------------------
void figEightPath( struct swarmFigEight * figEight, struct swarmStateEstimate * stateEstimate,
		 struct swarmCoord * carrot )
{
	enum {FIGEIGHT_STRAIGHT1, FIGEIGHT_CIRCLE1, FIGEIGHT_STRAIGHT2, FIGEIGHT_CIRCLE2};

	switch (figEight->mode)
	{
	case FIGEIGHT_STRAIGHT1:
			carrot->x = figEight->point1.x;
			carrot->y = figEight->point1.y;
			if (distanceToCoord( stateEstimate, &(figEight->point1)) < 3.0)
				figEight->mode = FIGEIGHT_CIRCLE1;
		break;

	case FIGEIGHT_CIRCLE1:
			circlePath( &(figEight->circle1), stateEstimate, carrot );
			if (distanceToCoord( stateEstimate, &(figEight->point2)) < 3.0)
				figEight->mode = FIGEIGHT_STRAIGHT2;
		break;
	case FIGEIGHT_STRAIGHT2:
			carrot->x = figEight->point3.x;
			carrot->y = figEight->point3.y;
			if (distanceToCoord( stateEstimate, &(figEight->point3)) < 3.0)
				figEight->mode = FIGEIGHT_CIRCLE2;
		break;

	case FIGEIGHT_CIRCLE2:
			circlePath( &(figEight->circle2), stateEstimate, carrot );
			if (distanceToCoord( stateEstimate, &(figEight->point4)) < 3.0)
				figEight->mode = FIGEIGHT_STRAIGHT1;
		break;

	}

}

// ------------------------------------------------------------------------
// pathFollow
//
// Takes a trajectory and generates a carrot to follow it.
// ------------------------------------------------------------------------
void pathFollow( struct swarmCoord * trajectory, struct swarmStateEstimate * stateEstimate,
		 struct swarmCoord * carrot )
{

	carrot->x  		= trajectory->x + PATHCARROTDISTANCE * cos(trajectory->psi);
	carrot->y  		= trajectory->y + PATHCARROTDISTANCE * sin(trajectory->psi);

	carrot->phase 	= phaseFromCoord( stateEstimate, trajectory );

	carrot->v 		= trajectory->v;
	carrot->psidot  = trajectory->psidot;
}



// ------------------------------------------------------------------------
// distanceToCoord
//
// returns the distance to a coordinate from current position
// ------------------------------------------------------------------------
double distanceToCoord( struct swarmStateEstimate * stateEstimate, struct swarmCoord * thisCoord )
{
	double distance;
	distance = (stateEstimate->x - thisCoord->x) * (stateEstimate->x - thisCoord->x);
	distance += (stateEstimate->y - thisCoord->y) * (stateEstimate->y - thisCoord->y);
	distance = sqrt( distance );
	return(distance);
}

// ------------------------------------------------------------------------
// phaseFromCoord
//
// returns the phase (as a distance) from a coordinate to current position,
// in the coordinate's heading direction
// ------------------------------------------------------------------------
double phaseFromCoord( struct swarmStateEstimate * stateEstimate, struct swarmCoord * thisCoord )
{
	double headingToOrb;
	double distance;
	double phase;

	distance = distanceToCoord( stateEstimate, thisCoord );

	headingToOrb = atan2( stateEstimate->y - thisCoord->y, stateEstimate->x - thisCoord->x );

	phase = distance * cos( headingToOrb - thisCoord->psi );

	return( phase );
}




