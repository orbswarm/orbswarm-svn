/*  test.c

    This file contains the code wrapper for processing data using the
    Kalman filter routines provided in kalman.c and kalmanswarm.c.

    Usage:  test <meass_fname> <num_samples>
                  [-o <output_fname>][-debug]

*/

#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include "kalman.h"
#include "kalmanswarm.h"
#include "matmath.h"
#include "kftime.h"
#include "../dispatcher/swarmdefines.h"

#define HELLO "SWARM EKF\n"

#define IMPROVE_DEBUG

/*  Default filename suffixes.  A . isn't used so MatLab can load results */

#define COVARIANCE_SUFFIX   "_cov"
#define OUTPUT_SUFFIX       "_out"

/*  Global variables reflecting command line options  */

int   debug;
char  meas_fname[ FILENAME_MAX ];
char  output_fname[ FILENAME_MAX ];
int   num_samples;


char  dbgstr[ 64 ];

/*  Prototypes of local functions  */
void load_meas( char *name, int num_meas, int num_steps, uFloat **meas );
void save_track( char *name, int num_steps, uFloat **trajectory );
void parse_arguments( int argc, char **argv );
void usage( char *str );


int main( int argc, char **argv )
{
  int      time, i, row, col; /* various iteration variables   */
  uFloat   *track;        /* ptr to state vector of kalman filter */

  uFloat   **meas;    /* features being tracked        */

  uFloat   **trajectory;  /* mean of extracted motion parameters   */

  uFloat   *traj_fptr;    /* ptr to single row of ext. motion parameters   */

                    /*  n = meas_size   m = STATE_SIZE  */
  uFloat   **P;     /*  Estimate Covariance        (mxm)   */
  uFloat   *x;      /*  Starting state             (1xm)   */

  struct swarmStateEstimate stateEstimate;

  struct swarmImuData imuData;

  struct swarmGpsDataStruct gpsData;

  struct swarmMotorData motorData;

  zeroStateEstimates( &stateEstimate );

  debug = 1;

  parse_arguments( argc, argv );   /*  Parse the command line arguments  */

  /*  Allocate memory for system, measurement, and state variables.
      Then either init them algorithmically or from data stored in a file.  */

  P = matrix( 1, STATE_SIZE, 1, STATE_SIZE );
  x = vector( 1, STATE_SIZE );
  meas = matrix( 1, num_samples, 1, MEAS_SIZE );
  trajectory = matrix( 1, num_samples, 1, STATE_SIZE );

  load_meas( meas_fname, MEAS_SIZE, num_samples, meas );

  kalmanInit( &stateEstimate );

      /*  For each sample in the test run, perform one estimation and
	  copy the results into the trajectory history   */

  start_clock();
      for( time = 1; time <= num_samples; time++ )
	{
/*
  		imuData.si_ratex 	= meas[time][MEAS_xr];
  		imuData.si_ratez 	= meas[time][MEAS_zr];
  		imuData.si_accx  	= meas[time][MEAS_xa];
  		imuData.si_accy  	= meas[time][MEAS_ya];
  		imuData.si_accz  	= meas[time][MEAS_za];*/

  		imuData.si_ratex 	= meas[time][MEAS_xr];
  		imuData.si_ratez 	= meas[time][MEAS_zr];
  		imuData.si_accx  	= meas[time][MEAS_xa];
  		imuData.si_accy  	= meas[time][MEAS_ya];
  		imuData.si_accz  	= meas[time][MEAS_za];
  		imuData.si_yawRate  = meas[time][MEAS_yaw];

		// motorData.speedRPS 	= meas[time][MEAS_omega];

		imuData.omega		= meas[time][MEAS_omega];

		gpsData.metFromMshipNorth = meas[time][MEAS_yg];
		gpsData.metFromMshipEast  = meas[time][MEAS_xg];
		gpsData.nmea_course 	  = meas[time][MEAS_psig];
		gpsData.speed 		  = meas[time][MEAS_vg];

		kalmanProcess( &gpsData, &imuData, &stateEstimate);

	  track = kalman_get_state();

	  if( debug )
	    {
	      sprintf( dbgstr, "State @ t = %d", time );
	      /*printVector( dbgstr, track, STATE_SIZE );*/
	    }
  traj_fptr = trajectory[ time ];

	  for( i = 1; i <= STATE_SIZE; i++ )
	    traj_fptr[ i ] = track[ i ];
	}

  end_clock("All samples processed in this many ticks -");

  /*  Save the set of estimated parameters into a file  */

  save_track( output_fname, num_samples, trajectory );

  freeMatrix( P, 1, STATE_SIZE, 1, STATE_SIZE );
  freeVector( x, 1, STATE_SIZE );
  freeMatrix( meas, 1, num_samples, 1, MEAS_SIZE );
  freeMatrix( trajectory, 1, num_samples, 1, STATE_SIZE );

  return(0);

}

void load_meas( char *name, int num_meas, int num_steps,
		   uFloat **meas )
{
  FILE    *fptr;
  int     i;
  int     sample;
  uFloat  data[ MEAS_SIZE + 1 ];

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
	if( fscanf( fptr, " %lf %lf %lf %lf %lf %lf %lf %lf %lf %lf %lf",
		&data[1], &data[2], &data[3], &data[4], &data[5], &data[6],
		&data[7], &data[8], &data[9], &data[10], &data[11] ) != 11 )
	  {
	    printf("load_meas: Error reading %s ! (%d samples read )\n",
		   name, sample );
	    fclose( fptr );
	    exit( -1 );
	  }

	for ( i = 1; i <= MEAS_SIZE; i++)
	meas[ sample ][ i ] = (uFloat)data[ i ];
      }

  fclose( fptr );
}

void save_track( char *name, int num_steps, uFloat **trajectory )
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
	  if( argv[i][0] == '-' )
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
