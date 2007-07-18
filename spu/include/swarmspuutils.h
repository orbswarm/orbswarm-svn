#include<unistd.h>
#include<sys/types.h>
#include<sys/mman.h>
#include<stdio.h>
#include<fcntl.h>
#include<string.h>
#include <sys/stat.h>			/* declare the 'stat' structure	*/
#include <time.h>
#include <errno.h>
#include "swarmdefines.h"
#include <stdlib.h>

/* This is a collection of general utility methods for working with the spu */
#ifndef SWARM_SPU_UTILS_H 
#define SWARM_SPU_UTILS_H 
/************************
 * Sets the indicated spu led state as defined in swarmdefines.h
 */
int toggleSpuLed(const unsigned int ledState);  
//void set_led(int led);
int resetOrbMCU(void);

//pass NULL for log dir to use default path defined in swarmdefines.h
int spulog(char* msg, int msgSize, char* logdir);

//Parses an NMEA data sentance into the seperate components needed for conversion
//Returns SWARM_SUCCESS on a successful parse and  SWARM_INVALID_GPS_SENTANCE if 
//the input string was garbage or an invalid type.  The parser only accepts NMEA
//sentances of type GPGGA 
int parseGPSSentance(swarmGpsData * gpsdata);

//converts the raw NMEA gps lat long data into decimal lat long data
int convertNMEAGpsLatLonDataToDecLatLon(swarmGpsData * gpsdata);

#endif
