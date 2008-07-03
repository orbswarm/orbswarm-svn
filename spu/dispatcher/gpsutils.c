
// ---------------------------------------------------------------------
// 
//	File: gpsutils.c
//      SWARM Orb SPU code http://www.orbswarm.com
//	GPS parsing utilities written by Matt and Jessie 
//
//
//      
//
//	Written by yourname, optional email
// -----------------------------------------------------------------------
#include "gpsutils.h"
#include "scanner.h"

//extern int parseDebug;

int parseGPSGGASentence(swarmGpsData * gpsdata)
{
  int status = SWARM_SUCCESS; 

  char* paramptr = NULL;
  int paramcount = 0;
  char gpssentcpy[MAX_GPS_SENTENCE_SZ];
  char* strtok_r_ptr = NULL;

  strcpy(gpssentcpy, gpsdata->ggaSentence);
   
  paramptr = strtok_r(gpssentcpy,SWARM_NMEA_GPS_DATA_DELIM,&strtok_r_ptr);
  if(paramptr != NULL){
     strcpy(gpsdata->gpsSentenceType,paramptr);
     paramcount++; 
  }
  logit(eGpsLog, eLogDebug,  "\nstrtok returned **%s** for gps sentence type\n",gpsdata->gpsSentenceType);
  if(strcmp(gpsdata->gpsSentenceType,SWARM_NMEA_GPS_SENTENCE_TYPE_GPGGA) == 0)
    { 
      logit(eGpsLog, eLogDebug,  "\nparsing rest of gps data\n");
      //Sentence type is correct so go ahead and get the rest of the data 
      while(paramptr != NULL)
	{
	  paramptr = strtok_r(NULL,SWARM_NMEA_GPS_DATA_DELIM,&strtok_r_ptr);
	  if(paramptr != NULL)
	    {
	      switch(paramcount)
		{
		case 1:
		  //utctime
		  sscanf(paramptr,"%s",gpsdata->nmea_utctime);
		  sscanf(paramptr,"%lf",&(gpsdata->utcTime));
		  //fprintf(stderr, "\n utctime : %s",paramptr);
		  break;
		case 2:
		  //nmea format latddmm
		  sscanf(paramptr,"%lf",&(gpsdata->nmea_latddmm));
		  //fprintf(stderr, "\n latddmm: %s",paramptr);
		  break;
		case 3:
		  //nmea format latsector
		  sscanf(paramptr,"%c",&(gpsdata->nmea_latsector));
		  //fprintf(stderr, "\n latsector: %s",paramptr);
		  break;
		case 4:
		  //nmea format londdmm
		  sscanf(paramptr,"%lf",&(gpsdata->nmea_londdmm));
		  //fprintf(stderr, "\n londdmm: %s",paramptr);
		  break;
		case 5:
		  //nmea format lonsector 
		  sscanf(paramptr,"%c",&(gpsdata->nmea_lonsector));
		  //fprintf(stderr, "\n lonsector : %s",paramptr);
		  break;
		default:
		  break;
		};
	    }
	  paramcount++; 
	}
      //fprintf(stderr,"\n RAW GPS DATA %s,%s,%Lf,%c,%Lf,%c \n",gpsdata->gpsSentenceType,gpsdata->nmea_utctime,gpsdata->nmea_latddmm,gpsdata->nmea_latsector,gpsdata->nmea_londdmm,gpsdata->nmea_lonsector);
    } else  {
    logit(eGpsLog, eLogError, "\nRETURNING INVALID GPS GGA SENTENCE\n");
    return SWARM_INVALID_GPS_SENTENCE;
  }
  
  return status;
}

