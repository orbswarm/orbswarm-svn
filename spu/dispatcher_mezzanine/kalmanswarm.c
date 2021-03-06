/*  kalmanswarm.c
    
    This file contains code segments for a Kalman Filter that characterize
    a particular system and measurement model.

    In this case, the system being characterized is spherical art robot.

    Rehashed for SWARM (http://www.orbswarm.com) by Michael Prados, 8/10/07
*/


#define PI 3.14159265359


#include <stdio.h>
#include <math.h>
#include "kalman.h"
#include "kalmanswarm.h"
#include "matmath.h"

extern int     debug;


/*  Temporary variables, declared statically to avoid lots of run-time
    memory allocation.      */

static uFloat  *measurementVec;        /* a measurement_size x 1 vector */
static uFloat  *stateVec;              /* a state_size x 1 vector */


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

   x[ STATE_vdot ] 	= stateEstimate->vdot;
   x[ STATE_v ]    	= stateEstimate->v;
   x[ STATE_phidot ] 	= stateEstimate->phidot;
   x[ STATE_phi ] 	= stateEstimate->phi;
   x[ STATE_psi ] 	= stateEstimate->psi;
   x[ STATE_theta ] 	= stateEstimate->theta;
   x[ STATE_x ] 	= stateEstimate->x;
   x[ STATE_y ] 	= stateEstimate->y;

   extended_kalman_init( P, x );

   freeMatrix( P, 1, STATE_SIZE, 1, STATE_SIZE );
   freeVector( x, 1, STATE_SIZE );   	

   return SWARM_SUCCESS;
}


int kalmanProcess( struct swarmGpsData * gpsData, struct swarmImuData * imuData, struct swarmStateEstimate * stateEstimate)
{

   measurementVec[ MEAS_xa ] = imuData->si_accy;
   measurementVec[ MEAS_ya ] = imuData->si_accz;
   measurementVec[ MEAS_za ] = imuData->si_accx;
   measurementVec[ MEAS_xr ] = -(imuData->si_ratey);
   measurementVec[ MEAS_zr ] = imuData->si_ratex;

/*
   measurementVec[ MEAS_omega ] = swarmImuData.omega;
*/

   measurementVec[ MEAS_xg ] = gpsData->metFromMshipEast;
   measurementVec[ MEAS_yg ] = gpsData->metFromMshipNorth;
   measurementVec[ MEAS_psig ] = (uFloat)gpsData->nmea_course;   
   measurementVec[ MEAS_vg ] = (uFloat)gpsData->speed;

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

   return SWARM_SUCCESS;
}

/****************  System Model Manifestations   **************
*/
 

