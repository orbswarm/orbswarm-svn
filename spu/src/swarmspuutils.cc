/**************************
 * Implementation file for swarmspuutils.h
 */

#include "../include/swarmspuutils.h"


// toggle the reset pin on the daughterboard MCU
int resetOrbMCU(void)
{  
   volatile unsigned int *PBDR, *PBDDR;
   unsigned char *start;
   int fd = open("/dev/mem", O_RDWR|O_SYNC);

   start =(unsigned char*) mmap(0, getpagesize(), PROT_READ|PROT_WRITE, MAP_SHARED, fd, 0x80840000);
   PBDR = (unsigned int *)(start + 0x04);     // port b
   PBDDR = (unsigned int *)(start + 0x14);    // port b direction register


   *PBDDR = 0x0FF;                             // all output (just lower 2 bits)

   *PBDR = 0xFE;                              // pin 0 is MCU reset
   sleep(1);
   *PBDR = 0xFF; 
 
   close(fd);
   return 0;
}

int toggleSpuLed(const unsigned int ledState)
{  
   volatile unsigned int *PEDR, *PEDDR;
   unsigned char *start;
   int fd = open("/dev/mem", O_RDWR|O_SYNC);

   start =(unsigned char*) mmap(0, getpagesize(), PROT_READ|PROT_WRITE, MAP_SHARED, fd, 0x80840000);
   PEDR = (unsigned int *)(start + 0x20);     // port e data
   PEDDR = (unsigned int *)(start + 0x24);    // port e direction register

   *PEDDR = 0xff;                             // all output (just 2 bits)

     if(ledState == SPU_LED_RED_ON) 
     {
       *PEDR |= 0xFE; 
     }
     else if(ledState == SPU_LED_GREEN_ON)
     {
       *PEDR |= 0xFD; 
     }
     else if(ledState == SPU_LED_BOTH_ON)
     {
       *PEDR |= 0xFF; 
     }
     else if(ledState == SPU_LED_BOTH_OFF)
     {
       *PEDR &= 0xFC; 
     }
     else if(ledState == SPU_LED_RED_OFF)
     {
       *PEDR &= 0xFD; 
     }
     else if(ledState == SPU_LED_GREEN_OFF)
     {
       *PEDR &= 0xFE; 
     }
     else
     {
       fprintf(stderr,"\nUNKNOWN LED STATE");
     }
   close(fd);
   return 0;
}

//Returns file descriptor for log file and 
FILE* openCurrentLogFile(char* logdir )
{
  char logfilename[MAX_LOG_FILE_NAME_SZ]; 
  long int prevfilemodtime = 0; 
  FILE *logfileFD = NULL;
  for (int lognum = 1; lognum <=  MAX_NUM_LOG_FILES; lognum++)
  {
    if(logdir == NULL)
    {
      sprintf(logfilename,"%s/%s%d",DEFAULT_LOG_PATH, LOG_FILE_BASE_NAME,lognum); 
    }
    else
    {
      sprintf(logfilename,"%s/%s%d",logdir, LOG_FILE_BASE_NAME,lognum); 
    }
    struct stat stat_p;		/* 'stat_p' is a pointer to a structure
				 * of type 'stat'.  			*/
    if ( -1 ==  stat (logfilename, &stat_p))
    {
      if(errno == ENOENT) //log file not created yet so ok to create and use
      {
        logfileFD = fopen(logfilename,"w");  
        break;
      }
      else //some other error so report and return an error status 
      {
        fprintf(stderr,"Error occoured attempting to stat %s\n", logfilename);
        logfileFD = NULL; 
        break;
      }
    }
    //Stat was successful so check the file size to see if it is ok to use 
    //fprintf(stderr,"\nMod Time is %d for file: %s", stat_p.st_mtime,logfilename);
    if(stat_p.st_size < MAX_LOG_FILE_SZ) //File is smaller than max so open  
    { 
        logfileFD = fopen(logfilename,"a"); //open in append mode  
        break;
    }
    else if(stat_p.st_mtime < prevfilemodtime) 
    {
      //file is older than previous so ok to truncate and use
      logfileFD = fopen(logfilename,"w"); //open in truncate mode  
      break;
    }
    else if(lognum == MAX_NUM_LOG_FILES)
    {
      //all of the logs are full so we need to start with the first log again
      if(logdir == NULL)
      {
        sprintf(logfilename,"%s/%s%d",DEFAULT_LOG_PATH, LOG_FILE_BASE_NAME,1); 
      }
      else
      {
        sprintf(logfilename,"%s/%s%d",logdir, LOG_FILE_BASE_NAME,1); 
      }
      logfileFD = fopen(logfilename,"w"); //open in truncate mode  
      break; 
    }
    prevfilemodtime =  stat_p.st_mtime;
  } 
  return logfileFD;
}

