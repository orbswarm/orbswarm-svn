/**************************
 * Implementation file for swarmspuutils.h
 */

#include "../include/swarmspuutils.h"


// toggle the reset pin on the daighterboard MCU
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

int parseGPSSentance(swarmGpsData * gpsdata)
{
  int status = SWARM_SUCCESS; 

  char* paramptr = NULL;
  int paramcount = 0;
  char gpssentcpy[MAX_GPS_SENTANCE_SZ];
  if(gpssentcpy == NULL){
    status = SWARM_OUT_OF_MEMORY_ERROR;
    return status;
  }
  strcpy(gpssentcpy, gpsdata->gpsSentance);
   
  paramptr = strtok(gpssentcpy,SWARM_NMEA_GPS_DATA_DELIM);
  if(paramptr != NULL){
     strcpy(gpsdata->gpsSentanceType,&paramptr[1]); //skip the leading $ char 
     paramcount++; 
  }
  //fprintf(stderr, "\nstrtok returned **%s** for gps sentance type\n",gpsdata->gpsSentanceType);
  if(strcmp(gpsdata->gpsSentanceType,SWARM_NMEA_GPS_SENTANCE_TYPE_GPGGA) == 0)
  { 
    //fprintf(stderr, "\nparsing rest of gps data\n");
    //Sentance type is correct so go ahead and get the rest of the data 
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
            sscanf(paramptr,"%Lf",&(gpsdata->nmea_latddmm));
            //fprintf(stderr, "\n latddmm: %s",paramptr);
            break;
          case 3:
            //nmea format latsector
            sscanf(paramptr,"%c",&(gpsdata->nmea_latsector));
            //fprintf(stderr, "\n latsector: %s",paramptr);
            break;
          case 4:
            //nmea format londdmm
            sscanf(paramptr,"%Lf",&(gpsdata->nmea_londdmm));
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
  //fprintf(stderr,"\n RAW GPS DATA %s,%s,%Lf,%c,%Lf,%c \n",gpsdata->gpsSentanceType,gpsdata->nmea_utctime,gpsdata->nmea_latddmm,gpsdata->nmea_latsector,gpsdata->nmea_londdmm,gpsdata->nmea_lonsector);
  }
  else
  {
    status = SWARM_INVALID_GPS_SENTANCE; 
  }
  return status;
}

int convertNMEAGpsLatLonDataToDecLatLon(swarmGpsData * gpsdata)
{
   long double latdd = 0;
   long double londd = 0;
   long double latmm = 0;
   long double lonmm = 0;

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