void systemF( uFloat *stateDot, uFloat *state )
{
	stateDot[ STATE_vdot ] 		= 0.0;
	stateDot[ STATE_v ] 		= state[ STATE_vdot ];
	stateDot[ STATE_phidot ] 	= 0.0;
	stateDot[ STATE_phi ] 		= state[ STATE_phidot ];
	stateDot[ STATE_theta ] 	= 0.0;
	stateDot[ STATE_psi ] 		= state[ STATE_v ] * tan( state[ STATE_phi ] ) / RADIUS;
	stateDot[ STATE_x ] 		= state[ STATE_v ] * cos( state[ STATE_psi ] );
	stateDot[ STATE_y ] 		= state[ STATE_v ] * sin( state[ STATE_psi ] );
	
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


	jacobian[ STATE_v ][ STATE_vdot ] 	= 1.0;
	jacobian[ STATE_v ][ STATE_v ] 		= 0.0;
	jacobian[ STATE_v ][ STATE_phidot ] 	= 0.0;
	jacobian[ STATE_v ][ STATE_phi ] 	= 0.0;
	jacobian[ STATE_v ][ STATE_theta ] 	= 0.0;
	jacobian[ STATE_v ][ STATE_psi ] 	= 0.0;
	jacobian[ STATE_v ][ STATE_x ] 		= 0.0;
	jacobian[ STATE_v ][ STATE_y ] 		= 0.0;


	jacobian[ STATE_phidot ][ STATE_vdot ] 	= 0.0;
	jacobian[ STATE_phidot ][ STATE_v ] 	= 0.0;
	jacobian[ STATE_phidot ][ STATE_phidot ]= 0.0;
	jacobian[ STATE_phidot ][ STATE_phi ] 	= 0.0;
	jacobian[ STATE_phidot ][ STATE_theta ] = 0.0;
	jacobian[ STATE_phidot ][ STATE_psi ] 	= 0.0;
	jacobian[ STATE_phidot ][ STATE_x ] 	= 0.0;
	jacobian[ STATE_phidot ][ STATE_y ] 	= 0.0;


	jacobian[ STATE_phi ][ STATE_vdot ] 	= 0.0;
	jacobian[ STATE_phi ][ STATE_v ] 	= 0.0;
	jacobian[ STATE_phi ][ STATE_phidot ] 	= 1.0;
	jacobian[ STATE_phi ][ STATE_phi ] 	= 0.0;
	jacobian[ STATE_phi ][ STATE_theta ] 	= 0.0;
	jacobian[ STATE_phi ][ STATE_psi ] 	= 0.0;
	jacobian[ STATE_phi ][ STATE_x ] 	= 0.0;
	jacobian[ STATE_phi ][ STATE_y ] 	= 0.0;


	jacobian[ STATE_theta ][ STATE_vdot ] 	= 0.0;
	jacobian[ STATE_theta ][ STATE_v ] 	= 0.0;
	jacobian[ STATE_theta ][ STATE_phidot ] = 0.0;
	jacobian[ STATE_theta ][ STATE_phi ] 	= 0.0;
	jacobian[ STATE_theta ][ STATE_theta ] 	= 0.0;
	jacobian[ STATE_theta ][ STATE_psi ] 	= 0.0;
	jacobian[ STATE_theta ][ STATE_x ] 	= 0.0;
	jacobian[ STATE_theta ][ STATE_y ] 	= 0.0;


	jacobian[ STATE_psi ][ STATE_vdot ] 	= 0.0;
	jacobian[ STATE_psi ][ STATE_v ] 	= tan( state[ STATE_phi ] ) / RADIUS;
	jacobian[ STATE_psi ][ STATE_phidot ] 	= 0.0;
	jacobian[ STATE_psi ][ STATE_phi ] 	= state[ STATE_v ] / (cos( state[ STATE_phi ] ) 
							* cos( state[ STATE_phi ] ) * RADIUS);
	jacobian[ STATE_psi ][ STATE_theta ] 	= 0.0;
	jacobian[ STATE_psi ][ STATE_psi ] 	= 0.0;
	jacobian[ STATE_psi ][ STATE_x ] 	= 0.0;
	jacobian[ STATE_psi ][ STATE_y ] 	= 0.0;


	jacobian[ STATE_x ][ STATE_vdot ] 	= 0.0;
	jacobian[ STATE_x ][ STATE_v ] 		= cos( state[ STATE_psi ] );
	jacobian[ STATE_x ][ STATE_phidot ] 	= 0.0;
	jacobian[ STATE_x ][ STATE_phi ] 	= 0.0;
	jacobian[ STATE_x ][ STATE_theta ] 	= 0.0;
	jacobian[ STATE_x ][ STATE_psi ] 	= -state[ STATE_v ] * sin( state[ STATE_psi ] );
	jacobian[ STATE_x ][ STATE_x ] 		= 0.0;
	jacobian[ STATE_x ][ STATE_y ] 		= 0.0;


	jacobian[ STATE_y ][ STATE_vdot ] 	= 0.0;
	jacobian[ STATE_y ][ STATE_v ] 		= sin( state[ STATE_psi ] );
	jacobian[ STATE_y ][ STATE_phidot ] 	= 0.0;
	jacobian[ STATE_y ][ STATE_phi ] 	= 0.0;
	jacobian[ STATE_y ][ STATE_theta ] 	= 0.0;
	jacobian[ STATE_y ][ STATE_psi ] 	= state[ STATE_v ] * cos( state[ STATE_psi ] );
	jacobian[ STATE_y ][ STATE_x ] 		= 0.0;
	jacobian[ STATE_y ][ STATE_y ] 		= 0.0;



#ifdef PRINT_DEBUG
	/* printMatrix( "jacobian", jacobian, STATE_SIZE, STATE_SIZE ); */
#endif

}


