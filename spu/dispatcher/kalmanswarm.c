/*  kalmanswarm.c

    This file contains code segments for a Kalman Filter that characterize
    a particular system and measurement model.

    In this case, the system being characterized is spherical art robot.

    Rehashed for SWARM (http://www.orbswarm.com) by Michael Prados, 8/10/07
*/


#define ARBITRARY_SCALE        1

#define PHICENTER 0.02

#include <stdio.h>
#include <math.h>
#include <string.h>
#include "kalman.h"
#include "kalmanswarm.h"
#include "matmath.h"

extern int     debug;



/*  Temporary variables, declared statically to avoid lots of run-time
    memory allocation.      */

static uFloat  *measurementVec;        /* a measurement_size x 1 vector */
static uFloat  *stateVec;              /* a state_size x 1 vector */

void kalmanInitialBias( struct swarmGpsDataStruct * gpsData, struct swarmImuData * imuData,
						struct swarmStateEstimate * stateEstimate )
{

	stateEstimate->x 	= 0.05 * gpsData->UTMEasting  + 0.95 * stateEstimate->x;
	stateEstimate->y 	= 0.05 * gpsData->UTMNorthing + 0.95 * stateEstimate->y;

	stateEstimate->xab 	= 0.05 * imuData->si_accx + 0.95 * stateEstimate->xab;
	stateEstimate->yab 	= 0.05 * imuData->si_accy + 0.95 * stateEstimate->yab;
	stateEstimate->zab 	= 0.05 * (imuData->si_accz + GRAVITY) + 0.95 * stateEstimate->zab;

	stateEstimate->xrb 	= 0.05 * imuData->si_ratex + 0.95 * stateEstimate->xrb;
	stateEstimate->zrb 	= 0.05 * imuData->si_ratez + 0.95 * stateEstimate->zrb;

	stateEstimate->yawb 	= 0.05 * imuData->si_yawRate + 0.95 * stateEstimate->yawb;
}

int kalmanInit( struct swarmStateEstimate * stateEstimate )
{
   int      row, col; /* various iteration variables   */

   uFloat   **P;     /*  Estimate Covariance        (mxm)   */
   uFloat   *x;      /*  Starting state             (1xm)   */

   P = matrix( 1, STATE_SIZE, 1, STATE_SIZE );
   x = vector( 1, STATE_SIZE );

   measurementVec = vector( 1, MEAS_SIZE );
   stateVec = vector( 1, STATE_SIZE );

   for( row = 1; row <= STATE_SIZE; row++ )
     for( col = 1; col <= STATE_SIZE; col++ )
       P[ row ][ col ] = 0.0;

   x[ STATE_vdot ] 		= stateEstimate->vdot;
   x[ STATE_v ]    		= stateEstimate->v;
   x[ STATE_phidot ] 	= stateEstimate->phidot;
   x[ STATE_phi ] 		= stateEstimate->phi;
   x[ STATE_psi ] 		= stateEstimate->psi;
   x[ STATE_theta ] 	= stateEstimate->theta;
   x[ STATE_x ] 		= stateEstimate->x;
   x[ STATE_y ] 		= stateEstimate->y;
   x[ STATE_xab ] 		= stateEstimate->xab;
   x[ STATE_yab ] 		= stateEstimate->yab;
   x[ STATE_zab ]		= stateEstimate->zab;
   x[ STATE_xrb ]		= stateEstimate->xrb;
   x[ STATE_zrb ] 		= stateEstimate->zrb;
   x[ STATE_yawb ] 		= stateEstimate->yawb;


   extended_kalman_init( P, x );

   freeMatrix( P, 1, STATE_SIZE, 1, STATE_SIZE );
   freeVector( x, 1, STATE_SIZE );

   return SWARM_SUCCESS;
}


int kalmanProcess( struct swarmGpsDataStruct * gpsData, struct swarmImuData * imuData, struct swarmStateEstimate * stateEstimate)
{

   measurementVec[ MEAS_xa ] 	= imuData->si_accx;
   measurementVec[ MEAS_ya ] 	= imuData->si_accy;
   measurementVec[ MEAS_za ] 	= imuData->si_accz;
   measurementVec[ MEAS_xr ] 	= imuData->si_ratex;
   measurementVec[ MEAS_zr ] 	= imuData->si_ratez;
   measurementVec[ MEAS_yaw ] 	= imuData->si_yawRate;

   measurementVec[ MEAS_omega ] = imuData->omega;

   measurementVec[ MEAS_xg ]   	= gpsData->metFromMshipEast;
   measurementVec[ MEAS_yg ]   	= gpsData->metFromMshipNorth;

   measurementVec[ MEAS_vg ]   	= (uFloat)gpsData->speed;
   //correct for direction is v < 0
   if (stateVec[ STATE_v ] < 0)
     measurementVec[ MEAS_vg ] 	= - measurementVec[ MEAS_vg ];


   measurementVec[ MEAS_psig ] 	= (uFloat)gpsData->nmea_course;

   //correct for direction is v < 0
   if (stateVec[ STATE_v ] < 0)
     measurementVec[ MEAS_psig ] += PI;

   // psig is heading between -pi and pi. bring it to the same range as the current psi.
   measurementVec[ MEAS_psig ] += rint((stateVec[ STATE_psi ]-measurementVec[ MEAS_psig ])/(2*PI))*2*PI;

   // if encoder indicates low velocity, ignore GPS heading and velocity information
   if (imuData->omega < 1.0)
   {
	   measurementVec[ MEAS_psig ]  = stateVec[ STATE_psi ];
	   measurementVec[ MEAS_vg ]	= stateVec[ STATE_v ];
   }

   extended_kalman_step( measurementVec );

   stateVec = kalman_get_state();

   stateEstimate->vdot   = stateVec[ STATE_vdot ];
   stateEstimate->v 	 = stateVec[ STATE_v ];
   stateEstimate->phidot = stateVec[ STATE_phidot ];
   stateEstimate->phi 	 = stateVec[ STATE_phi ];
   stateEstimate->psi    = stateVec[ STATE_psi ];
   stateEstimate->theta  = stateVec[ STATE_theta ];
   stateEstimate->x      = stateVec[ STATE_x ];
   stateEstimate->y      = stateVec[ STATE_y ];
   stateEstimate->xab    = stateVec[ STATE_xab ];
   stateEstimate->yab    = stateVec[ STATE_yab ];
   stateEstimate->zab    = stateVec[ STATE_zab ];
   stateEstimate->xrb    = stateVec[ STATE_xrb ];
   stateEstimate->zrb    = stateVec[ STATE_zrb ];
   stateEstimate->yawb    = stateVec[ STATE_yawb ];

   return SWARM_SUCCESS;
}


