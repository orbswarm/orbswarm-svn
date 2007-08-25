
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
      if((rindex(message, MSG_END_MOTOR_CONTROLER)!= NULL)) 
        messageType = AGGR_MSG_TYPE_MOTOR_CONTROL; 
    break;
    case MSG_HEAD_LIGHTING : 
      // The firs char of the message matched so lets check the last  
      if((rindex(message, MSG_END_LIGHTING)!= NULL)) 
        messageType = AGGR_MSG_TYPE_EFFECTS; 
    break;
    case MSG_HEAD_MOTHER_SHIP : 
      // The firs char of the message matched so lets check the last  
      
         fprintf(stderr, "\nLAST CHAR **%c** of message \n",message[messageSize-1]);
      if(rindex(message, MSG_END_MOTHER_SHIP) != NULL) 
      {
         char* msgptr = NULL;
         char* strtok_r_buf;
         char msgBufCpy[MAX_BUFF_SZ + 1];
         strcpy(msgBufCpy, message);
         msgptr = strtok_r(&msgBufCpy[1],MOTHER_SHIP_MSG_DELIM,&strtok_r_buf);
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

void genSpuDump(char* logBuffer, int maxBufSz, swarmGpsData *gpsData, spuADConverterStatus *adConverterStatus, swarmMotorData* motorData)
{
  char * scratchBuff = (char *)malloc(maxBufSz * 2);
  //char *adBuffer = (char *)malloc(50);
  int bytesUsed = sprintf(scratchBuff,"NORTH=%lf\nEAST=%lf\nUTC_TIME=%s\nHEADING=%f\nSPEED=%f\nMSHIP_N=%lf\nMSHIP_E=%lf\nAD_CHANNEL_0=%3.3fV\nAD_CHANNEL_1=%3.3fV\nAD_CHANNEL_2=%3.3fV\nAD_CHANNEL_3=%3.3fV\nAD_CHANNEL_4=%3.3fV\nSONAR=%3.3f inches\nBATTERY=%3.3f inches\nSTEER_ACTUAL=%d\nDRIVE_ACTUAL%d\n",
						   gpsData->UTMNorthing,
						   gpsData->UTMEasting,
						   gpsData->nmea_utctime,
						   gpsData->nmea_course,
						   gpsData->speed, 
						   gpsData->metFromMshipNorth, 
						   gpsData->metFromMshipEast,
                                                   adConverterStatus->ad_vals[0],
                                                   adConverterStatus->ad_vals[1],
                                                   adConverterStatus->ad_vals[2],
                                                   adConverterStatus->ad_vals[3],
                                                   adConverterStatus->ad_vals[4],
                                                   adConverterStatus->sonar,
                                                   adConverterStatus->battery_voltage,
                                                   motorData->steerActual,
                                                   motorData->driveActual
                                                   );
				
	

	strncpy(logBuffer,scratchBuff, maxBufSz -1);
  
  free(scratchBuff);
  //free(adBuffer);
}

int getMessageForDelims(char* msgBuff, int maxMsgSz, int* msgSize,
               char* input, int inputSz, char startDelim, char endDelim, bool incDelimsInMsg) 
{
   int status = SWARM_SUCCESS;
   int msgStartIdx = 0;
   int msgEndIdx = 0;
   int tempMsgSize = 0;
   bool foundStart = false;
   bool foundEnd = false;
   bool delimsSame = false;

   if(startDelim == endDelim)
     delimsSame = true;

   if(input != NULL){
      for(int i = 0; i < inputSz;i++)
      {
         if(!delimsSame)
         {
           if(input[i] == startDelim)
           {
             foundStart = true;
             msgStartIdx = i;
           } 
           else if(input[i] == endDelim) 
           {
             if(foundStart)
               {
                /*
                  great we found the end of the message and it is consitant with
                  what we got for the beginning
                */ 
                   msgEndIdx = i;
                   foundEnd = true;
                   tempMsgSize =(msgEndIdx - msgStartIdx) + 1;
               } 
               else
               {
                /* Something got fucked up so report Error */
                //fprintf(stderr,"\nSETTING STATUS TO : AGGR_MSG_FOOTER_WITHOUT_HEADER_ERROR \n");
                 foundEnd = true;
                 status = AGGR_MSG_FOOTER_WITHOUT_HEADER_ERROR; 
               }
           }
         }
         else
         {
           if(input[i] == startDelim) //start and end delims the same
           {
             if(!foundStart) //First occurance of delim
             {
               foundStart = true;
               msgStartIdx = i;
             }
             else if(foundStart && (msgStartIdx != i)) //second occurance of delim
             {
               /*
                 great we found the end of the message and it is consitant with
                 what we got for the beginning
               */ 
               msgEndIdx = i;
               foundEnd = true;
               tempMsgSize = (msgEndIdx - msgStartIdx) + 1;
             }
           }
         }
         
         if(status != SWARM_SUCCESS) break; //get out of loop on error
         if(foundStart && foundEnd) break; //get out of loop when 1 message found
      }
      if(foundStart && foundEnd)
      {
         if(tempMsgSize < maxMsgSz)
         { 
          if(!incDelimsInMsg)
          {
            msgStartIdx++;
            tempMsgSize = tempMsgSize - 2;
          }
          memcpy(msgBuff,&input[msgStartIdx],tempMsgSize);
          //Null terminate the message so string funcs can manip it
          msgBuff[tempMsgSize] = 0;
          *msgSize = tempMsgSize;
         }
         else
         {
           status = AGGR_MSG_TO_LARGE_FOR_BUFFER_ERROR;  
         }
      }
      else if(foundStart && !foundEnd && (status == SWARM_SUCCESS))
      {
                //fprintf(stderr,"\nSETTING STATUS TO : AGGR_MSG_HEADER_WITHOUT_FOOTER_ERROR\n");
        status = AGGR_MSG_HEADER_WITHOUT_FOOTER_ERROR; 
      }
      else if(!foundStart && !foundEnd)
      {
        //fprintf(stderr,"\nSETTING STATUS TO : AGGR_MSG_NO_DELIMS_FOUND_ERROR\n");
        status = AGGR_MSG_NO_DELIMS_FOUND_ERROR; 
      }
   }
   return status;
}

/* retrieve IP address using ioctl interface */
int getIP(const char *Interface, char *ip)
{
  int                  s;
  struct ifreq         ifr;
  int                  err;
  struct sockaddr_in * addr_in;

  s = socket( AF_INET, SOCK_DGRAM, IPPROTO_UDP );
  if ( -1 == s )
  {
    perror("Getting interface socket");
    close(s);
    return(-1);
  }

  if(Interface == NULL) Interface="eth0";

  strncpy( ifr.ifr_name, Interface, IFNAMSIZ );
  err = ioctl( s, SIOCGIFADDR, &ifr );
  if ( -1 == err )
  {
    perror("Getting IP address");
    close(s);
    return(-1);
  }
  addr_in = (struct sockaddr_in *)&ifr.ifr_addr;

  if(ip != NULL)
  {
    sprintf(ip,"%s",inet_ntoa(addr_in->sin_addr));
  }

  close(s);
  return(0);
}

int parseLSMP(char *msgptr,int msglen,int lnsport,int motorport) {

int i=0;
char inchar;
char motorbuf[10];
char lnsbuf[1024];
char addrbyte[2];
static int inmsg;
static int inlns;
static int inmtr;
static int msgpos;
static int addrcnt;
static int motorpos;
static int lnspos;


/*     printf("inmsg=%d inlns=%d inmtr=%d msgpos=%d addrcnt=%d motorpos=%d lnspos=%d\n",inmsg,inlns,inmtr,msgpos,addrcnt,motorpos,lnspos);
*/

    if (inmsg == 0) {
     		memset(motorbuf,0,sizeof(motorbuf));
     		memset(lnsbuf,0,sizeof(lnsbuf));
    } 

     for (i=0;i < msglen;i++) {
		inchar = *msgptr++;
	/*	printf("%c %d",inchar,i);  */
		if (inmsg ==0) {
			if (inchar='{') {
			/*	printf("INPKT\n"); */
		    		inmsg=1;
				
			}
		} else {
			if (((isdigit(inchar)) >0) &&(addrcnt !=2)) {
				addrbyte[addrcnt++]=inchar;
			} else if (addrcnt==2){
				/*	printf("INMSG\n"); */
					switch (inchar){
						case ' ':
							if(inlns==1) {
								lnsbuf[lnspos++]=inchar;
							}
							if (inmtr==1) {
								motorbuf[motorpos++]=inchar;
							}
							break;
						case '{':
							addrcnt=0;
							lnspos=0;
							motorpos=0;
							inlns=0;
							inmtr=0;
     							memset(motorbuf,0,sizeof(motorbuf));
     							memset(lnsbuf,0,sizeof(lnsbuf));
							break;
						case '<':
							inlns=1;
							lnsbuf[lnspos++]=inchar;
							break;
						case '>':
							inlns=2;
							lnsbuf[lnspos++]=inchar;
							break;
						case '$':
							inmtr=1;
							motorbuf[motorpos++]=inchar;
							break;
						case '*':
							inmtr=2;
							motorbuf[motorpos++]=inchar;
							break;
						case '}':
							inmsg=0;
							/*printf("*ENDMSG*\n"); */
							break;
						default:
							if (inlns==1) {
							 if( ((isalnum(inchar))>0) || ((isspace(inchar))>0))
							    	lnsbuf[lnspos++]=inchar;
							}
							if (inmtr==1) {
							  if( ((isalnum(inchar))>0) || ((isspace(inchar))>0))
							   	motorbuf[motorpos++]=inchar;
							}
							break;
					}
					if (inmsg==0) {
						if ((inlns==2) && (lnspos > 6)){
							printf("lnsbuf=%s lnspos=%d\n",lnsbuf,lnspos);
							writeCharsToSerialPort(lnsport, lnsbuf, (sizeof(lnsbuf)));
							inlns=0;
						} 

						if ( (inmtr==2) && (motorpos >3)) {
						      printf("motorbuf=%s motorpos=%d\n",motorbuf,motorpos);
							writeCharsToSerialPort(motorport, motorbuf, (sizeof(motorbuf)));
							inmtr=0;
						} 

					addrcnt=0;
					inlns=0;
					inmtr=0;
					lnspos=0;
					motorpos=0;
     					memset(motorbuf,0,sizeof(motorbuf));
     					memset(lnsbuf,0,sizeof(lnsbuf));
					}
						
				} 
	     		}
			
		}									
}
