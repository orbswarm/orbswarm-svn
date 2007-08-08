/*  kalman.c

    This file contains the code for a kalman filter, an
    extended kalman filter, and an iterated extended kalman filter.

    For ready extensibility, the apply_measurement() and apply_system()
    functions are located in a separate file: kalman_cam.c is an example.

    It uses the matmath functions provided in an accompanying file
    to perform matrix and quaternion manipulation.


    J. Watlington, 11/15/95

    Modified:
    11/30/95  wad  The extended kalman filter section seems to be
                   working now.
*/

#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include "kalman.h"
#include "kalmanswarm.h"

#define ITERATION_THRESHOLD      2.0
#define ITERATION_DIVERGENCE     20

#define PERIOD  0.10

#define PRINT_DEBUG 

/*  The following are the global variables of the Kalman filters,
    used to point to data structures used throughout.     */

static m_elem  *state_pre;         /* ptr to apriori state vectors, x(-)     */
static m_elem  *state_post;        /* ptr to aposteriori state vectors, x(+) */

static m_elem  **cov_pre;          /* ptr to apriori covariance matrix, P(-) */
static m_elem  **cov_post;         /* ptr to apriori covariance matrix, P(-) */
static m_elem  **sys_noise_cov;    /* system noise covariance matrix (GQGt)  */
static m_elem  **mea_noise_cov;    /* measurement noise variance vector (R)  */

static m_elem  **sys_transfer;     /* system transfer function (Phi)    */
static m_elem  **mea_transfer;     /* measurement transfer function (H) */

static m_elem  **kalman_gain;      /* The Kalman Gain matrix (K) */

int            global_step = 0;    /* the current step number (k) */


/*  Temporary variables, declared statically to avoid lots of run-time
    memory allocation.      */

static m_elem  *z_estimate;        /* a measurement_size x 1 vector */
static m_elem  **temp_state_state; /* a state_size x state_size matrix */
static m_elem  **temp_meas_state;  /* a measurement_size x state_size matrix */
static m_elem  **temp_meas_meas;   /* a measurement_size squared matrix */
static m_elem  **temp_meas_2;      /* another one ! */

/* variables for Runge-Kutta */
static m_elem  *k1;        /* a state_size x 1 vector */
static m_elem  *k2;        /* a state_size x 1 vector */
static m_elem  *k3;        /* a state_size x 1 vector */
static m_elem  *k4;        /* a state_size x 1 vector */

static m_elem  *tempState;        /* a state_size x 1 vector */

/*  Prototypes of internal functions  */

static void rungeKutta( m_elem *old_state, m_elem *new_state );
 
static void generate_system_transfer( m_elem *state, m_elem **phi );

static void alloc_globals( int num_state,
			  int num_measurement );
static void update_system( m_elem *z, m_elem *x_minus,
			 m_elem **kalman_gain, m_elem *x_plus );
static void estimate_prob( m_elem **P_post, m_elem **Phi, m_elem **GQGt,
			  m_elem **P_pre );
static void update_prob( m_elem **P_pre, m_elem **R, m_elem **H,
			m_elem **P_post, m_elem **K );
static void take_inverse( m_elem **in, m_elem **out, int n );




/******************************************************************

  Non-linear Kalman Filtering

  extended_kalman_init()
  This function initializes the extended kalman filter.
*/

void extended_kalman_init( m_elem **P, m_elem *x )
{
#ifdef PRINT_DEBUG
  printf( "ekf: Initializing filter\n" );
#endif

  alloc_globals( STATE_SIZE, MEAS_SIZE );

  sys_transfer = matrix( 1, STATE_SIZE, 1, STATE_SIZE );
  mea_transfer = matrix( 1, MEAS_SIZE, 1, MEAS_SIZE );

  /*  Init the global variables using the arguments.  */

  vec_copy( x, state_post, STATE_SIZE );
  vec_copy( x, state_pre, STATE_SIZE );
  mat_copy( P, cov_post, STATE_SIZE, STATE_SIZE );
  mat_copy( P, cov_pre, STATE_SIZE, STATE_SIZE );

  covarianceSet( sys_noise_cov, mea_noise_cov );	

#ifdef PRINT_DEBUG
  printf( "ekf: Completed Initialization\n" );
#endif

}


/*  extended_kalman_step()
    This function takes a set of measurements, and performs a single
    recursion of the extended kalman filter.
*/

