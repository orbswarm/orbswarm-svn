/*  kalman.c

    This file contains the code for a kalman filter, an
    extended kalman filter, and an iterated extended kalman filter.

    For ready extensibility, the apply_measurement() and apply_system()
    functions are located in a separate file.

    It uses the matmath functions provided in an accompanying file
    to perform matrix manipulation.

    This code comes from a mixture of sources, and at this time, may not have clear rights.
    Redistribute at your own risk.
*/

#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include "kalman.h"
#include "kalmanswarm.h"


/*  The following are the global variables of the Kalman filters,
    used to point to data structures used throughout.     */

static uFloat  *state_pre;         /* ptr to apriori state vectors, x(-)     */
static uFloat  *state_post;        /* ptr to aposteriori state vectors, x(+) */

static uFloat  **cov_pre;          /* ptr to apriori covariance matrix, P(-) */
static uFloat  **cov_post;         /* ptr to apriori covariance matrix, P(-) */
static uFloat  **sys_noise_cov;    /* system noise covariance matrix (Qk)  */
static uFloat  **mea_noise_cov;    /* measurement noise variance vector (R)  */

static uFloat  **sys_transfer;     /* system transfer function (Phi)    */
static uFloat  **mea_transfer;     /* measurement transfer function (H) */

static uFloat  **kalman_gain;      /* The Kalman Gain matrix (K) */

int            global_step = 0;    /* the current step number (k) */


/*  Temporary variables, declared statically to avoid lots of run-time
    memory allocation.      */

static uFloat  *z_estimate;        /* a measurement_size x 1 vector */
static uFloat  **temp_state_state; /* a state_size x state_size matrix */
static uFloat  **temp_meas_state;  /* a measurement_size x state_size matrix */
static uFloat  **temp_state_meas;  /* a measurement_size x state_size matrix */
static uFloat  **temp_meas_meas;   /* a measurement_size squared matrix */
static uFloat  **temp_meas_2;      /* another one ! */

/* variables for Runge-Kutta */
static uFloat  *k1;        /* a state_size x 1 vector */
static uFloat  *k2;        /* a state_size x 1 vector */
static uFloat  *k3;        /* a state_size x 1 vector */
static uFloat  *k4;        /* a state_size x 1 vector */

static uFloat  *tempState;        /* a state_size x 1 vector */

/*  Prototypes of internal functions  */

static void rungeKutta( uFloat *old_state, uFloat *new_state );
 
static void generate_system_transfer( uFloat *state, uFloat **phi );

static void alloc_globals( int num_state,
			  int num_measurement );
static void update_system( uFloat *z, uFloat *x_minus,
			 uFloat **kalman_gain, uFloat *x_plus );
static void estimate_prob( uFloat **P_post, uFloat **Phi, uFloat **GQGt,
			  uFloat **P_pre );
static void update_prob( uFloat **P_pre, uFloat **R, uFloat **H,
			uFloat **P_post, uFloat **K );
static void take_inverse( uFloat **in, uFloat **out, int n );




/******************************************************************

  Non-linear Kalman Filtering

  extended_kalman_init()
  This function initializes the extended kalman filter.
*/

void extended_kalman_init( uFloat **P, uFloat *x )
{
#ifdef PRINT_DEBUG
  printf( "ekf: Initializing filter\n" );
#endif

  alloc_globals( STATE_SIZE, MEAS_SIZE );

  sys_transfer = matrix( 1, STATE_SIZE, 1, STATE_SIZE );
  mea_transfer = matrix( 1, MEAS_SIZE, 1, STATE_SIZE );

  /*  Init the global variables using the arguments.  */

  vecCopy( x, state_post, STATE_SIZE );
  vecCopy( x, state_pre, STATE_SIZE );
  matCopy( P, cov_post, STATE_SIZE, STATE_SIZE );
  matCopy( P, cov_pre, STATE_SIZE, STATE_SIZE );

  covarianceSet( sys_noise_cov, mea_noise_cov );	

#ifdef PRINT_DEBUG
  printf( "ekf: Completed Initialization\n" );
#endif

}


/*  extended_kalman_step()
    This function takes a set of measurements, and performs a single
    recursion of the extended kalman filter.
*/

void extended_kalman_step( uFloat *z_in )
{
#ifdef PRINT_DEBUG
  printf( "ekf: step %d\n", global_step );
#endif

#ifdef PRINT_DEBUG
	printVector( "measurements ", z_in, MEAS_SIZE );
#endif
  /*****************  Gain Loop  *****************
    First, linearize locally, then do normal gain loop    */

  generate_system_transfer( state_pre, sys_transfer );
  generate_measurement_transfer( state_pre, mea_transfer );

  estimate_prob( cov_post, sys_transfer, sys_noise_cov, cov_pre );
  update_prob( cov_pre, mea_noise_cov, mea_transfer, cov_post, kalman_gain );

  /**************  Estimation Loop  ***************/

  rungeKutta( state_post, state_pre );
  update_system( z_in, state_pre, kalman_gain, state_post );

  global_step++;
}