int spulog(char* msg, int msgSize, char* logdir)
{ 
  int bytesWritten = 0;
  FILE* logFD = openCurrentLogFile(logdir);
  if(logFD != NULL)
  {
    char logentbuf[MAX_LOG_ENTRY_SZ];
    if(msgSize >= (MAX_LOG_ENTRY_SZ - 1))
    {
      //fprintf(stderr,"IN max size if msgSize = %d\n",msgSize);
      strncpy(logentbuf,msg,MAX_LOG_ENTRY_SZ - 1);
      logentbuf[MAX_LOG_ENTRY_SZ - 1] = '\n';
      logentbuf[MAX_LOG_ENTRY_SZ] = 0;
      //fprintf(stderr,"IN max size if logEnt = %s\n",logentbuf);
      bytesWritten = MAX_LOG_ENTRY_SZ - 2;
    }
    else
    {
      sprintf(logentbuf,"%s\n",msg); 
      bytesWritten = msgSize;
    }
    fprintf(logFD,logentbuf);
    fclose(logFD);
  }
  return bytesWritten;
}

int parseGPSSentence(swarmGpsData * gpsdata)
{
  int status = SWARM_SUCCESS; 

  char* paramptr = NULL;
  int paramcount = 0;
  char gpssentcpy[MAX_GPS_SENTENCE_SZ];
  if(gpssentcpy == NULL){
    status = SWARM_OUT_OF_MEMORY_ERROR;
    return status;
  }
  strcpy(gpssentcpy, gpsdata->gpsSentence);
   
  paramptr = strtok(gpssentcpy,SWARM_NMEA_GPS_DATA_DELIM);
  if(paramptr != NULL){
     strcpy(gpsdata->gpsSentenceType,&paramptr[1]); //skip the leading $ char 
     paramcount++; 
  }
  //fprintf(stderr, "\nstrtok returned **%s** for gps sentence type\n",gpsdata->gpsSentenceType);
  if(strcmp(gpsdata->gpsSentenceType,SWARM_NMEA_GPS_SENTENCE_TYPE_GPGGA) == 0)
  { 
    //fprintf(stderr, "\nparsing rest of gps data\n");
    //Sentence type is correct so go ahead and get the rest of the data 
    while(paramptr != NULL)
    {
      paramptr = strtok(NULL,SWARM_NMEA_GPS_DATA_DELIM);
      if(paramptr != NULL)
      {
        switch(paramcount)
        {
          case 1:
            //utctime
            sscanf(paramptr,"%s",gpsdata->nmea_utctime);
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
    return SWARM_INVALID_GPS_SENTENCE;
  }

  // populate nmea_course, speed, and mode

  strcpy(gpssentcpy, gpsdata->vtgSentence);
  paramptr = strtok(gpssentcpy, SWARM_NMEA_GPS_DATA_DELIM);
  if (paramptr == NULL) {
    return SWARM_INVALID_GPS_SENTENCE;
  }

  // skip leading $
  if (paramptr[0] != '$') {
    return SWARM_INVALID_GPS_SENTENCE;
  }
  if(strcmp(paramptr+1, SWARM_NMEA_GPS_SENTENCE_TYPE_GPVTG) != 0) {
    return SWARM_INVALID_GPS_SENTENCE;
  }

  int e;
  char discard_char;
  float discard_float;
  float degrees, kmph;
  char mode;

  // Course over ground (degrees, referenced to true north)
  paramptr = strtok(NULL, SWARM_NMEA_GPS_DATA_DELIM);
  e = sscanf(paramptr, "%f", &degrees);
  if (e != 1) { return SWARM_INVALID_GPS_SENTENCE; }

  // Indicator of course reference (T == true north)
  paramptr = strtok(NULL, SWARM_NMEA_GPS_DATA_DELIM);
  e = sscanf(paramptr, "%c", &discard_char);
  if (e != 1 || discard_char != 'T') { return SWARM_INVALID_GPS_SENTENCE; }

  // Course over ground (not supported)
  paramptr = strtok(NULL, SWARM_NMEA_GPS_DATA_DELIM);
  e = sscanf(paramptr, "%f", &discard_float);
  if (e != 1) { return SWARM_INVALID_GPS_SENTENCE; }

  // Indicator of course reference (M == magnetic north)
  paramptr = strtok(NULL, SWARM_NMEA_GPS_DATA_DELIM);
  e = sscanf(paramptr, "%c", &discard_char);
  if (e != 1 || discard_char != 'M') { return SWARM_INVALID_GPS_SENTENCE; }

  // Speed over ground (knots)
  paramptr = strtok(NULL, SWARM_NMEA_GPS_DATA_DELIM);
  e = sscanf(paramptr, "%f", &discard_float);
  if (e != 1) { return SWARM_INVALID_GPS_SENTENCE; }

  // Units (knots)
  paramptr = strtok(NULL, SWARM_NMEA_GPS_DATA_DELIM);
  e = sscanf(paramptr, "%c", &discard_char);
  if (e != 1 || discard_char != 'N') { return SWARM_INVALID_GPS_SENTENCE; }
  
  // Speed over ground (km/h)
  paramptr = strtok(NULL, SWARM_NMEA_GPS_DATA_DELIM);
  e = sscanf(paramptr, "%f", &kmph);
  if (e != 1) { return SWARM_INVALID_GPS_SENTENCE; }
    
  // Units (km/h)
  paramptr = strtok(NULL, SWARM_NMEA_GPS_DATA_DELIM);
  e = sscanf(paramptr, "%c", &discard_char);
  if (e != 1 || discard_char != 'K') { return SWARM_INVALID_GPS_SENTENCE; }

  // Mode
  paramptr = strtok(NULL, SWARM_NMEA_GPS_DATA_DELIM);
  mode = paramptr[0];
  if (mode != 'A' && mode != 'D' && mode != 'E' && mode != 'N') {
    return SWARM_INVALID_GPS_SENTENCE;
  }

  // Trailing knick-knacks
  if (paramptr[1] != '*') {
    return SWARM_INVALID_GPS_SENTENCE;
  }
  char checksum[2];
  checksum[0] = paramptr[3];
  checksum[0] = paramptr[4];
  if (paramptr[5] != '\r'
      || paramptr[6] != '\n') {
    return SWARM_INVALID_GPS_SENTENCE;
  }

  // Done!
  if (strtok(NULL, SWARM_NMEA_GPS_DATA_DELIM) != NULL) {
    return SWARM_INVALID_GPS_SENTENCE;
  }

  // populate structure

  // convert from due north to due east
  degrees -= 90;
  if (degrees < 0) { degrees += 360; }
  // convert from degrees to radians
  gpsdata->nmea_course = deg2rad * degrees;
  // convert from km/h to m/s
  gpsdata->speed = kmph * (1000.0 / (60.0*60.0));
  gpsdata->mode = mode;

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
	double LongTemp = (Long+180)-int((Long+180)/360)*360-180; // -180.00 .. 179.9;

	double LatRad = Lat*deg2rad;
	double LongRad = LongTemp*deg2rad;
	double LongOriginRad;
	int    ZoneNumber;

	ZoneNumber = int((LongTemp + 180)/6) + 1;
  
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
	LongOriginRad = LongOrigin * deg2rad;

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

int getMessageType(char* message)
{
  int messageSize = strlen(message); 
  int messageType = AGGR_MSG_TYPE_UNKNOWN; //default to unknown 
  switch(message[0])
  {
    case MSG_HEAD_MOTOR_CONTROLER : 
      // The firs char of the message matched so lets check the last  
      if(message[messageSize] == MSG_END_MOTOR_CONTROLER) 
        messageType = AGGR_MSG_TYPE_MOTOR_CONTROL; 
    break;
    case MSG_HEAD_LIGHTING : 
      // The firs char of the message matched so lets check the last  
      if(message[messageSize] == MSG_END_LIGHTING) 
        messageType = AGGR_MSG_TYPE_EFFECTS; 
    break;
    case MSG_HEAD_MOTHER_SHIP : 
      // The firs char of the message matched so lets check the last  
      if(message[messageSize] == MSG_END_MOTHER_SHIP) 
      {
         char* msgptr = NULL;
         char msgBufCpy[MAX_BUFF_SZ + 1];
         strcpy(msgBufCpy, message);
         char msgdelim[1];   
         msgdelim[0] = MOTHER_SHIP_MSG_DELIM;
         msgptr = strtok(&msgBufCpy[1],msgdelim);
         if(msgptr != NULL)
         {
           if(strcmp(msgptr,MOTHER_SHIP_MSG_HEAD_STATUS) == 0)
           {
             messageType = AGGR_MSG_TYPE_MOTHER_SHIP_SPU_POLL; 
           }          
           else if(strcmp(msgptr,MOTHER_SHIP_MSG_HEAD_TRAJECTORY) == 0)
           {
             messageType = AGGR_MSG_TYPE_TRAJECTORY; 
           }
           else if(strcmp(msgptr,MOTHER_SHIP_MSG_HEAD_LOCATION) == 0)
           {
             messageType = AGGR_MSG_TYPE_MOTHER_SHIP_LOC; 
           }
         }
      }
    break;
  } 
  return messageType;
}
