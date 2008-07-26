/******Swarm Defines include file *********/
#ifndef SWARM_DEFINES_H
#define SWARM_DEFINES_H 1

#define VERBOSE 1 //set to 1 for increased debugging messages
#define PI 3.14159265359

#define MAX_BUFF_SZ 1024 //Serial port read/write buffer size
#define SWARM_SUCCESS 0
#define SWARM_SERIAL_WRITE_ERR 10 //failed to write data to serial port
#define SWARM_OUT_OF_MEMORY_ERROR 11 //failed to malloc some memory

#define MAX_GPS_SENTENCE_SZ  256

#define MAX_LOG_ENTRY_SZ 1024
#define MAX_NUM_LOG_FILES 5      //the maximum number of log files allowed
#define MAX_LOG_FILE_SZ 262144

#define MAX_LOG_FILE_NAME_SZ 512

#define LOG_FILE_BASE_NAME "swarm_log_"
#define DEFAULT_LOG_PATH "./logs"

//Constants for converting from lat/lon to UTM
#define WGS84_EQUATORIAL_RADIUS_METERS 6387137
#define WGS84_ECCENTRICITY_SQUARED 0.00669438

// defines for use with the AD Converters
#define SONAR_CHANNEL 2
#define SONAR_MAX_VOLTAGE 5.0
#define SONAR_SAMPLE_PRECISION 2

#define AD_DEFAULT_MAX_VOLTAGE 5.0 // the default max voltage that will be expected on the AD converter input pins. For differing voltages, we should put specialized code in getAdConverterStatus() in adconverter.cc
                                   // (for instance, on the line monitoring the batteries)
#define AD_DEFAULT_PRECISION 2     // when reading multiple values from the AD converter, not pausing between reads produces errors. a value of 1 will not pause, anything greater than 1 will pause

#define BATTERY_CHANNEL 3
#define BATTERY_MAX_VOLTAGE 26

#define GRONKULATOR_FREQ_IN_MILLIS 90 //10 Hz

/************************Constants**************************************/
/*
const double PI = 3.14159265;
const double FOURTHPI = PI / 4;
const double deg2rad = PI / 180;
const double rad2deg = 180.0 / PI;
*/
/*************************STRUCTURES************************************/
typedef struct	swarmGpsDataStruct
{
  char gpsSentenceType[32];
  char ggaSentence[MAX_GPS_SENTENCE_SZ];
  char vtgSentence[MAX_GPS_SENTENCE_SZ];
  char nmea_utctime[64];
  double nmea_latddmm;
  double nmea_londdmm;
  char nmea_latsector;
  char nmea_lonsector;
  double latdd;
  double londd;
  double utcTime;
  double metFromMshipNorth;
  double metFromMshipEast;
  double utcTimeMship;
  double UTMNorthing;
  double UTMEasting;
  char UTMZone[32];
  float nmea_course; //heading radians respect to due east
  float speed; //meters per second
  char mode;
} swarmGpsData;

struct	swarmImuData
{
  /* IMU data converted to SI units */
  double si_ratex;
  double si_ratez;
  double si_accx;
  double si_accy;
  double si_accz;

  double omega;

  /* raw data from IMU */
  int int_ratex;
  int int_ratez;
  int int_accx;
  int int_accy;
  int int_accz;
  int int_vref; 	     /* this is vref on the IMU. Should not change */
  int int_adc0;		     /* this is the steering pot raw value */
  int int_adc1;		     /* this is the current sensor raw value */


  /* integer bias (what we get when we measure a zero reading */
  /* subtract these values from the raw int vals to get a zero-mean value*/
  /* [even though these are doubles, their units are still raw (NOT SI!)] */
  double ratex_bias;
  double ratez_bias;
  double accx_bias;
  double accy_bias;
  double accz_bias;
  double vref_bias;
  double adc0_bias;
  double adc1_bias;

  /* these hold the descriptive strings scanned by the parser */
  /* useful for debug */
  char adc0_str[10];
  char adc1_str[10];
  char ratex_str[10];
  char ratez_str[10];
  char accx_str[10];
  char accy_str[10];
  char accz_str[10];
  char vref_str[10];
};

// store motor data; I think we only care about current & odometer
// but get them anyway
struct	swarmMotorData
{

  /* steering motor data */
  int steerTarget;		/* target steering angle for PID */
  int steerActual; 		/* measured steering angle (arbitrary units) */
  int steerPWM;			/* what we're sending to the steering motor */

  /* drive motor data */
  int driveTarget;		/* target speed for PID loop */
  int driveActual;		/* actual measured speed */
  int drivePWM;			/* what we are sending to the motor control */
  int odometer;		/* odometer count (will roll over) */
  int rawCurrent;		/* raw current measure */
  double speedRPS;		/* speed in radians per seconds */

  /* strings for debug */
  char steerTarget_str[10];
  char steerActual_str[10];
  char steerPWM_str[10];

  char driveTarget_str[10];
  char driveActual_str[10];
  char drivePWM_str[10];
  char odometer_str[10];
  char rawCurrent_str[10];

};

struct	swarmAdcData
{
  /* IMU data converted to SI units */
  double adcCH0;
  double adcCH1;
  double adcCH2;
  double adcCH3;
  double adcCH4;
};

struct  swarmStateEstimate
{
  double vdot;
  double v;
  double phidot;
  double phi;
  double psi;
  double theta;
  double x;
  double y;
  double xab;
  double yab;
  double zab;
  double xrb;
  double zrb;
};

struct swarmFeedback
{
  double vDes;
  double deltaDes;
};

struct swarmPID
{
  double error;
  double lastError;
  double deadBand;
  double Kp;
  double Ki;
  double Kd;
  double minDrive;
  double maxDrive;
  double iSum;
  double iLimit;
  double debugOutput; // flag for outputing PID tuning info
};

struct spuADConverterStatus {
	double ad_vals[5];
	double sonar; // this is in inches
	double battery_voltage;
};

extern swarmGpsData *latestGpsCordinates;

#endif