void zeroStateEstimates( struct swarmStateEstimate * stateEstimate )
{
  stateEstimate->vdot 	= 0.0;	// acceleration
  stateEstimate->v 		= 0.0;  // velocity
  stateEstimate->phidot = 0.0;	// roll angle rate
  stateEstimate->phi 	= 0.0;  // roll angle
  stateEstimate->psi 	= -PI/5; 	// heading / yaw
  stateEstimate->theta 	= 0.0;  // frontward pitch
  stateEstimate->x 		= 554159.9;  // meters East
  stateEstimate->y 		= 4177058.3;	// meters North
  stateEstimate->xab	= 0.86;	// x accelerometer bias
  stateEstimate->yab	= 1.52;	// y accelerometer bias
  stateEstimate->zab	= -1.87;	// z accelerometer bias
  stateEstimate->xrb	= -0.26;	// x rate gyro bias
  stateEstimate->zrb	= 2.316;	// z rate gyro bias
  stateEstimate->yawb	= 0.0;	// z rate gyro bias
}

void initStateEstimates(struct swarmGpsDataStruct * gpsData, struct swarmImuData * imuData,
		struct swarmStateEstimate * stateEstimate )
{
	stateEstimate->x 	= gpsData->UTMEasting;
	stateEstimate->y 	= gpsData->UTMNorthing;

	stateEstimate->xab 	= imuData->si_accx;
	stateEstimate->yab 	= imuData->si_accy;
	stateEstimate->zab 	= (imuData->si_accz + GRAVITY);

	stateEstimate->xrb 	= imuData->si_ratex;
	stateEstimate->zrb 	= imuData->si_ratez;

	stateEstimate->yawb = imuData->si_yawRate;
}

/****************  System Model Manifestations   **************
*/


void systemF( uFloat *stateDot, uFloat *state )
{
	stateDot[ STATE_vdot ] 		= 0.0;
	stateDot[ STATE_v ] 		= state[ STATE_vdot ];
	stateDot[ STATE_phidot ] 	= -state[ STATE_phi ];
	stateDot[ STATE_phi ] 		= state[ STATE_phidot ]- PHICENTER * state[ STATE_phi ];
	stateDot[ STATE_theta ] 	= -state[ STATE_theta ] * 1.0;
	stateDot[ STATE_psi ] 		= -state[ STATE_v ] * tan( state[ STATE_phi ] ) / RADIUS;
	stateDot[ STATE_x ] 		= state[ STATE_v ] * cos( state[ STATE_psi ] );
	stateDot[ STATE_y ] 		= state[ STATE_v ] * sin( state[ STATE_psi ] );
	stateDot[ STATE_xab ] 		= 0.0;
	stateDot[ STATE_yab ] 		= 0.0;
	stateDot[ STATE_zab ] 		= 0.0;
	stateDot[ STATE_xrb ] 		= 0.0;
	stateDot[ STATE_zrb ] 		= 0.0;
	stateDot[ STATE_yawb ] 		= 0.0;
}



/*  systemJacobian
    This function generates the pseudo-transfer
    function, representing the first terms of a taylor expansion
    of the actual non-linear system transfer function.     */