uFloat *kalman_get_state( void )
{
  return( state_post );
}

static void rungeKutta( uFloat *old_state, uFloat *new_state )
{

#ifdef PRINT_DEBUG
  printf( "ekf: rungeKutta\n" );
#endif

  systemF( tempState, old_state );

  vecScalarMult( tempState, PERIOD, k1, STATE_SIZE );

  /* k2 = period * F( old_state + k1 * 0.5) */
  vecScalarMult( k1, 0.5, tempState, STATE_SIZE ); /* tempState = k1 * 0.5 */
  vecAdd( old_state, tempState, tempState, STATE_SIZE );  /* tempState = old_state + k1 * 0.5 */
  systemF( tempState, old_state );
  vecScalarMult( tempState, PERIOD, k2, STATE_SIZE );

  /* k3 = period * F( old_state + k2 * 0.5) */
  vecScalarMult( k2, 0.5, tempState, STATE_SIZE ); /* tempState = k2 * 0.5 */
  vecAdd( old_state, tempState, tempState, STATE_SIZE );  /* tempState = old_state + k2 * 0.5 */
  systemF( tempState, old_state );
  vecScalarMult( tempState, PERIOD, k3, STATE_SIZE );
  
  /* k4 = period * F( old_state + k3) */
  vecAdd( old_state, tempState, tempState, STATE_SIZE );  /* tempState = old_state + k3 */
  systemF( tempState, old_state );
  vecScalarMult( tempState, PERIOD, k4, STATE_SIZE );

  /*  new_state = old_state + 0.16667 * ( k1 + 2*k2 + 2*k3 + k4 ) */
  vecCopy( k1, new_state, STATE_SIZE );
  vecScalarMult( k2, 2.0, tempState, STATE_SIZE );
  vecAdd( new_state, tempState, new_state, STATE_SIZE);
  vecScalarMult( k3, 2.0, tempState, STATE_SIZE );
  vecAdd( new_state, tempState, new_state, STATE_SIZE);
  vecAdd( new_state, k4, new_state, STATE_SIZE);
  vecScalarMult( new_state, 0.16667, new_state, STATE_SIZE );
  vecAdd( new_state, old_state, new_state, STATE_SIZE);

}

/*  generate_system_transfer
    This function generates the pseudo-transfer
    function, representing the first terms of a taylor expansion
    of the actual non-linear system transfer function.     */

void generate_system_transfer( uFloat *state, uFloat **phi )
{
  int  row, col;
  
#ifdef PRINT_DEBUG
  printf( "ekf: linearizing system transfer\n" );
#endif

  for( row = 1; row <= STATE_SIZE; row++ )
    for( col = 1; col <= STATE_SIZE; col++ )
      phi[ row ][ col ] = 0.0;

  for( col = 1; col <= STATE_SIZE; col++ )
    phi[ col ][ col ] = 1.0;

  systemJacobian( state, temp_state_state );

  matMultScalar( temp_state_state, PERIOD, temp_state_state,
	      STATE_SIZE, STATE_SIZE );	

  matAdd( phi, temp_state_state, phi, STATE_SIZE, STATE_SIZE );  	



}


/************************************************************

   Internal Functions, defined in order of appearance

   alloc_globals()
   This function allocates memory for the global variables used by this
   code module.     */

static void alloc_globals( int num_state, int num_measurement )
{
#ifdef PRINT_DEBUG
  printf( "ekf: allocating memory\n" );
#endif

  /*  Allocate some global variables.  */

  state_pre = vector( 1, STATE_SIZE );
  state_post = vector( 1, STATE_SIZE );
  cov_pre = matrix( 1, STATE_SIZE, 1, STATE_SIZE );
  cov_post = matrix( 1, STATE_SIZE, 1, STATE_SIZE );
  kalman_gain = matrix( 1, STATE_SIZE, 1, MEAS_SIZE );

  sys_noise_cov = matrix( 1, STATE_SIZE, 1, STATE_SIZE );
  mea_noise_cov = matrix( 1, MEAS_SIZE, 1, MEAS_SIZE );

  /*  Alloc some temporary variables   */

  z_estimate = vector( 1, MEAS_SIZE );
  temp_state_state = matrix( 1, STATE_SIZE, 1, STATE_SIZE );
  temp_meas_state = matrix( 1, MEAS_SIZE, 1, STATE_SIZE );
  temp_state_meas = matrix( 1, STATE_SIZE, 1, MEAS_SIZE );
  temp_meas_meas = matrix( 1, MEAS_SIZE, 1, MEAS_SIZE );
  temp_meas_2 = matrix( 1, MEAS_SIZE, 1, MEAS_SIZE );
  
  /* variables for Runge-Kutta */
  k1 = vector( 1, STATE_SIZE );
  k2 = vector( 1, STATE_SIZE );
  k3 = vector( 1, STATE_SIZE );
  k4 = vector( 1, STATE_SIZE );

  tempState = vector( 1, STATE_SIZE );

}
   