/****************  Measurement Model Manifestations   **************
  
  apply_measurement()
  This function is called to predict the next measurement given a
  predicted state.  It is an evaluation of the measurement's transfer
  function.                                      */

void apply_measurement( uFloat *newState, uFloat *est_measurement )
{

  est_measurement[ MEAS_xa ] = newState[ STATE_vdot ] 
	- newState[ STATE_v ] * newState[ STATE_v ] * newState[ STATE_phi ] 
	* newState[ STATE_phi ] * newState[ STATE_theta ] / RADIUS
	+ newState[ STATE_theta ] * GRAVITY;

  est_measurement[ MEAS_ya ] = newState[ STATE_v ] * newState[ STATE_v ] * newState[ STATE_phi ] / RADIUS
	+ newState[ STATE_phi ] * GRAVITY;
	
  est_measurement[ MEAS_za ] = newState[ STATE_theta ] * newState[ STATE_vdot ] 
	+ newState[ STATE_phi ] * newState[ STATE_phi ] * newState[ STATE_v ] 
	* newState[ STATE_v ] / RADIUS
	- GRAVITY;

  est_measurement[ MEAS_xr ] = newState [ STATE_phidot ] 
	- newState[ STATE_theta ] * newState[ STATE_v ] * newState[ STATE_phi ] / RADIUS;

  est_measurement[ MEAS_zr ] = newState [ STATE_theta ] * newState [ STATE_phidot ] 
	+ newState[ STATE_v ] * newState[ STATE_phi ] / RADIUS;

  est_measurement[ MEAS_xg ] = newState[ STATE_x ]; 

  est_measurement[ MEAS_yg ] = newState[ STATE_y ];

  est_measurement[ MEAS_psig ] = ( newState[ STATE_psi ]-floor(newState[ STATE_psi ] / (2 * PI))*2*PI );

  est_measurement[ MEAS_vg ] = newState[ STATE_v ];

  est_measurement[ MEAS_omega ] = newState[ STATE_v ] / (RADIUS * cos( newState[ STATE_phi ] ));

}

/*  generate_measurement_transfer
    If non-linear, this function generates the pseudo-transfer
    function, representing the first terms of a taylor expansion
    of the actual non-linear measurement transfer function.     */

