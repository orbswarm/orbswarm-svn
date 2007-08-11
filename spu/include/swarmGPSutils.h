
// ---------------------------------------------------------------------
// 
//	File:  swarmGPSutils.h 
//      SWARM Orb SPU code http://www.orbswarm.com
//	: prototypes for GPS parsing routines 
//
//
//      
//
//	Written by ????, optional email
// refactored from swarmsputils.c  by Jon Foote (Head Rotor ar rotorbrain.com)
// -----------------------------------------------------------------------


//Parse an NMEA data sentence into the seperate components needed for conversion
//Returns SWARM_SUCCESS on a successful parse and  SWARM_INVALID_GPS_SENTENCE if 
//the input string was garbage or an invalid type.  The parser only accepts NMEA
//sentences of type GPGGA 
int parseGPSSentence(swarmGpsData * gpsdata);

//converts the raw NMEA gps lat long data into decimal lat long data
int convertNMEAGpsLatLonDataToDecLatLon(swarmGpsData * gpsdata);

//converts decimal lat/lon to UTM 
//East Longitudes are positive, West longitudes are negative. 
//North latitudes are positive, South latitudes are negative
//Lat and Long are in decimal degrees
void decimalLatLongtoUTM(const double ref_equ_radius, const double ref_ecc_squared, swarmGpsData * gpsdata);