/* update_system()
   This function generates an updated version of the state estimate,
   based on what we know about the measurement system.       */

static void update_system( uFloat *z, uFloat *x_pre,
			 uFloat **K, uFloat *x_post )
{
#ifdef PRINT_DEBUG
  printf( "ekf: updating system\n" );
#endif

  apply_measurement( x_pre, z_estimate ); /* z_estimate = h ( x_pre ) */
  vecSub( z, z_estimate, z_estimate, MEAS_SIZE ); /* z_estimate = z - z_estimate */
  matMultVector( K, z_estimate, x_post, STATE_SIZE, MEAS_SIZE ); /* x_post = K * (z- z_estimate) */
  vecAdd( x_post, x_pre, x_post, STATE_SIZE ); /* x_post + x_pre + K * (z-z_estimate) */
}

/* estimate_prob()
   This function estimates the change in the variance of the state
   variables, given the system transfer function.   */

static void estimate_prob( uFloat **P_post, uFloat **Phi, uFloat **Qk,
			  uFloat **P_pre )
{
#ifdef PRINT_DEBUG
  printf( "ekf: estimating prob\n" );
#endif

  matMultTranspose( P_post, Phi, temp_state_state,
		     STATE_SIZE, STATE_SIZE, STATE_SIZE ); /* temp_state_state = P_post * Phi' */
  matMult( Phi, temp_state_state, P_pre,
		     STATE_SIZE, STATE_SIZE, STATE_SIZE ); /* P_pre = Phi * P_post * Phi' */
  matAdd( P_pre, Qk, P_pre, STATE_SIZE, STATE_SIZE );   /* P_pre = Qk + Phi * P_post * Phi' */
}


/*  update_prob()
    This function updates the state variable variances.
    Inputs:
    P_pre - the apriori probability matrix ( state x state )
    R     - measurement noise covariance   ( meas x meas )
    H     - the measurement transfer matrix ( meas x state )
    Outputs:
    P_post - the aposteriori probability matrix (state x state )
    K     - the Kalman gain matrix ( state x meas )
*/

static void update_prob( uFloat **P_pre, uFloat **R, uFloat **H,
			uFloat **P_post, uFloat **K )
{
#ifdef PRINT_DEBUG
  printf( "ekf: updating prob\n" );
#endif
#ifdef DIV_DEBUG
  printMatrix( "P", P_pre, STATE_SIZE, STATE_SIZE );
#endif

#ifdef DIV_DEBUG
  printMatrix( "H", H, MEAS_SIZE, STATE_SIZE );
#endif

#ifdef DIV_DEBUG
  printMatrix( "R", R, MEAS_SIZE, MEAS_SIZE );
#endif


  matMultTranspose( P_pre, H, temp_state_meas,
		     STATE_SIZE, STATE_SIZE, MEAS_SIZE ); /* temp_state_meas = P_pre*H' */
  matMult( H, temp_state_meas, temp_meas_meas,
	   MEAS_SIZE, STATE_SIZE, MEAS_SIZE );  /* temp_meas_meas = (H*P_pre)*H' */


  matAdd( temp_meas_meas, R, temp_meas_meas,
	  MEAS_SIZE, MEAS_SIZE );  

  take_inverse( temp_meas_meas, temp_meas_2, MEAS_SIZE ); /* temp_meas_2 = inv( H*P_pre*H' + R) */

#ifdef DIV_DEBUG
  printMatrix( "1 / (HPH + R)", temp_meas_2,
	       MEAS_SIZE, MEAS_SIZE );
#endif

  matMult( temp_state_meas, temp_meas_2, K,
		     STATE_SIZE, MEAS_SIZE, MEAS_SIZE ); /* K = P_pre * H' * (inv( H*P_pre*H' + R))' */

#ifdef DIV_DEBUG
  printMatrix( "Kalman Gain", K, STATE_SIZE, MEAS_SIZE );
#endif

  matMult( H, P_pre, temp_meas_state,
	   MEAS_SIZE, STATE_SIZE, STATE_SIZE );  /* temp_meas_state = H * P_pre  */

  matMult( K, temp_meas_state, temp_state_state,
	   STATE_SIZE, MEAS_SIZE, STATE_SIZE );  /* temp_state_state = K*H*P_pre */

#ifdef PRINT_DEBUG
  printf( "ekf: updating prob 3\n" );
#endif
  matSub(  P_pre, temp_state_state, P_post, STATE_SIZE, STATE_SIZE ); /* P_post = P_pre - K*H*P_pre */

#ifdef DIV_DEBUG
  printMatrix( "New P_post", P_post,
	       STATE_SIZE, STATE_SIZE );
#endif
}


static void take_inverse( uFloat **in, uFloat **out, int n )
{
#ifdef PRINT_DEBUG
  printf( "ekf: calculating inverse\n" );
#endif
  /*  Nothing fancy for now, just a Gauss-Jordan technique,
      with good pivoting (thanks to NR).     */

  gjInverse( in, n, out, 0 );  /* out is SCRATCH  */
  matCopy( in, out, n, n );
}