int parseGPSVTGSentance(swarmGpsData * gpsdata)
{
  
  int status = SWARM_SUCCESS; 

  char* paramptr = NULL;
  //int paramcount = 0;
  char gpssentcpy[MAX_GPS_SENTENCE_SZ];
  char* strtok_r_ptr = NULL;

  // populate nmea_course, speed, and mode
  logit(eGpsLog, eLogDebug, "\n GPS sentence is %s", gpsdata->vtgSentence); 
  strcpy(gpssentcpy, gpsdata->vtgSentence);
  paramptr = strtok_r(gpssentcpy, SWARM_NMEA_GPS_DATA_DELIM,&strtok_r_ptr);
  
  //fprintf(stderr, "\nstrtok returned **%s** for gps sentence type\n",paramptr);
  
  if (paramptr == NULL) {
    logit(eGpsLog, eLogInfo,"\nRETURNING INVALID GPS VTG SENTANCE PARAM PTR NULL\n");
    return SWARM_INVALID_GPS_SENTENCE;
  }
  
  
  // skip leading $
  /*
  if (paramptr[0] != '$') {
  //fprintf(stderr,"\nRETURNING INVALID GPS SENTANCE MISSING $\n");
  return SWARM_INVALID_GPS_SENTENCE;
  }
  if(strcmp(paramptr+1, SWARM_NMEA_GPS_SENTENCE_TYPE_GPVTG) != 0) {
  //fprintf(stderr,"\nRETURNING INVALID TYPE NOT GPVTG $\n");
  return SWARM_INVALID_GPS_SENTENCE;
  }
  */

  int e;
  char discard_char;
  float discard_float;
  float degrees, kmph;
//  char mode;

  // Course over ground (degrees, referenced to true north)
  paramptr = strtok_r(NULL, SWARM_NMEA_GPS_DATA_DELIM,&strtok_r_ptr);
  logit(eGpsLog, eLogDebug,"\nSTRTOK GOT %s as course over ground\n",paramptr);  
  e = sscanf(paramptr, "%f", &degrees);
  if (e != 1) { return SWARM_INVALID_GPS_SENTENCE; }
  
  // Indicator of course reference (T == true north)
  paramptr = strtok_r(NULL, SWARM_NMEA_GPS_DATA_DELIM,&strtok_r_ptr);
  logit(eGpsLog, eLogDebug,"\nSTRTOK GOT %s as course reference\n",paramptr);
  e = sscanf(paramptr, "%c", &discard_char);
  if (e != 1 || discard_char != 'T') { return SWARM_INVALID_GPS_SENTENCE; }
  
  // Indicator of course reference (M == magnetic north)
  paramptr = strtok_r(NULL, SWARM_NMEA_GPS_DATA_DELIM,&strtok_r_ptr);
  logit(eGpsLog, eLogDebug,"\nSTRTOK GOT %s as course ref\n",paramptr);
  e = sscanf(paramptr, "%c", &discard_char);
  if (e != 1 || discard_char != 'M') { return SWARM_INVALID_GPS_SENTENCE; }

  // Speed over ground (knots)
  paramptr = strtok_r(NULL, SWARM_NMEA_GPS_DATA_DELIM,&strtok_r_ptr);
  logit(eGpsLog, eLogDebug,"\nSTRTOK GOT %s as speed over ground in knots\n",paramptr);
  e = sscanf(paramptr, "%f", &discard_float);
  if (e != 1) { return SWARM_INVALID_GPS_SENTENCE; }

  // Units (knots)
  paramptr = strtok_r(NULL, SWARM_NMEA_GPS_DATA_DELIM,&strtok_r_ptr);
  logit(eGpsLog, eLogDebug,"\nSTRTOK GOT %s as unit of speed for prev field\n",paramptr);
  e = sscanf(paramptr, "%c", &discard_char);
  if (e != 1 || discard_char != 'N') { return SWARM_INVALID_GPS_SENTENCE; }
  
  // Speed over ground (km/h)
  paramptr = strtok_r(NULL, SWARM_NMEA_GPS_DATA_DELIM,&strtok_r_ptr);
  logit(eGpsLog, eLogDebug,"\nSTRTOK GOT %s as speed over ground 2\n",paramptr);
  e = sscanf(paramptr, "%f", &kmph);
  if (e != 1) { return SWARM_INVALID_GPS_SENTENCE; }
    
  paramptr = strtok_r(NULL, SWARM_NMEA_GPS_DATA_DELIM,&strtok_r_ptr);
  logit(eGpsLog, eLogDebug,"\nSTRTOK GOT %s as unit of speed for prev field\n",paramptr);
  e = sscanf(paramptr, "%c", &discard_char);
  if (e != 1 || discard_char != 'K') { return SWARM_INVALID_GPS_SENTENCE; }

//  paramptr = strtok_r(NULL, SWARM_NMEA_GPS_DATA_DELIM,&strtok_r_ptr);
//  logit(eGpsLog, eLogDebug, "\nSTRTOK GOT %s as nav mode indicator\n",paramptr);
//  mode = paramptr[0];
//  
//  if (mode != 'A' && mode != 'D' && mode != 'E' && mode != 'N') {
//    return SWARM_INVALID_GPS_SENTENCE;
//  }
  
  // convert from due north to due east
  degrees -= 90;
  if (degrees < 0) 
    { 
      degrees += 360; 
    }
  // convert from degrees to radians

  gpsdata->nmea_course = PI /180.0 * (degrees * -1);

  // convert from km/h to m/s
  gpsdata->speed = kmph * (1000.0 / (60.0*60.0));
//  gpsdata->mode = mode;

  return status;
}