void systemJacobian( uFloat *state, uFloat **jacobian )
{

#ifdef PRINT_DEBUG
  printf( "ekf: calculating system jacobian\n" );
#endif

	jacobian[ STATE_vdot ][ STATE_vdot ] 	= 0.0;
	jacobian[ STATE_vdot ][ STATE_v ] 	= 0.0;
	jacobian[ STATE_vdot ][ STATE_phidot ] 	= 0.0;
	jacobian[ STATE_vdot ][ STATE_phi ] 	= 0.0;
	jacobian[ STATE_vdot ][ STATE_theta ] 	= 0.0;
	jacobian[ STATE_vdot ][ STATE_psi ] 	= 0.0;
	jacobian[ STATE_vdot ][ STATE_x ] 	= 0.0;
	jacobian[ STATE_vdot ][ STATE_y ] 	= 0.0;
	jacobian[ STATE_vdot ][ STATE_xab ] 	= 0.0;
	jacobian[ STATE_vdot ][ STATE_yab ] 	= 0.0;
	jacobian[ STATE_vdot ][ STATE_zab ] 	= 0.0;
	jacobian[ STATE_vdot ][ STATE_xrb ] 	= 0.0;
	jacobian[ STATE_vdot ][ STATE_zrb ] 	= 0.0;
	jacobian[ STATE_vdot ][ STATE_yawb ] 	= 0.0;

	jacobian[ STATE_v ][ STATE_vdot ] 	= 1.0;
	jacobian[ STATE_v ][ STATE_v ] 		= 0.0;
	jacobian[ STATE_v ][ STATE_phidot ] 	= 0.0;
	jacobian[ STATE_v ][ STATE_phi ] 	= 0.0;
	jacobian[ STATE_v ][ STATE_theta ] 	= 0.0;
	jacobian[ STATE_v ][ STATE_psi ] 	= 0.0;
	jacobian[ STATE_v ][ STATE_x ] 		= 0.0;
	jacobian[ STATE_v ][ STATE_y ] 		= 0.0;
	jacobian[ STATE_v ][ STATE_xab ] 	= 0.0;
	jacobian[ STATE_v ][ STATE_yab ] 	= 0.0;
	jacobian[ STATE_v ][ STATE_zab ] 	= 0.0;
	jacobian[ STATE_v ][ STATE_xrb ] 	= 0.0;
	jacobian[ STATE_v ][ STATE_zrb ] 	= 0.0;
	jacobian[ STATE_v ][ STATE_yawb ] 	= 0.0;

	jacobian[ STATE_phidot ][ STATE_vdot ] 	= 0.0;
	jacobian[ STATE_phidot ][ STATE_v ] 	= 0.0;
	jacobian[ STATE_phidot ][ STATE_phidot ]= 0.0;
	jacobian[ STATE_phidot ][ STATE_phi ] 	= 0.0;
	jacobian[ STATE_phidot ][ STATE_theta ] = 0.0;
	jacobian[ STATE_phidot ][ STATE_psi ] 	= 0.0;
	jacobian[ STATE_phidot ][ STATE_x ] 	= 0.0;
	jacobian[ STATE_phidot ][ STATE_y ] 	= 0.0;
	jacobian[ STATE_phidot ][ STATE_xab ] 	= 0.0;
	jacobian[ STATE_phidot ][ STATE_yab ] 	= 0.0;
	jacobian[ STATE_phidot ][ STATE_zab ] 	= 0.0;
	jacobian[ STATE_phidot ][ STATE_xrb ] 	= 0.0;
	jacobian[ STATE_phidot ][ STATE_zrb ] 	= 0.0;
	jacobian[ STATE_phidot ][ STATE_yawb ] 	= 0.0;

	jacobian[ STATE_phi ][ STATE_vdot ] 	= 0.0;
	jacobian[ STATE_phi ][ STATE_v ] 	= 0.0;
	jacobian[ STATE_phi ][ STATE_phidot ] 	= 1.0;
	jacobian[ STATE_phi ][ STATE_phi ] 	= - PHICENTER;
	jacobian[ STATE_phi ][ STATE_theta ] 	= 0.0;
	jacobian[ STATE_phi ][ STATE_psi ] 	= 0.0;
	jacobian[ STATE_phi ][ STATE_x ] 	= 0.0;
	jacobian[ STATE_phi ][ STATE_y ] 	= 0.0;
	jacobian[ STATE_phi ][ STATE_xab ] 	= 0.0;
	jacobian[ STATE_phi ][ STATE_yab ] 	= 0.0;
	jacobian[ STATE_phi ][ STATE_zab ] 	= 0.0;
	jacobian[ STATE_phi ][ STATE_xrb ] 	= 0.0;
	jacobian[ STATE_phi ][ STATE_zrb ] 	= 0.0;
	jacobian[ STATE_phi ][ STATE_yawb ] 	= 0.0;

	jacobian[ STATE_theta ][ STATE_vdot ] 	= 0.0;
	jacobian[ STATE_theta ][ STATE_v ] 	= 0.0;
	jacobian[ STATE_theta ][ STATE_phidot ] = 0.0;
	jacobian[ STATE_theta ][ STATE_phi ] 	= 0.0;
	jacobian[ STATE_theta ][ STATE_theta ] 	= -1.0;
	jacobian[ STATE_theta ][ STATE_psi ] 	= 0.0;
	jacobian[ STATE_theta ][ STATE_x ] 	= 0.0;
	jacobian[ STATE_theta ][ STATE_y ] 	= 0.0;
	jacobian[ STATE_theta ][ STATE_xab ] 	= 0.0;
	jacobian[ STATE_theta ][ STATE_yab ] 	= 0.0;
	jacobian[ STATE_theta ][ STATE_zab ] 	= 0.0;
	jacobian[ STATE_theta ][ STATE_xrb ] 	= 0.0;
	jacobian[ STATE_theta ][ STATE_zrb ] 	= 0.0;
	jacobian[ STATE_theta ][ STATE_yawb ] 	= 0.0;

	jacobian[ STATE_psi ][ STATE_vdot ] 	= 0.0;
	jacobian[ STATE_psi ][ STATE_v ] 	= -tan( state[ STATE_phi ] ) / RADIUS;
	jacobian[ STATE_psi ][ STATE_phidot ] 	= 0.0;
	jacobian[ STATE_psi ][ STATE_phi ] 	= -state[ STATE_v ] / (cos( state[ STATE_phi ] )
							* cos( state[ STATE_phi ] ) * RADIUS);
	jacobian[ STATE_psi ][ STATE_theta ] 	= 0.0;
	jacobian[ STATE_psi ][ STATE_psi ] 	= 0.0;
	jacobian[ STATE_psi ][ STATE_x ] 	= 0.0;
	jacobian[ STATE_psi ][ STATE_y ] 	= 0.0;
	jacobian[ STATE_psi ][ STATE_xab ] 	= 0.0;
	jacobian[ STATE_psi ][ STATE_yab ] 	= 0.0;
	jacobian[ STATE_psi ][ STATE_zab ] 	= 0.0;
	jacobian[ STATE_psi ][ STATE_xrb ] 	= 0.0;
	jacobian[ STATE_psi ][ STATE_zrb ] 	= 0.0;
	jacobian[ STATE_psi ][ STATE_yawb ] 	= 0.0;

	jacobian[ STATE_x ][ STATE_vdot ] 	= 0.0;
	jacobian[ STATE_x ][ STATE_v ] 		= cos( state[ STATE_psi ] );
	jacobian[ STATE_x ][ STATE_phidot ] 	= 0.0;
	jacobian[ STATE_x ][ STATE_phi ] 	= 0.0;
	jacobian[ STATE_x ][ STATE_theta ] 	= 0.0;
	jacobian[ STATE_x ][ STATE_psi ] 	= -state[ STATE_v ] * sin( state[ STATE_psi ] );
	jacobian[ STATE_x ][ STATE_x ] 		= 0.0;
	jacobian[ STATE_x ][ STATE_y ] 		= 0.0;
	jacobian[ STATE_x ][ STATE_xab ] 	= 0.0;
	jacobian[ STATE_x ][ STATE_yab ] 	= 0.0;
	jacobian[ STATE_x ][ STATE_zab ] 	= 0.0;
	jacobian[ STATE_x ][ STATE_xrb ] 	= 0.0;
	jacobian[ STATE_x ][ STATE_zrb ] 	= 0.0;
	jacobian[ STATE_x ][ STATE_yawb ] 	= 0.0;

	jacobian[ STATE_y ][ STATE_vdot ] 	= 0.0;
	jacobian[ STATE_y ][ STATE_v ] 		= sin( state[ STATE_psi ] );
	jacobian[ STATE_y ][ STATE_phidot ] 	= 0.0;
	jacobian[ STATE_y ][ STATE_phi ] 	= 0.0;
	jacobian[ STATE_y ][ STATE_theta ] 	= 0.0;
	jacobian[ STATE_y ][ STATE_psi ] 	= state[ STATE_v ] * cos( state[ STATE_psi ] );
	jacobian[ STATE_y ][ STATE_x ] 		= 0.0;
	jacobian[ STATE_y ][ STATE_y ] 		= 0.0;
	jacobian[ STATE_y ][ STATE_xab ] 	= 0.0;
	jacobian[ STATE_y ][ STATE_yab ] 	= 0.0;
	jacobian[ STATE_y ][ STATE_zab ] 	= 0.0;
	jacobian[ STATE_y ][ STATE_xrb ] 	= 0.0;
	jacobian[ STATE_y ][ STATE_zrb ] 	= 0.0;
	jacobian[ STATE_y ][ STATE_yawb ] 	= 0.0;

	jacobian[ STATE_xab ][ STATE_vdot ] 	= 0.0;
	jacobian[ STATE_xab ][ STATE_v ] 	= 0.0;
	jacobian[ STATE_xab ][ STATE_phidot ] 	= 0.0;
	jacobian[ STATE_xab ][ STATE_phi ] 	= 0.0;
	jacobian[ STATE_xab ][ STATE_theta ] 	= 0.0;
	jacobian[ STATE_xab ][ STATE_psi ] 	= 0.0;
	jacobian[ STATE_xab ][ STATE_x ] 	= 0.0;
	jacobian[ STATE_xab ][ STATE_y ] 	= 0.0;
	jacobian[ STATE_xab ][ STATE_xab ] 	= 0.0;
	jacobian[ STATE_xab ][ STATE_yab ] 	= 0.0;
	jacobian[ STATE_xab ][ STATE_zab ] 	= 0.0;
	jacobian[ STATE_xab ][ STATE_xrb ] 	= 0.0;
	jacobian[ STATE_xab ][ STATE_zrb ] 	= 0.0;
	jacobian[ STATE_xab ][ STATE_yawb ] 	= 0.0;

	jacobian[ STATE_yab ][ STATE_vdot ] 	= 0.0;
	jacobian[ STATE_yab ][ STATE_v ] 	= 0.0;
	jacobian[ STATE_yab ][ STATE_phidot ] 	= 0.0;
	jacobian[ STATE_yab ][ STATE_phi ] 	= 0.0;
	jacobian[ STATE_yab ][ STATE_theta ] 	= 0.0;
	jacobian[ STATE_yab ][ STATE_psi ] 	= 0.0;
	jacobian[ STATE_yab ][ STATE_x ] 	= 0.0;
	jacobian[ STATE_yab ][ STATE_y ] 	= 0.0;
	jacobian[ STATE_yab ][ STATE_xab ] 	= 0.0;
	jacobian[ STATE_yab ][ STATE_yab ] 	= 0.0;
	jacobian[ STATE_yab ][ STATE_zab ] 	= 0.0;
	jacobian[ STATE_yab ][ STATE_xrb ] 	= 0.0;
	jacobian[ STATE_yab ][ STATE_zrb ] 	= 0.0;
	jacobian[ STATE_yab ][ STATE_yawb ] 	= 0.0;

	jacobian[ STATE_zab ][ STATE_vdot ] 	= 0.0;
	jacobian[ STATE_zab ][ STATE_v ] 	= 0.0;
	jacobian[ STATE_zab ][ STATE_phidot ] 	= 0.0;
	jacobian[ STATE_zab ][ STATE_phi ] 	= 0.0;
	jacobian[ STATE_zab ][ STATE_theta ] 	= 0.0;
	jacobian[ STATE_zab ][ STATE_psi ] 	= 0.0;
	jacobian[ STATE_zab ][ STATE_x ] 	= 0.0;
	jacobian[ STATE_zab ][ STATE_y ] 	= 0.0;
	jacobian[ STATE_zab ][ STATE_xab ] 	= 0.0;
	jacobian[ STATE_zab ][ STATE_yab ] 	= 0.0;
	jacobian[ STATE_zab ][ STATE_zab ] 	= 0.0;
	jacobian[ STATE_zab ][ STATE_xrb ] 	= 0.0;
	jacobian[ STATE_zab ][ STATE_zrb ] 	= 0.0;
	jacobian[ STATE_zab ][ STATE_yawb ] 	= 0.0;

	jacobian[ STATE_xrb ][ STATE_vdot ] 	= 0.0;
	jacobian[ STATE_xrb ][ STATE_v ] 	= 0.0;
	jacobian[ STATE_xrb ][ STATE_phidot ] 	= 0.0;
	jacobian[ STATE_xrb ][ STATE_phi ] 	= 0.0;
	jacobian[ STATE_xrb ][ STATE_theta ] 	= 0.0;
	jacobian[ STATE_xrb ][ STATE_psi ] 	= 0.0;
	jacobian[ STATE_xrb ][ STATE_x ] 	= 0.0;
	jacobian[ STATE_xrb ][ STATE_y ] 	= 0.0;
	jacobian[ STATE_xrb ][ STATE_xab ] 	= 0.0;
	jacobian[ STATE_xrb ][ STATE_yab ] 	= 0.0;
	jacobian[ STATE_xrb ][ STATE_zab ] 	= 0.0;
	jacobian[ STATE_xrb ][ STATE_xrb ] 	= 0.0;
	jacobian[ STATE_xrb ][ STATE_zrb ] 	= 0.0;
	jacobian[ STATE_xrb ][ STATE_yawb ] 	= 0.0;

	jacobian[ STATE_zrb ][ STATE_vdot ] 	= 0.0;
	jacobian[ STATE_zrb ][ STATE_v ] 	= 0.0;
	jacobian[ STATE_zrb ][ STATE_phidot ] 	= 0.0;
	jacobian[ STATE_zrb ][ STATE_phi ] 	= 0.0;
	jacobian[ STATE_zrb ][ STATE_theta ] 	= 0.0;
	jacobian[ STATE_zrb ][ STATE_psi ] 	= 0.0;
	jacobian[ STATE_zrb ][ STATE_x ] 	= 0.0;
	jacobian[ STATE_zrb ][ STATE_y ] 	= 0.0;
	jacobian[ STATE_zrb ][ STATE_xab ] 	= 0.0;
	jacobian[ STATE_zrb ][ STATE_yab ] 	= 0.0;
	jacobian[ STATE_zrb ][ STATE_zab ] 	= 0.0;
	jacobian[ STATE_zrb ][ STATE_xrb ] 	= 0.0;
	jacobian[ STATE_zrb ][ STATE_zrb ] 	= 0.0;
	jacobian[ STATE_zrb ][ STATE_yawb ] 	= 0.0;

	jacobian[ STATE_yawb ][ STATE_vdot ] 	= 0.0;
	jacobian[ STATE_yawb ][ STATE_v ] 	= 0.0;
	jacobian[ STATE_yawb ][ STATE_phidot ] 	= 0.0;
	jacobian[ STATE_yawb ][ STATE_phi ] 	= 0.0;
	jacobian[ STATE_yawb ][ STATE_theta ] 	= 0.0;
	jacobian[ STATE_yawb ][ STATE_psi ] 	= 0.0;
	jacobian[ STATE_yawb ][ STATE_x ] 	= 0.0;
	jacobian[ STATE_yawb ][ STATE_y ] 	= 0.0;
	jacobian[ STATE_yawb ][ STATE_xab ] 	= 0.0;
	jacobian[ STATE_yawb ][ STATE_yab ] 	= 0.0;
	jacobian[ STATE_yawb ][ STATE_zab ] 	= 0.0;
	jacobian[ STATE_yawb ][ STATE_xrb ] 	= 0.0;
	jacobian[ STATE_yawb ][ STATE_zrb ] 	= 0.0;
	jacobian[ STATE_yawb ][ STATE_yawb ] 	= 0.0;


#ifdef PRINT_DEBUG
	/* print_matrix( "jacobian", jacobian, STATE_SIZE, STATE_SIZE ); */
#endif

}