void generate_measurement_transfer( uFloat *state, uFloat **H )
{


#ifdef PRINT_DEBUG
  printf( "ekf: generating measurement jacobian\n" );
#endif

  H[ MEAS_xa ][ STATE_vdot ] 	= 1.0;
  H[ MEAS_xa ][ STATE_v ] 	= -2.0 * state[ STATE_v ] * state[ STATE_phi ] * state[ STATE_phi ] 
					* state[ STATE_theta ] / RADIUS;
  H[ MEAS_xa ][ STATE_phidot ] 	= 0.0;
  H[ MEAS_xa ][ STATE_phi ] 	= -2.0 * state[ STATE_v ] * state[ STATE_v ] * state[ STATE_phi ] 
					* state[ STATE_theta ] / RADIUS;
  H[ MEAS_xa ][ STATE_theta ] 	= -state[ STATE_v ] * state[ STATE_v ] * state[ STATE_phi ] 
					* state[ STATE_phi ] / RADIUS + GRAVITY;
  H[ MEAS_xa ][ STATE_psi ] 	= 0.0;
  H[ MEAS_xa ][ STATE_x ] 	= 0.0;
  H[ MEAS_xa ][ STATE_y ] 	= 0.0;


  H[ MEAS_ya ][ STATE_vdot ] 	= 0.0;
  H[ MEAS_ya ][ STATE_v ] 	= 2.0 * state[ STATE_v ] * state[ STATE_phi ] / RADIUS;
  H[ MEAS_ya ][ STATE_phidot ] 	= 0.0;
  H[ MEAS_ya ][ STATE_phi ] 	= state[ STATE_v ] * state[ STATE_v ] / RADIUS + GRAVITY;
  H[ MEAS_ya ][ STATE_theta ] 	= 0.0;
  H[ MEAS_ya ][ STATE_psi ] 	= 0.0;
  H[ MEAS_ya ][ STATE_x ] 	= 0.0;
  H[ MEAS_ya ][ STATE_y ] 	= 0.0;
 

  H[ MEAS_za ][ STATE_vdot ] 	= state[ STATE_theta ];
  H[ MEAS_za ][ STATE_v ] 	= 2.0 * state[ STATE_v ] * state[ STATE_phi ] * state[ STATE_phi ] / RADIUS;
  H[ MEAS_za ][ STATE_phidot ] 	= 0.0;
  H[ MEAS_za ][ STATE_phi ] 	= 2.0 * state[ STATE_v ] * state[ STATE_v ] * state[ STATE_phi ] / RADIUS;
  H[ MEAS_za ][ STATE_theta ] 	= state[ STATE_vdot ];
  H[ MEAS_za ][ STATE_psi ] 	= 0.0;
  H[ MEAS_za ][ STATE_x ] 	= 0.0;
  H[ MEAS_za ][ STATE_y ] 	= 0.0;


  H[ MEAS_xr ][ STATE_vdot ] 	= 0.0;
  H[ MEAS_xr ][ STATE_v ] 	= -state[ STATE_theta ] * state[ STATE_phi ] / RADIUS;
  H[ MEAS_xr ][ STATE_phidot ] 	= 1.0;
  H[ MEAS_xr ][ STATE_phi ] 	= -state[ STATE_v ] * state[ STATE_theta ] / RADIUS;
  H[ MEAS_xr ][ STATE_theta ] 	= -state[ STATE_v ] * state[ STATE_phi ] / RADIUS;
  H[ MEAS_xr ][ STATE_psi ] 	= 0.0;
  H[ MEAS_xr ][ STATE_x ] 	= 0.0;
  H[ MEAS_xr ][ STATE_y ] 	= 0.0;
 

  H[ MEAS_zr ][ STATE_vdot ] 	= 0.0;
  H[ MEAS_zr ][ STATE_v ] 	= state[ STATE_phi ] / RADIUS;
  H[ MEAS_zr ][ STATE_phidot ] 	= state[ STATE_theta ];
  H[ MEAS_zr ][ STATE_phi ] 	= state[ STATE_v ] / RADIUS;
  H[ MEAS_zr ][ STATE_theta ] 	= state[ STATE_phidot ];
  H[ MEAS_zr ][ STATE_psi ] 	= 0.0;
  H[ MEAS_zr ][ STATE_x ] 	= 0.0;
  H[ MEAS_zr ][ STATE_y ] 	= 0.0;
 

  H[ MEAS_xg ][ STATE_vdot ] 	= 0.0;
  H[ MEAS_xg ][ STATE_v ] 	= 0.0;
  H[ MEAS_xg ][ STATE_phidot ] 	= 0.0;
  H[ MEAS_xg ][ STATE_phi ] 	= 0.0;
  H[ MEAS_xg ][ STATE_theta ] 	= 0.0;
  H[ MEAS_xg ][ STATE_psi ] 	= 0.0;
  H[ MEAS_xg ][ STATE_x ] 	= 1.0;
  H[ MEAS_xg ][ STATE_y ] 	= 0.0;


  H[ MEAS_yg ][ STATE_vdot ] 	= 0.0;
  H[ MEAS_yg ][ STATE_v ] 	= 0.0;
  H[ MEAS_yg ][ STATE_phidot ] 	= 0.0;
  H[ MEAS_yg ][ STATE_phi ] 	= 0.0;
  H[ MEAS_yg ][ STATE_theta ] 	= 0.0;
  H[ MEAS_yg ][ STATE_psi ] 	= 0.0;
  H[ MEAS_yg ][ STATE_x ] 	= 0.0;
  H[ MEAS_yg ][ STATE_y ] 	= 1.0;


  H[ MEAS_psig ][ STATE_vdot ] 	= 0.0;
  H[ MEAS_psig ][ STATE_v ] 	= 0.0;
  H[ MEAS_psig ][ STATE_phidot ]= 0.0;
  H[ MEAS_psig ][ STATE_phi ] 	= 0.0;
  H[ MEAS_psig ][ STATE_theta ] = 0.0;
  H[ MEAS_psig ][ STATE_psi ] 	= 1.0;
  H[ MEAS_psig ][ STATE_x ] 	= 0.0;
  H[ MEAS_psig ][ STATE_y ] 	= 0.0;


  H[ MEAS_vg ][ STATE_vdot ] 	= 0.0;
  H[ MEAS_vg ][ STATE_v ] 	= 1.0;
  H[ MEAS_vg ][ STATE_phidot ] 	= 0.0;
  H[ MEAS_vg ][ STATE_phi ] 	= 0.0;
  H[ MEAS_vg ][ STATE_theta ] 	= 0.0;
  H[ MEAS_vg ][ STATE_psi ] 	= 0.0;
  H[ MEAS_vg ][ STATE_x ] 	= 0.0;
  H[ MEAS_vg ][ STATE_y ] 	= 0.0;


  H[ MEAS_omega ][ STATE_vdot ]   = 0.0;
  H[ MEAS_omega ][ STATE_v ] 	  = 1.0 / ( RADIUS * cos(state[ STATE_phi ]) );
  H[ MEAS_omega ][ STATE_phidot ] = 0.0;
  H[ MEAS_omega ][ STATE_phi ]    = state[ STATE_v ]* sin(state[ STATE_phi ]) / (RADIUS * cos(state[ STATE_phi ]) 
					* cos(state[ STATE_phi ]) );
  H[ MEAS_omega ][ STATE_theta ]  = 0.0;
  H[ MEAS_omega ][ STATE_psi ]    = 0.0;
  H[ MEAS_omega ][ STATE_x ] 	  = 0.0;
  H[ MEAS_omega ][ STATE_y ] 	  = 0.0;



}

