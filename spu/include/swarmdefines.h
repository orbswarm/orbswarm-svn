/******Swarm Defines include file *********/
#ifndef SWARM_DEFINES_H
#define SWARM_DEFINES_H 1

#define MAX_BUFF_SZ 512 //Serial port read/write buffer size
#define SWARM_SUCCESS 0
#define SWARM_SERIAL_WRITE_ERR 10 //failed to write data to serial port 
#define SWARM_OUT_OF_MEMORY_ERROR 11 //failed to malloc some memory 
#define SWARM_INVALID_GPS_SENTANCE 100 
#define SWARM_NMEA_GPS_SENTANCE_TYPE_GPGGA "GPGGA"
#define SWARM_NMEA_GPS_DATA_DELIM ","
#define MAX_GPS_SENTANCE_SZ 1024 


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

/*************************STRUCTURES************************************/
struct	swarmGpsData
{
  char gpsSentanceType[32]; 
  char gpsSentance[MAX_GPS_SENTANCE_SZ];
  char nmea_utctime[64];
  long double nmea_latddmm;
  long double nmea_londdmm;
  char nmea_latsector;
  char nmea_lonsector;
  long double latdd;
  long double londd;
  long double utcTime;
  long double metFromMshipX;
  long double metFromMshipY;
  long double utcTimeMship;
};

#endif