/****************  Measurement Model Manifestations   **************

  apply_measurement()
  This function is called to predict the next measurement given a
  predicted state.  It is an evaluation of the measurement's transfer
  function.                                      */

void apply_measurement( uFloat *newState, uFloat *est_measurement )
{

  est_measurement[ MEAS_xa ] = newState[ STATE_vdot ] + newState[ STATE_theta ] * GRAVITY + newState[ STATE_xab ];
	// + newState[ STATE_v ] * newState[ STATE_v ] * newState[ STATE_phi ]
	// * newState[ STATE_phi ] * newState[ STATE_theta ] / RADIUS;


  est_measurement[ MEAS_ya ] = newState[ STATE_v ] * newState[ STATE_v ] * newState[ STATE_phi ] / RADIUS
	- newState[ STATE_phi ] * GRAVITY + newState[ STATE_yab ];

  est_measurement[ MEAS_za ] = newState[ STATE_theta ] * newState[ STATE_vdot ]
	- GRAVITY + newState[ STATE_zab ];
	// + newState[ STATE_phi ] * newState[ STATE_phi ] * newState[ STATE_v ]
	// * newState[ STATE_v ] / RADIUS;


  est_measurement[ MEAS_xr ] = -newState [ STATE_phidot ] + PHICENTER * newState[ STATE_phi ] + newState[ STATE_xrb ];
	// - newState[ STATE_theta ] * newState[ STATE_v ] * newState[ STATE_phi ] / RADIUS;


  est_measurement[ MEAS_zr ] = - newState[ STATE_v ] * newState[ STATE_phi ] / RADIUS
	+ newState[ STATE_zrb ];
	// newState [ STATE_theta ] * newState [ STATE_phidot ];

  est_measurement[ MEAS_xg ] = newState[ STATE_x ];

  est_measurement[ MEAS_yg ] = newState[ STATE_y ];

  est_measurement[ MEAS_psig ] = newState[ STATE_psi ];

  est_measurement[ MEAS_vg ] = newState[ STATE_v ];

  est_measurement[ MEAS_omega ] = newState[ STATE_v ] / RADIUS; // * cos( newState[ STATE_phi ] ));

  est_measurement[ MEAS_yaw ] = - newState[ STATE_v ] * newState[ STATE_phi ] / RADIUS
	+ newState[ STATE_yawb ];

}