/* set the covariance */

void covarianceSet( uFloat **Qk, uFloat **R )
{
  int  row, col;
  
  for( row = 1; row <= STATE_SIZE; row++ )
    for( col = 1; col <= STATE_SIZE; col++ )
      Qk[ row ][ col ] = 0.0;

  /* These values actually define Qc, then we multiply by PERIOD to get Qk */

  Qk[ STATE_vdot ][ STATE_vdot ] 	= 0.01;
  Qk[ STATE_v ][ STATE_v ] 		= 0.0;
  Qk[ STATE_phidot ][ STATE_phidot ]	= 0.001;
  Qk[ STATE_phi ][ STATE_phi ] 		= 0.0;
  Qk[ STATE_theta ][ STATE_theta ] 	= 0.01;
  Qk[ STATE_psi ][ STATE_psi ] 		= 0.0001;
  Qk[ STATE_x ][ STATE_x ] 		= 0.0;
  Qk[ STATE_y ][ STATE_y ] 		= 0.0;

  matMultScalar( Qk, PERIOD, Qk, STATE_SIZE, STATE_SIZE );
  

  for( row = 1; row <= MEAS_SIZE; row++ )
    for( col = 1; col <= MEAS_SIZE; col++ )
      R[ row ][ col ] = 0.0;

  R[ MEAS_xa ][ MEAS_xa ] 	= 0.1;
  R[ MEAS_ya ][ MEAS_ya ] 	= 0.1;
  R[ MEAS_za ][ MEAS_za ] 	= 0.1;
  R[ MEAS_xr ][ MEAS_xr ] 	= 0.1;
  R[ MEAS_zr ][ MEAS_zr ] 	= 0.1;
  R[ MEAS_xg ][ MEAS_xg ] 	= 0.1;
  R[ MEAS_yg ][ MEAS_yg ] 	= 0.1;
  R[ MEAS_psig ][ MEAS_psig ] 	= 0.1;
  R[ MEAS_vg ][ MEAS_vg ] 	= 0.1;
  R[ MEAS_omega ][ MEAS_omega ] = 0.1;

}




