/******Swarm Defines include file *********/
#ifndef SWARM_DEFINES_H
#define SWARM_DEFINES_H 1

#define MAX_BUFF_SZ 1024 //Serial port read/write buffer size
#define SWARM_SUCCESS 0
#define SWARM_SERIAL_WRITE_ERR 10 //failed to write data to serial port 
#define SWARM_OUT_OF_MEMORY_ERROR 11 //failed to malloc some memory 
#define SWARM_INVALID_GPS_SENTENCE 100 
#define SWARM_NMEA_GPS_SENTENCE_TYPE_GPGGA "GPGGA"
#define SWARM_NMEA_GPS_SENTENCE_TYPE_GPVTG "GPVTG"
#define SWARM_NMEA_GPS_DATA_DELIM ","
#define MAX_GPS_SENTENCE_SZ 1024 


#define AGGR_MSG_TYPE_UNKNOWN 200 
#define AGGR_MSG_TYPE_MOTHER_SHIP_SPU_POLL 201 
#define AGGR_MSG_TYPE_TRAJECTORY 202 
#define AGGR_MSG_TYPE_MOTHER_SHIP_LOC 203 
#define AGGR_MSG_TYPE_EFFECTS 204 
#define AGGR_MSG_TYPE_MOTOR_CONTROL 205 

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

/**********************************MOTHER SHIP MESSAGE HEADERS**********************************/
#define MSG_HEAD_MOTOR_CONTROLER '$'
#define MSG_END_MOTOR_CONTROLER '*'
#define MSG_HEAD_LIGHTING '<'          //used to identify lighting/sound messages
#define MSG_END_LIGHTING '>'          //used to identify lighting/sound messages
#define MSG_HEAD_MOTHER_SHIP '{'       //used to supply the orb with info about the mothership
#define MSG_END_MOTHER_SHIP '}'       //used to supply the orb with info about the mothership
                                       //E.G. the location of the mothership in UTM format

#define AGGR_DATA_XFER_ACK '!'  //found at the end of a data stream from the Agg 
                                       //Note: a data stream may contain >1 messages
#define AGGR_MESSAGE_DELIM_END '\n' 

#define AGGR_GPS_QUERY_CMD "$Ag*$"
#define AGGR_ZIGBEE_QUERY_CMD "$Az*$"

#define AGG_GPS_START_DELIM '$'
#define AGG_GPS_STOP_DELIM '\n'

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
  double metFromMshipX;
  double metFromMshipY;
  double utcTimeMship;
  double UTMNorthing;
  double UTMEasting;
  char UTMZone[32];
  float nmea_course;
  float speed;
  char mode;
};
#endif