/*  generate_measurement_transfer
    If non-linear, this function generates the pseudo-transfer
    function, representing the first terms of a taylor expansion
    of the actual non-linear measurement transfer function.     */

void generate_measurement_transfer( uFloat *state, uFloat **H )
{

  H[ MEAS_xa ][ STATE_vdot ] 	= 1.0;
  H[ MEAS_xa ][ STATE_v ] 	= 0.0; // 2.0 * state[ STATE_v ] * state[ STATE_phi ] * state[ STATE_phi ]
					// * state[ STATE_theta ] / RADIUS;
  H[ MEAS_xa ][ STATE_phidot ] 	= 0.0;
  H[ MEAS_xa ][ STATE_phi ] 	= 0.0; //2.0 * state[ STATE_v ] * state[ STATE_v ] * state[ STATE_phi ]
					//* state[ STATE_theta ] / RADIUS;
  H[ MEAS_xa ][ STATE_theta ] 	= GRAVITY; // state[ STATE_v ] * state[ STATE_v ] * state[ STATE_phi ]
					// * state[ STATE_phi ] / RADIUS - GRAVITY;
  H[ MEAS_xa ][ STATE_psi ] 	= 0.0;
  H[ MEAS_xa ][ STATE_x ] 	= 0.0;
  H[ MEAS_xa ][ STATE_y ] 	= 0.0;
  H[ MEAS_xa ][ STATE_xab ] 	= 1.0;
  H[ MEAS_xa ][ STATE_yab ] 	= 0.0;
  H[ MEAS_xa ][ STATE_zab ] 	= 0.0;
  H[ MEAS_xa ][ STATE_xrb ] 	= 0.0;
  H[ MEAS_xa ][ STATE_zrb ] 	= 0.0;
  H[ MEAS_xa ][ STATE_yawb ] 	= 0.0;

  H[ MEAS_ya ][ STATE_vdot ] 	= 0.0;
  H[ MEAS_ya ][ STATE_v ] 	= 2.0 * state[ STATE_v ] * state[ STATE_phi ] / RADIUS;
  H[ MEAS_ya ][ STATE_phidot ] 	= 0.0;
  H[ MEAS_ya ][ STATE_phi ] 	= state[ STATE_v ] * state[ STATE_v ] / RADIUS - GRAVITY;
  H[ MEAS_ya ][ STATE_theta ] 	= 0.0;
  H[ MEAS_ya ][ STATE_psi ] 	= 0.0;
  H[ MEAS_ya ][ STATE_x ] 	= 0.0;
  H[ MEAS_ya ][ STATE_y ] 	= 0.0;
  H[ MEAS_ya ][ STATE_xab ] 	= 0.0;
  H[ MEAS_ya ][ STATE_yab ] 	= 1.0;
  H[ MEAS_ya ][ STATE_zab ] 	= 0.0;
  H[ MEAS_ya ][ STATE_xrb ] 	= 0.0;
  H[ MEAS_ya ][ STATE_zrb ] 	= 0.0;
  H[ MEAS_ya ][ STATE_yawb ] 	= 0.0;


  H[ MEAS_za ][ STATE_vdot ] 	= state[ STATE_theta ];
  H[ MEAS_za ][ STATE_v ] 	= 0.0; //2.0 * state[ STATE_v ] * state[ STATE_phi ] * state[ STATE_phi ] / RADIUS;
  H[ MEAS_za ][ STATE_phidot ] 	= 0.0;
  H[ MEAS_za ][ STATE_phi ] 	= 0.0; //2.0 * state[ STATE_v ] * state[ STATE_v ] * state[ STATE_phi ] / RADIUS;
  H[ MEAS_za ][ STATE_theta ] 	= -state[ STATE_vdot ];
  H[ MEAS_za ][ STATE_psi ] 	= 0.0;
  H[ MEAS_za ][ STATE_x ] 	= 0.0;
  H[ MEAS_za ][ STATE_y ] 	= 0.0;
  H[ MEAS_za ][ STATE_xab ] 	= 0.0;
  H[ MEAS_za ][ STATE_yab ] 	= 0.0;
  H[ MEAS_za ][ STATE_zab ] 	= 1.0;
  H[ MEAS_za ][ STATE_xrb ] 	= 0.0;
  H[ MEAS_za ][ STATE_zrb ] 	= 0.0;
  H[ MEAS_za ][ STATE_yawb ] 	= 0.0;

  H[ MEAS_xr ][ STATE_vdot ] 	= 0.0;
  H[ MEAS_xr ][ STATE_v ] 	= 0.0;//-state[ STATE_theta ] * state[ STATE_phi ] / RADIUS;
  H[ MEAS_xr ][ STATE_phidot ] 	= 1.0;
  H[ MEAS_xr ][ STATE_phi ] 	= 0.0;// -state[ STATE_v ] * state[ STATE_theta ] / RADIUS;
  H[ MEAS_xr ][ STATE_theta ] 	= 0.0;//-state[ STATE_v ] * state[ STATE_phi ] / RADIUS;
  H[ MEAS_xr ][ STATE_psi ] 	= 0.0;
  H[ MEAS_xr ][ STATE_x ] 	= 0.0;
  H[ MEAS_xr ][ STATE_y ] 	= 0.0;
  H[ MEAS_xr ][ STATE_xab ] 	= 0.0;
  H[ MEAS_xr ][ STATE_yab ] 	= 0.0;
  H[ MEAS_xr ][ STATE_zab ] 	= 0.0;
  H[ MEAS_xr ][ STATE_xrb ] 	= 1.0;
  H[ MEAS_xr ][ STATE_zrb ] 	= 0.0;
  H[ MEAS_xr ][ STATE_yawb ] 	= 0.0;

  H[ MEAS_zr ][ STATE_vdot ] 	= 0.0;
  H[ MEAS_zr ][ STATE_v ] 	= -state[ STATE_phi ] / RADIUS;
  H[ MEAS_zr ][ STATE_phidot ] 	= 0.0; //state[ STATE_theta ];
  H[ MEAS_zr ][ STATE_phi ] 	= -state[ STATE_v ] / RADIUS;
  H[ MEAS_zr ][ STATE_theta ] 	= 0.0; //state[ STATE_phidot ];
  H[ MEAS_zr ][ STATE_psi ] 	= 0.0;
  H[ MEAS_zr ][ STATE_x ] 	= 0.0;
  H[ MEAS_zr ][ STATE_y ] 	= 0.0;
  H[ MEAS_zr ][ STATE_xab ] 	= 0.0;
  H[ MEAS_zr ][ STATE_yab ] 	= 0.0;
  H[ MEAS_zr ][ STATE_zab ] 	= 0.0;
  H[ MEAS_zr ][ STATE_xrb ] 	= 0.0;
  H[ MEAS_zr ][ STATE_zrb ] 	= 1.0;
  H[ MEAS_zr ][ STATE_yawb ] 	= 0.0;

  H[ MEAS_xg ][ STATE_vdot ] 	= 0.0;
  H[ MEAS_xg ][ STATE_v ] 	= 0.0;
  H[ MEAS_xg ][ STATE_phidot ] 	= 0.0;
  H[ MEAS_xg ][ STATE_phi ] 	= 0.0;
  H[ MEAS_xg ][ STATE_theta ] 	= 0.0;
  H[ MEAS_xg ][ STATE_psi ] 	= 0.0;
  H[ MEAS_xg ][ STATE_x ] 	= 1.0;
  H[ MEAS_xg ][ STATE_y ] 	= 0.0;
  H[ MEAS_xg ][ STATE_xab ] 	= 0.0;
  H[ MEAS_xg ][ STATE_yab ] 	= 0.0;
  H[ MEAS_xg ][ STATE_zab ] 	= 0.0;
  H[ MEAS_xg ][ STATE_xrb ] 	= 0.0;
  H[ MEAS_xg ][ STATE_zrb ] 	= 0.0;
  H[ MEAS_xg ][ STATE_yawb ] 	= 0.0;

  H[ MEAS_yg ][ STATE_vdot ] 	= 0.0;
  H[ MEAS_yg ][ STATE_v ] 	= 0.0;
  H[ MEAS_yg ][ STATE_phidot ] 	= 0.0;
  H[ MEAS_yg ][ STATE_phi ] 	= 0.0;
  H[ MEAS_yg ][ STATE_theta ] 	= 0.0;
  H[ MEAS_yg ][ STATE_psi ] 	= 0.0;
  H[ MEAS_yg ][ STATE_x ] 	= 0.0;
  H[ MEAS_yg ][ STATE_y ] 	= 1.0;
  H[ MEAS_yg ][ STATE_xab ] 	= 0.0;
  H[ MEAS_yg ][ STATE_yab ] 	= 0.0;
  H[ MEAS_yg ][ STATE_zab ] 	= 0.0;
  H[ MEAS_yg ][ STATE_xrb ] 	= 0.0;
  H[ MEAS_yg ][ STATE_zrb ] 	= 0.0;
  H[ MEAS_yg ][ STATE_yawb ] 	= 0.0;

  H[ MEAS_psig ][ STATE_vdot ] 	= 0.0;
  H[ MEAS_psig ][ STATE_v ] 	= 0.0;
  H[ MEAS_psig ][ STATE_phidot ]= 0.0;
  H[ MEAS_psig ][ STATE_phi ] 	= 0.0;
  H[ MEAS_psig ][ STATE_theta ] = 0.0;
  H[ MEAS_psig ][ STATE_psi ] 	= 1.0;
  H[ MEAS_psig ][ STATE_x ] 	= 0.0;
  H[ MEAS_psig ][ STATE_y ] 	= 0.0;
  H[ MEAS_psig ][ STATE_xab ] 	= 0.0;
  H[ MEAS_psig ][ STATE_yab ] 	= 0.0;
  H[ MEAS_psig ][ STATE_zab ] 	= 0.0;
  H[ MEAS_psig ][ STATE_xrb ] 	= 0.0;
  H[ MEAS_psig ][ STATE_zrb ] 	= 0.0;
  H[ MEAS_psig ][ STATE_yawb ] 	= 0.0;

  H[ MEAS_vg ][ STATE_vdot ] 	= 0.0;
  H[ MEAS_vg ][ STATE_v ] 	= 1.0;
  H[ MEAS_vg ][ STATE_phidot ] 	= 0.0;
  H[ MEAS_vg ][ STATE_phi ] 	= 0.0;
  H[ MEAS_vg ][ STATE_theta ] 	= 0.0;
  H[ MEAS_vg ][ STATE_psi ] 	= 0.0;
  H[ MEAS_vg ][ STATE_x ] 	= 0.0;
  H[ MEAS_vg ][ STATE_y ] 	= 0.0;
  H[ MEAS_vg ][ STATE_xab ] 	= 0.0;
  H[ MEAS_vg ][ STATE_yab ] 	= 0.0;
  H[ MEAS_vg ][ STATE_zab ] 	= 0.0;
  H[ MEAS_vg ][ STATE_xrb ] 	= 0.0;
  H[ MEAS_vg ][ STATE_zrb ] 	= 0.0;
  H[ MEAS_vg ][ STATE_yawb ] 	= 0.0;

  H[ MEAS_omega ][ STATE_vdot ]   = 0.0;
  H[ MEAS_omega ][ STATE_v ] 	  = 1.0 / ( RADIUS ); // * cos(state[ STATE_phi ]) );
  H[ MEAS_omega ][ STATE_phidot ] = 0.0;
  H[ MEAS_omega ][ STATE_phi ]    = 0.0; //state[ STATE_v ]* sin(state[ STATE_phi ]) / (RADIUS * cos(state[ STATE_phi ])
					// * cos(state[ STATE_phi ]) );
  H[ MEAS_omega ][ STATE_theta ]  = 0.0;
  H[ MEAS_omega ][ STATE_psi ]    = 0.0;
  H[ MEAS_omega ][ STATE_x ] 	  = 0.0;
  H[ MEAS_omega ][ STATE_y ] 	  = 0.0;
  H[ MEAS_omega ][ STATE_xab ]    = 0.0;
  H[ MEAS_omega ][ STATE_yab ]    = 0.0;
  H[ MEAS_omega ][ STATE_zab ]    = 0.0;
  H[ MEAS_omega ][ STATE_xrb ]    = 0.0;
  H[ MEAS_omega ][ STATE_zrb ]    = 0.0;
  H[ MEAS_omega ][ STATE_yawb ]    = 0.0;

  H[ MEAS_yaw ][ STATE_vdot ] 	= 0.0;
  H[ MEAS_yaw ][ STATE_v ] 		= -state[ STATE_phi ] / RADIUS;
  H[ MEAS_yaw ][ STATE_phidot ] = 0.0;
  H[ MEAS_yaw ][ STATE_phi ] 	= -state[ STATE_v ] / RADIUS;
  H[ MEAS_yaw ][ STATE_theta ] 	= 0.0;
  H[ MEAS_yaw ][ STATE_psi ] 	= 0.0;
  H[ MEAS_yaw ][ STATE_x ] 		= 0.0;
  H[ MEAS_yaw ][ STATE_y ] 		= 0.0;
  H[ MEAS_yaw ][ STATE_xab ] 	= 0.0;
  H[ MEAS_yaw ][ STATE_yab ] 	= 0.0;
  H[ MEAS_yaw ][ STATE_zab ] 	= 0.0;
  H[ MEAS_yaw ][ STATE_xrb ] 	= 0.0;
  H[ MEAS_yaw ][ STATE_zrb ] 	= 0.0;
  H[ MEAS_yaw ][ STATE_yawb ] 	= 1.0;

}