int convertNMEAGpsLatLonDataToDecLatLon(swarmGpsData * gpsdata)
{
  double latdd = 0;
  double londd = 0;
  double latmm = 0;
  double lonmm = 0;
  
  //get the degress part
  latdd = (int)(gpsdata->nmea_latddmm / 100);
  londd = (int)(gpsdata->nmea_londdmm / 100);
  
  //get the minutes part
  latmm = gpsdata->nmea_latddmm - 100 * latdd;
  lonmm = gpsdata->nmea_londdmm - 100 * londd;

   //Turn DDMM.MMMM into DDD.DDDDD format
   latdd = latdd + (latmm / 60.0); 
   londd = londd + (lonmm / 60.0); 

   // Add sign for N/S and E/W sector:
   if(gpsdata->nmea_latsector == 'S')
     latdd = -1.0 * latdd;
   if(gpsdata->nmea_lonsector == 'W')
     londd = -1.0 * londd;

   gpsdata->latdd = latdd;
   gpsdata->londd = londd;

   return SWARM_SUCCESS;  
}

char UTMLetterDesignator(double Lat)
{
        //This routine determines the correct UTM letter designator for the given latitude
        //returns 'Z' if latitude is outside the UTM limits of 84N to 80S
	char LetterDesignator;

	if((84 >= Lat) && (Lat >= 72)) LetterDesignator = 'X';
	else if((72 > Lat) && (Lat >= 64)) LetterDesignator = 'W';
	else if((64 > Lat) && (Lat >= 56)) LetterDesignator = 'V';
	else if((56 > Lat) && (Lat >= 48)) LetterDesignator = 'U';
	else if((48 > Lat) && (Lat >= 40)) LetterDesignator = 'T';
	else if((40 > Lat) && (Lat >= 32)) LetterDesignator = 'S';
	else if((32 > Lat) && (Lat >= 24)) LetterDesignator = 'R';
	else if((24 > Lat) && (Lat >= 16)) LetterDesignator = 'Q';
	else if((16 > Lat) && (Lat >= 8)) LetterDesignator = 'P';
	else if(( 8 > Lat) && (Lat >= 0)) LetterDesignator = 'N';
	else if(( 0 > Lat) && (Lat >= -8)) LetterDesignator = 'M';
	else if((-8> Lat) && (Lat >= -16)) LetterDesignator = 'L';
	else if((-16 > Lat) && (Lat >= -24)) LetterDesignator = 'K';
	else if((-24 > Lat) && (Lat >= -32)) LetterDesignator = 'J';
	else if((-32 > Lat) && (Lat >= -40)) LetterDesignator = 'H';
	else if((-40 > Lat) && (Lat >= -48)) LetterDesignator = 'G';
	else if((-48 > Lat) && (Lat >= -56)) LetterDesignator = 'F';
	else if((-56 > Lat) && (Lat >= -64)) LetterDesignator = 'E';
	else if((-64 > Lat) && (Lat >= -72)) LetterDesignator = 'D';
	else if((-72 > Lat) && (Lat >= -80)) LetterDesignator = 'C';
	else LetterDesignator = 'Z'; //error flag indicating that the Latitude is outside the UTM limits

	return LetterDesignator;
}

