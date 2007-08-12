
// ---------------------------------------------------------------------
// 
//	File:  swarmGPSutils.h 
//      SWARM Orb SPU code http://www.orbswarm.com
//	: prototypes for GPS parsing routines 
//
//
//      
//
//	Written by Matthew Cline and Jessie, optional email
// refactored from swarmsputils.c  by Jon Foote (Head Rotor ar rotorbrain.com)
// -----------------------------------------------------------------------

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
#include <math.h>

//Parse an NMEA data sentence into the seperate components needed for conversion
//Returns SWARM_SUCCESS on a successful parse and  SWARM_INVALID_GPS_SENTENCE if 
//the input string was garbage or an invalid type.  The parser only accepts NMEA
//sentences of type GPGGA 
int parseGPSSentence(swarmGpsData * gpsdata);

//converts the raw NMEA gps lat long data into decimal lat long data
int convertNMEAGpsLatLonDataToDecLatLon(swarmGpsData * gpsdata);

int parseGPSVtgSentance(swarmGpsData * gpsdata);

int parseRawAggregatorGPSData(char* rawGPS, swarmGpsData * gpsdata); 

//converts decimal lat/lon to UTM 
//East Longitudes are positive, West longitudes are negative. 
//North latitudes are positive, South latitudes are negative
//Lat and Long are in decimal degrees
void decimalLatLongtoUTM(const double ref_equ_radius, const double ref_ecc_squared, swarmGpsData * gpsdata);