/* set the covariance */
void covarianceSet( uFloat **Qk, uFloat **R )
{
  int  row, col;

  for( row = 1; row <= STATE_SIZE; row++ )
    for( col = 1; col <= STATE_SIZE; col++ )
      Qk[ row ][ col ] = 0.0;

  /* These values actually define Qc, then we multiply by PERIOD to get Qk */

  Qk[ STATE_vdot ][ STATE_vdot ] 	= 0.0001;
  Qk[ STATE_v ][ STATE_v ] 			= 0.0;
  Qk[ STATE_phidot ][ STATE_phidot ]= 0.01;
  Qk[ STATE_phi ][ STATE_phi ] 		= 0.0;
  Qk[ STATE_theta ][ STATE_theta ] 	= 0.002;
  Qk[ STATE_psi ][ STATE_psi ] 		= 0.0000001;
  Qk[ STATE_x ][ STATE_x ] 			= 0.0;
  Qk[ STATE_y ][ STATE_y ] 			= 0.0;
  Qk[ STATE_xab ][ STATE_xab ] 		= 0.000005;
  Qk[ STATE_yab ][ STATE_yab ] 		= 0.00005;
  Qk[ STATE_zab ][ STATE_zab ] 		= 0.000005;
  Qk[ STATE_xrb ][ STATE_xrb ] 		= 0.00005;
  Qk[ STATE_zrb ][ STATE_zrb ]  	= 0.000001;
  Qk[ STATE_yawb ][ STATE_yawb ]  	= 0.0000000001;

  matMultScalar( Qk, PERIOD, Qk, STATE_SIZE, STATE_SIZE );

  for( row = 1; row <= MEAS_SIZE; row++ )
    for( col = 1; col <= MEAS_SIZE; col++ )
      R[ row ][ col ] = 0.0;

  R[ MEAS_xa ][ MEAS_xa ] 		= 1.0;
  R[ MEAS_ya ][ MEAS_ya ] 		= 1.0;
  R[ MEAS_za ][ MEAS_za ] 		= 1.0;
  R[ MEAS_xr ][ MEAS_xr ] 		= 0.1;
  R[ MEAS_zr ][ MEAS_zr ] 		= 0.2;
  R[ MEAS_xg ][ MEAS_xg ] 		= 1.5;
  R[ MEAS_yg ][ MEAS_yg ] 		= 1.5;
  R[ MEAS_psig ][ MEAS_psig ] 	= 2*PI;
  R[ MEAS_vg ][ MEAS_vg ] 		= 2.0;
  R[ MEAS_omega ][ MEAS_omega ] = 0.1;
  R[ MEAS_yaw ][ MEAS_yaw ] 	= 0.005;
}

