/*  test.c

    This file contains the code wrapper for processing data using the
    Kalman filter routines provided in kalman.c and kalman_camera.c.

    Usage:  test <meass_fname> <num_samples>
                  [-o <output_fname>][-debug]

    J. Watlington, 11/27/95

    Modified:
    12/4/95  Added ekf/iekf and monte_carlo flags, cause I got this sucker
             to converge !  The measurement linearization was the culprit.
    12/9/95  Added initial state estimation, using an iterative Levenberg-
             Marquardt minimization technique applied alternatively to the
	     camera parameters and the meas point location.
*/

#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include "kalman.h"
#include "kalmanswarm.h"
#include "matmath.h"


#define HELLO "Camera Motion Estimator\n"

#define IMPROVE_DEBUG

/*  Default filename suffixes.  A . isn't used so MatLab can load results */

#define COVARIANCE_SUFFIX   "_cov"
#define OUTPUT_SUFFIX       "_out"

/*  Global variables reflecting command line options  */

int   debug;
char  meas_fname[ FILENAME_MAX ];
char  output_fname[ FILENAME_MAX ];
int   meas_size;
int   num_samples;



/*   Random Number Generator state variable   */

extern float gasdev( long *idum );  /* random # gen, in random.c */
long  rseed = -1;

char  dbgstr[ 64 ];

/*  Prototypes of local functions  */
void load_meas( char *name, int num_meas, int num_steps, m_elem **meas );
void save_track( char *name, int num_steps, m_elem **trajectory );
void parse_arguments( int argc, char **argv );
void usage( char *str );


int main( int argc, char **argv )
{
  int      trial, time, i, row, col; /* various iteration variables   */
  m_elem   *track;        /* ptr to state vector of kalman filter */

  m_elem   **meas;    /* features being tracked        */

  m_elem   **trajectory;  /* mean of extracted motion parameters   */

  m_elem   *traj_fptr;    /* ptr to single row of ext. motion parameters   */

                    /*  n = meas_size   m = STATE_SIZE  */
  m_elem   **P;     /*  Estimate Covariance        (mxm)   */
  m_elem   *x;      /*  Starting state             (1xm)   */

  debug = 1;	

  parse_arguments( argc, argv );   /*  Parse the command line arguments  */

  /*  Allocate memory for system, measurement, and state variables.
      Then either init them algorithmically or from data stored in a file.  */

  P = matrix( 1, STATE_SIZE, 1, STATE_SIZE );
  x = vector( 1, STATE_SIZE );
  meas = matrix( 1, num_samples, 1, STATE_SIZE );
  trajectory = matrix( 1, num_samples, 1, STATE_SIZE );

  load_meas( meas_fname, MEAS_SIZE, num_samples, meas );


  for( row = 1; row <= STATE_SIZE; row++ )
    for( col = 1; col <= STATE_SIZE; col++ )
      P[ row ][ col ] = 0.0;

  for( row = 1; row <= STATE_SIZE; row++ )
      x[ row ] = 0.0;

  extended_kalman_init( P, x );
    
      /*  For each sample in the test run, perform one estimation and
	  copy the results into the trajectory history   */
      
  start_clock();	

      for( time = 1; time <= num_samples; time++ )
	{

	  extended_kalman_step( &meas[ time ][0] );

	  track = kalman_get_state();
	  if( debug )
	    {
	      sprintf( dbgstr, "State @ t = %d", time );
	      print_vector( dbgstr, track, STATE_SIZE );
	    }
  traj_fptr = trajectory[ time ];

	  for( i = 1; i <= STATE_SIZE; i++ )
	    traj_fptr[ i ] = track[ i ];
	}

  end_clock("All samples processed in this many ticks -");
    
  /*  Save the set of estimated parameters into a file  */
  
  save_track( output_fname, num_samples, trajectory );

  free_matrix( P, 1, STATE_SIZE, 1, STATE_SIZE );
  free_vector( x, 1, STATE_SIZE );
  free_matrix( meas, 1, num_samples, 1, meas_size );
  free_matrix( trajectory, 1, num_samples, 1, STATE_SIZE );

}

void load_meas( char *name, int num_meas, int num_steps,
		   m_elem **meas )
{
  FILE    *fptr;
  int     i;
  int     sample;
  m_elem  data[ MEAS_SIZE + 1 ];

  if( debug )
    printf( "Loading meas from %s\n", name );

  if( (fptr = fopen( name, "r" )) == NULL )
    {
      printf( "load_meas: Unable to open %s for reading !\n",
	     name );
      exit( -1 );
    }


    for( sample = 1; sample <= num_steps; sample ++ )
      {
	if( fscanf( fptr, " %lf %lf %lf %lf %lf %lf %lf %lf %lf %lf", 
		&data[1], &data[2], &data[3], &data[4], &data[5], &data[6], 
		&data[7], &data[8], &data[9], &data[10] ) != 10 )
	  {
	    printf("load_meas: Error reading %s ! (%d samples %d points read )\n",
		   name, sample );
	    fclose( fptr );
	    exit( -1 );
	  }
	
	for ( i = 1; i <= MEAS_SIZE; i++)
	meas[ sample ][ i ] = (m_elem)data[ i ];
      }

  fclose( fptr );
}

void save_track( char *name, int num_steps, m_elem **trajectory )
{
  FILE     *fptr;
  int      sample, state_var;

  if( (fptr = fopen( name, "w" )) == NULL )
    {
      printf( "save_track: Unable to open %s for writing !\n", name );
      exit( -1 );
    }

  for( sample = 1; sample <= num_steps; sample++ )
    {
      for( state_var = 1; state_var <= STATE_SIZE; state_var++ )
	fprintf( fptr, "%lf ", (double)trajectory[ sample ][ state_var ] );
      fprintf( fptr, "\n" );
    }

  fclose( fptr );
}


void parse_arguments( int argc, char **argv )
{
  int   i;

  printf( HELLO );

  if( argc < 2 )
    usage( "Not enough arguments" );
  else
    {
      /* Parse the three required arguments, then calculate some
	 default values for file names, in case the user decides not
	 to provide them.  */

      sscanf( argv[1], "%s", meas_fname );
      sscanf( argv[2], "%d", &num_samples );
      sprintf( output_fname, "%s%s", meas_fname, OUTPUT_SUFFIX );

      /*  If more than the required number of arguments was provided,
	  take a look and see if any of them make sense.   */

      i = 4;
      while( i < argc )
	{
	  if( argv[i][0] = '-' )
	    {
	      switch( argv[i][1] ) {
	      case 'o':      /* -o <output_fname>  */
		if( ++i >= argc )
		  usage( "-o <fname> missing argument" );
		sscanf( argv[i], "%s", output_fname );
		break;
	      default:
		usage( "unknown argument" );
	      }
	    }
	  else
	    {
	      usage( "syntax error" );
	    }
	  i++;
	}
    }

      printf( "Implementing an Extended Kalman Filter\n" );

}

void usage( char *str )
{
  printf( "test: %s\n", str );
  printf( "usage: test <meas_fname><num_samples>\n" );
  printf( "\t[ -o <output_fname> ][ -debug ]\n" );
  exit( -1 );
}