void decimalLatLongtoUTM(const double ref_equ_radius, const double ref_ecc_squared, swarmGpsData * gpsdata)
{
        double Lat = gpsdata->latdd;
        double Long = gpsdata->londd;

	double a = ref_equ_radius;
	double eccSquared = ref_ecc_squared;
	double k0 = 0.9996;

	double LongOrigin;
	double eccPrimeSquared;
	double N, T, C, A, M;
	
//Make sure the longitude is between -180.00 .. 179.9
	double LongTemp = (Long+180) - (int)((Long+180)/360)*360-180; // -180.00 .. 179.9;

	double LatRad = Lat*DEG2RAD;
	double LongRad = LongTemp*DEG2RAD;
	double LongOriginRad;
	int    ZoneNumber;

	ZoneNumber = (int)((LongTemp + 180)/6) + 1;
  
	if( Lat >= 56.0 && Lat < 64.0 && LongTemp >= 3.0 && LongTemp < 12.0 )
		ZoneNumber = 32;

  // Special zones for Svalbard
	if( Lat >= 72.0 && Lat < 84.0 ) 
	{
	  if(      LongTemp >= 0.0  && LongTemp <  9.0 ) ZoneNumber = 31;
	  else if( LongTemp >= 9.0  && LongTemp < 21.0 ) ZoneNumber = 33;
	  else if( LongTemp >= 21.0 && LongTemp < 33.0 ) ZoneNumber = 35;
	  else if( LongTemp >= 33.0 && LongTemp < 42.0 ) ZoneNumber = 37;
	 }
	LongOrigin = (ZoneNumber - 1)*6 - 180 + 3;  //+3 puts origin in middle of zone
	LongOriginRad = LongOrigin * DEG2RAD;

	//compute the UTM Zone from the latitude and longitude
	sprintf(gpsdata->UTMZone, "%d%c", ZoneNumber, UTMLetterDesignator(Lat));

	eccPrimeSquared = (eccSquared)/(1-eccSquared);

	N = a/sqrt(1-eccSquared*sin(LatRad)*sin(LatRad));
	T = tan(LatRad)*tan(LatRad);
	C = eccPrimeSquared*cos(LatRad)*cos(LatRad);
	A = cos(LatRad)*(LongRad-LongOriginRad);

	M = a*((1 - eccSquared/4 - 3*eccSquared*eccSquared/64	- 5*eccSquared*eccSquared*eccSquared/256)*LatRad - (3*eccSquared/8	+ 3*eccSquared*eccSquared/32	+ 45*eccSquared*eccSquared*eccSquared/1024)*sin(2*LatRad) + (15*eccSquared*eccSquared/256 + 45*eccSquared*eccSquared*eccSquared/1024)*sin(4*LatRad) - (35*eccSquared*eccSquared*eccSquared/3072)*sin(6*LatRad));
	
	gpsdata->UTMEasting = (double)(k0*N*(A+(1-T+C)*A*A*A/6
					+ (5-18*T+T*T+72*C-58*eccPrimeSquared)*A*A*A*A*A/120)
					+ 500000.0);

	gpsdata->UTMNorthing = (double)(k0*(M+N*tan(LatRad)*(A*A/2+(5-T+9*C+4*C*C)*A*A*A*A/24
				 + (61-58*T+T*T+600*C-330*eccPrimeSquared)*A*A*A*A*A*A/720)));
	if(Lat < 0)
	  gpsdata->UTMNorthing += 10000000.0; //10000000 meter offset for southern hemisphere
}

int parseRawAggregatorGPSData(char* rawGPS, swarmGpsData * gpsdata) 
{
   int status = SWARM_SUCCESS;
   char* sentptr = NULL;
   int gpscnt = 0;
   char gpsBufCpy[MAX_BUFF_SZ + 1];
   char* strtok_r_ptr = NULL;
   strcpy(gpsBufCpy, rawGPS);
   sentptr = strtok_r(gpsBufCpy,"\n",&strtok_r_ptr);
   while(sentptr != NULL)
   {
     switch(gpscnt)
     {
       case 0: 
         //printf("\nSTR TOKED SENTANCE PTR****%s****\n",sentptr);
         strcpy(gpsdata->ggaSentence,sentptr);
       break;//end GPS Lat/Lon parse

       case 1:   // Parse the gps velocity data 
         //printf("\nSTR TOKED SENTANCE PTR****%s****\n",sentptr);
         strcpy(gpsdata->vtgSentence,sentptr);
       break;//end GPS VTG data parse 
     }
     gpscnt++; 
     sentptr = strtok_r(NULL,"\n",&strtok_r_ptr);
   }
   return status;
}

int parseAndConvertGPSData(char* rawGPS, swarmGpsData * gpsdata) 
{
  int status = SWARM_SUCCESS;

  status = parseGPSGGASentence(gpsdata);
  if(status == SWARM_SUCCESS)
  { 
    logit(eGpsLog, eLogDebug,  "\n Parsed line %s \n",gpsdata->ggaSentence);
    status = convertNMEAGpsLatLonDataToDecLatLon(gpsdata);
    if(status == SWARM_SUCCESS)
    {
      logit(eGpsLog, eLogDebug,  "\n Decimal lat:%lf lon:%lf utctime:%s \n",gpsdata->latdd,gpsdata->londd,gpsdata->nmea_utctime);
           
      decimalLatLongtoUTM(WGS84_EQUATORIAL_RADIUS_METERS, WGS84_ECCENTRICITY_SQUARED, gpsdata);
      logit(eGpsLog, eLogDebug,  "Northing:%f,Easting:%f,UTMZone:%s\n",gpsdata->UTMNorthing,gpsdata->UTMEasting,gpsdata->UTMZone);
    }
  }
  else
  {
    logit(eGpsLog, eLogError,  "\n Failed GPS parse status=%i", status);
  }
  status = SWARM_SUCCESS;
  status = parseGPSVTGSentance(gpsdata);
  if(status != SWARM_SUCCESS){
    logit(eGpsLog, eLogDebug,  "\n Failed GPS VTG parse status=%i", status);
  }
  return status;
}

