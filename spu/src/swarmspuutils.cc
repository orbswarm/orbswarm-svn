
// ---------------------------------------------------------------------
// 
//	File: swarmspuutils.cc
//      SWARM Orb SPU code http://www.orbswarm.com
//	general SPU utilities
//
//
//      
//
//	Written by Matt C, Dillo, Niladri, Jesse Z, refactored by Jon F
// -----------------------------------------------------------------------
#include "../include/swarmspuutils.h"
#include "../include/swarmserial.h"

const int AD_POLL_PRECISION = 2;

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


int getMessageType(char* message)
{
  int messageSize = strlen(message); 
  int messageType = AGGR_MSG_TYPE_UNKNOWN; //default to unknown 
  fprintf(stderr,"\n First Char of Message :%c\n", message[0]); 
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
      
         fprintf(stderr, "\nLAST CHAR **%c** of message \n",message[messageSize-1]);
      if(rindex(message, MSG_END_MOTHER_SHIP) != NULL) 
      {
         char* msgptr = NULL;
         char msgBufCpy[MAX_BUFF_SZ + 1];
         strcpy(msgBufCpy, message);
         msgptr = strtok(&msgBufCpy[1],MOTHER_SHIP_MSG_DELIM);
         fprintf(stderr, "\nstrtok returned **%s** for message type\n",msgptr);
         if(msgptr != NULL)
         {
           if(strcmp(msgptr,MOTHER_SHIP_MSG_HEAD_STATUS) == 0)
           {
             fprintf(stderr, "\nIDENTIFIED MESSAGE POLL MESSAGE\n");
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

void genSpuDump(char* logBuffer, int maxBufSz, swarmGpsData *gpsData, spuADConverterStatus *adConverterStatus)
{
  char * scratchBuff = (char *)malloc(maxBufSz * 2);
  char *adBuffer = (char *)malloc(50);
  int bytesUsed = sprintf(scratchBuff,"NORTH=%lf\nEAST=%lf\nUTC_TIME=%s\nHEADING=%f\nSPEED=%f\nMSHIP_N=%lf\nMSHIP_E=%lf\n",
						   gpsData->UTMNorthing,
						   gpsData->UTMEasting,
						   gpsData->nmea_utctime,
						   gpsData->nmea_course,
						   gpsData->speed, 
						   gpsData->metFromMshipNorth, 
						   gpsData->metFromMshipEast);
				
	

  // This section removed by Jon because it means you need to link with adconverter.o anyplace you link to swarmspuutils.o
  // the way to do this is to populate a data structure (spustatus, say) then call genSPuDump. genSpuDump should not call get_ADC_channel directly

#ifndef foo
	for(int i = 0; i < 5; i++) {
		//sprintf(scratchBuff+strlen(scratchbuf), "AD_CHANNEL_%d=%3.3fV\r\n", i, get_ADC_channel(i, 3.3, AD_POLL_PRECISION)); 
		sprintf(adBuffer, "AD_CHANNEL_%d=%3.3fV\r\n", i, adConverterStatus->ad_vals[i]); 
		//get_ADC_channel(i, adMaxVoltage, AD_POLL_PRECISION)
		strncat(scratchBuff, adBuffer, strlen(adBuffer));
	}
	
	sprintf(adBuffer, "SONAR=%3.3f inches\r\n", adConverterStatus->sonar); 
	sprintf(adBuffer, "BATTERY=%3.3f inches\r\n", adConverterStatus->battery_voltage); 
	strncat(scratchBuff, adBuffer, strlen(adBuffer));

#endif
	strncpy(logBuffer,scratchBuff, maxBufSz -1);
  
  free(scratchBuff);
  free(adBuffer);
}