void extended_kalman_step( m_elem *z_in )
{
#ifdef PRINT_DEBUG
  printf( "ekf: step %d\n", global_step );
#endif

#ifdef PRINT_DEBUG
	print_vector( "measurements ", z_in, MEAS_SIZE );
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


m_elem *kalman_get_state( void )
{
  return( state_post );
}

static void rungeKutta( m_elem *old_state, m_elem *new_state )
{

#ifdef PRINT_DEBUG
  printf( "ekf: rungeKutta\n" );
#endif

  systemF( tempState, old_state );

  vecScalarMult( tempState, PERIOD, k1, STATE_SIZE );

  /* k2 = period * F( old_state + k1 * 0.5) */
  vecScalarMult( k1, (double)0.5, tempState, STATE_SIZE ); /* tempState = k1 * 0.5 */
  vec_add( old_state, tempState, tempState, STATE_SIZE );  /* tempState = old_state + k1 * 0.5 */
  systemF( tempState, old_state );
  vecScalarMult( tempState, PERIOD, k2, STATE_SIZE );

  /* k3 = period * F( old_state + k2 * 0.5) */
  vecScalarMult( k2, (double)0.5, tempState, STATE_SIZE ); /* tempState = k2 * 0.5 */
  vec_add( old_state, tempState, tempState, STATE_SIZE );  /* tempState = old_state + k2 * 0.5 */
  systemF( tempState, old_state );
  vecScalarMult( tempState, PERIOD, k3, STATE_SIZE );
  
  /* k4 = period * F( old_state + k3) */
  vec_add( old_state, tempState, tempState, STATE_SIZE );  /* tempState = old_state + k3 */
  systemF( tempState, old_state );
  vecScalarMult( tempState, PERIOD, k4, STATE_SIZE );

  /*  new_state = old_state + 0.16667 * ( k1 + 2*k2 + 2*k3 + k4 ) */
  vec_copy( k1, new_state, STATE_SIZE );
  vecScalarMult( k2, (double)2.0, tempState, STATE_SIZE );
  vec_add( new_state, tempState, new_state, STATE_SIZE);
  vecScalarMult( k3, (double)2.0, tempState, STATE_SIZE );
  vec_add( new_state, tempState, new_state, STATE_SIZE);
  vec_add( new_state, k4, new_state, STATE_SIZE);
  vecScalarMult( new_state, (double)0.16667, new_state, STATE_SIZE );
  vec_add( new_state, old_state, new_state, STATE_SIZE);

}

/*  generate_system_transfer
    This function generates the pseudo-transfer
    function, representing the first terms of a taylor expansion
    of the actual non-linear system transfer function.     */

void generate_system_transfer( m_elem *state, m_elem **phi )
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

  mat_mult_scalar( temp_state_state, PERIOD, temp_state_state,
	      STATE_SIZE, STATE_SIZE );	

  mat_add( phi, temp_state_state, phi, STATE_SIZE, STATE_SIZE );  	

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

static void update_system( m_elem *z, m_elem *x_pre,
			 m_elem **K, m_elem *x_post )
{
#ifdef PRINT_DEBUG
  printf( "ekf: updating system\n" );
#endif

  apply_measurement( x_pre, z_estimate );
  vec_sub( z, z_estimate, z_estimate, MEAS_SIZE );
  mat_mult_vector( K, z_estimate, x_post, STATE_SIZE, MEAS_SIZE );
  vec_add( x_post, x_pre, x_post, STATE_SIZE );
}

/* estimate_prob()
   This function estimates the change in the variance of the state
   variables, given the system transfer function.   */

static void estimate_prob( m_elem **P_post, m_elem **Phi, m_elem **GQGt,
			  m_elem **P_pre )
{
#ifdef PRINT_DEBUG
  printf( "ekf: estimating prob\n" );
#endif

  mat_mult_transpose( P_post, Phi, temp_state_state,
		     STATE_SIZE, STATE_SIZE, STATE_SIZE );
  mat_mult( Phi, temp_state_state, P_pre,
		     STATE_SIZE, STATE_SIZE, STATE_SIZE );
  mat_add( P_pre, GQGt, P_pre, STATE_SIZE, STATE_SIZE );
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

static void update_prob( m_elem **P_pre, m_elem **R, m_elem **H,
			m_elem **P_post, m_elem **K )
{
#ifdef PRINT_DEBUG
  printf( "ekf: updating prob\n" );
#endif
#ifdef DIV_DEBUG
  print_matrix( "P", P_pre, STATE_SIZE, STATE_SIZE );
#endif
  mat_mult( H, P_pre, temp_meas_state,
	   MEAS_SIZE, STATE_SIZE, STATE_SIZE );
  mat_mult_transpose( H, temp_meas_state, temp_meas_meas,
		     MEAS_SIZE, STATE_SIZE, MEAS_SIZE );
  mat_add( temp_meas_meas, R, temp_meas_meas,
	  MEAS_SIZE, MEAS_SIZE );

  take_inverse( temp_meas_meas, temp_meas_2, MEAS_SIZE );

#ifdef DIV_DEBUG
  print_matrix( "1 / (HPH + R)", temp_meas_2,
	       MEAS_SIZE, MEAS_SIZE );
#endif
  mat_transpose_mult( temp_meas_state, temp_meas_2, K,
		     STATE_SIZE, MEAS_SIZE, MEAS_SIZE );

/*
  print_matrix( "Kalman Gain", K, STATE_SIZE, MEAS_SIZE );
*/
  mat_mult( K, temp_meas_state, temp_state_state,
	   STATE_SIZE, MEAS_SIZE, STATE_SIZE );
#ifdef PRINT_DEBUG
  printf( "ekf: updating prob 3\n" );
#endif
  mat_add( temp_state_state, P_pre, P_post, STATE_SIZE, STATE_SIZE );
}


static void take_inverse( m_elem **in, m_elem **out, int n )
{
#ifdef PRINT_DEBUG
  printf( "ekf: calculating inverse\n" );
#endif
  /*  Nothing fancy for now, just a Gauss-Jordan technique,
      with good pivoting (thanks to NR).     */

  gaussj( in, n, out, 0 );  /* out is SCRATCH  */
  mat_copy( in, out, n, n );
}


