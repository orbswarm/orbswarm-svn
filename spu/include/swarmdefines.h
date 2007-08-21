/******Swarm Defines include file *********/
#ifndef SWARM_DEFINES_H
#define SWARM_DEFINES_H 1

#define VERBOSE 1 //set to 1 for increased debugging messages

#define MAX_BUFF_SZ 1024 //Serial port read/write buffer size
#define SWARM_SUCCESS 0
#define SWARM_SERIAL_WRITE_ERR 10 //failed to write data to serial port 
#define SWARM_OUT_OF_MEMORY_ERROR 11 //failed to malloc some memory 
#define SWARM_INVALID_GPS_SENTENCE 100 
#define SWARM_NMEA_GPS_SENTENCE_TYPE_GPGGA "GPGGA"
#define SWARM_NMEA_GPS_SENTENCE_TYPE_GPVTG "GPVTG"
#define SWARM_NMEA_GPS_DATA_DELIM ","
#define MAX_GPS_SENTENCE_SZ 1024 
#define SWARM_INVALID_AGG_DATA 14
#define AGGR_MSG_FOOTER_WITHOUT_HEADER_ERROR 15
#define AGGR_MSG_TO_LARGE_FOR_BUFFER_ERROR 16
#define AGGR_MSG_INCOMPLETE_OR_INVALID 17
#define AGGR_MSG_HEADER_WITHOUT_FOOTER_ERROR 18
#define AGGR_MSG_NO_DELIMS_FOUND_ERROR 19


#define AGGR_MSG_TYPE_UNKNOWN 200 
#define AGGR_MSG_TYPE_MOTHER_SHIP_SPU_POLL 201 
#define AGGR_MSG_TYPE_TRAJECTORY 202 
#define AGGR_MSG_TYPE_MOTHER_SHIP_LOC 203 
#define AGGR_MSG_TYPE_EFFECTS 204 
#define AGGR_MSG_TYPE_MOTOR_CONTROL 205 
#define AGGR_MSG_TYPE_MOTHER_SHIP 206 

#define SPU_LED_RED_ON 40  
#define SPU_LED_GREEN_ON 41 
#define SPU_LED_BOTH_ON 42 
#define SPU_LED_BOTH_OFF 43  
#define SPU_LED_RED_OFF 44   
#define SPU_LED_GREEN_OFF 45 

#define MAX_LOG_ENTRY_SZ 1024 
#define MAX_NUM_LOG_FILES 5      //the maximum number of log files allowed
#define MAX_LOG_FILE_SZ 262144

#define MAX_LOG_FILE_NAME_SZ 512 

#define LOG_FILE_BASE_NAME "swarm_log_"
#define DEFAULT_LOG_PATH "./logs"

//Constants for converting from lat/lon to UTM
#define WGS84_EQUATORIAL_RADIUS_METERS 6387137
#define WGS84_ECCENTRICITY_SQUARED 0.00669438

/*********MOTHER SHIP MESSAGE HEADERS**********************************/
#define MSG_HEAD_MOTOR_CONTROLER '$'
#define MSG_END_MOTOR_CONTROLER '*'
#define MSG_HEAD_LIGHTING '<'   //used to identify lighting/sound messages
#define MSG_END_LIGHTING '>'    //used to identify lighting/sound messages
#define MSG_HEAD_MOTHER_SHIP '['//used to supply the orb with info about the mothership
#define MSG_END_MOTHER_SHIP ']' //used to supply the orb with info about the mothership
                                //E.G. the location of the mothership in UTM format
#define MSG_HEAD_AGG_STREAM '{'//used to supply the orb with info about the mothership
#define MSG_END_AGG_STREAM '}' //used to supply the orb with info about the mothership

#define MOTHER_SHIP_MSG_DELIM " " //blank space for newline
#define MOTHER_SHIP_MSG_HEAD_STATUS "DUMP_STATUS"
#define MOTHER_SHIP_MSG_HEAD_MOTOR_STATUS "MOTOR_STATUS"
#define MOTHER_SHIP_MSG_HEAD_TRAJECTORY "TRAJ"
#define MOTHER_SHIP_MSG_HEAD_LOCATION "MSLOC"
#define MOTHER_SHIP_MSG_LOW_VERBOCITY "LOW_VERBOCITY"   //will not echo back random motor controller commands
#define MOTHER_SHIP_MSG_HIGH_VERBOCITY "HIGH_VERBOCITY" //echos anything that the motor controller echos back to the
                                                        //mother ship  
#define AGGR_DATA_XFER_ACK '!'  //found at the end of a data stream from the Agg 
                                       //Note: a data stream may contain >1 messages
#define AGGR_MESSAGE_DELIM_END ";" 

#define AGGR_GPS_QUERY_CMD "$Ag*$"
#define AGGR_ZIGBEE_QUERY_CMD "$Ax*$"

#define MAX_AGG_PACKET_SZ 100   // total packet sz including the header footer 
#define MAX_AGG_PACKET_PAYLOAD_SZ 91   // This is 100 bytes - headersz + footersz + '\0' 
#define AGGR_ZIGBEE_STREAM_WRITE_HEADER "$As"
#define AGGR_ZIGBEE_STREAM_WRITE_END "*$"

#define AGG_GPS_START_DELIM '$'
#define AGG_GPS_STOP_DELIM '\n'

// defines for use with the AD Converters
#define SONAR_CHANNEL 2
#define SONAR_MAX_VOLTAGE 5.0
#define SONAR_SAMPLE_PRECISION 2

#define AD_DEFAULT_MAX_VOLTAGE 5.0 // the default max voltage that will be expected on the AD converter input pins. For differing voltages, we should put specialized code in getAdConverterStatus() in adconverter.cc
                                   // (for instance, on the line monitoring the batteries)
#define AD_DEFAULT_PRECISION 2     // when reading multiple values from the AD converter, not pausing between reads produces errors. a value of 1 will not pause, anything greater than 1 will pause

#define BATTERY_CHANNEL 3
#define BATTERY_MAX_VOLTAGE 26

/************************Constants**************************************/

const double PI = 3.14159265;
const double FOURTHPI = PI / 4;
const double deg2rad = PI / 180;
const double rad2deg = 180.0 / PI;

/*************************STRUCTURES************************************/
struct	swarmGpsData
{
  char gpsSentenceType[32]; 
  char gpsSentence[MAX_GPS_SENTENCE_SZ];
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
};

struct	swarmImuData
{
  /* IMU data converted to SI units */
  double si_ratex;
  double si_ratey;
  double si_accx;
  double si_accy;
  double si_accz;

  /* raw data from IMU */
  int int_ratex;
  int int_ratey;
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
  double ratey_bias;
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
  char ratey_str[10];
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

#endif
