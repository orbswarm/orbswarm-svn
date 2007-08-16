#include  <stdio.h>    /* Standard input/output definitions */
#include  <unistd.h>
#include  <sys/ioctl.h>
#include  <sys/types.h>
#include  <sys/time.h>
#include  <sys/select.h>
#include "../include/swarmserial.h"
#include "../include/swarmdefines.h"
#include "../include/swarmspuutils.h"
#include "../include/swarmGPSutils.h"
#include "../include/swarmIMUutils.h"
#include "../include/adconverter.h"
#include "../include/state_mc.h"
#define foo

int main(int argc, char *argv[]) 
{

  int com1=0; /* File descriptor for the port */
  int com2=0; /* File descriptor for the port */
  int com3=0, com4=0, com5=0; 	/* ditto */
  int tenHzticks = 0;
  pid_t mypid=0;
  char buffer[MAX_BUFF_SZ + 1];
  int bytes2 = 0;
  char gpsBuff[MAX_BUFF_SZ + 1];
  int gpsBytes = 0;
  struct timeval tv;
  int status = SWARM_SUCCESS;
  int             max_fd;
  fd_set          fdset;
  int n =0; 
  char *logdir ="/tmp";
  char curtime[30];
  char logbuf[80];
  time_t logtime;
  swarmImuData imuData;
  swarmMotorData motData;	// holds data from motor controller
  
  mypid=getpid();
  time(&logtime);
  strncpy(curtime,(ctime(&logtime)),26);
  snprintf(logbuf,80,"mainloop[%d]:started at %s\n",mypid,curtime);
  spulog(logbuf,80,logdir);

  // need to find max fd for select()
  max_fd = (com2 > com1 ? com2 : com1) + 1;
  max_fd = (com3 > max_fd ? com3 : max_fd) + 1;
  max_fd = (com4 > max_fd ? com4 : max_fd) + 1;
  max_fd = (com5 > max_fd ? com5 : max_fd) + 1;

  swarmGpsData motherShipLoc; 
  swarmGpsData currentOrbLoc; 
  spuADConverterStatus adConverterStatus;
  swarmGpsData trajPoints[50];

  com2 = initSerialPort(COM2, 38400); //Aggregator
  com3 = initSerialPort(COM3, 38400); //Lighting and Effects
  com5 = initSerialPort(COM5, 38400); //Motor Controller
  printf("\ndone init");
  // main loop
  while(1)
    {
      ///////////////////////////// MAIN LOOP //////////////////////////
      // set a 10 hz timeout for the main loop
      tv.tv_sec = 0; 
      tv.tv_usec = 200000; // 100 ms or 10 hz

      FD_ZERO(&fdset);

      //n = select(max_fd, &fdset, NULL, NULL,&tv);
      n = 0;
      if (n <0){
	printf("Error during select\n");
	snprintf(logbuf,80,"mainloop[%d]:Error during select %s\n",mypid,curtime);
	spulog(logbuf,80,logdir);

	continue;
      }
      if(!n)
	{
	  ++tenHzticks;
	  if(tenHzticks == 5) {
	    toggleSpuLed(SPU_LED_RED_ON);  
	  }

	  if(tenHzticks == 10) {
	    tenHzticks = 0;
	    toggleSpuLed(SPU_LED_RED_OFF);  
	  }


	  if (0){ 			// always poll IMU

	    // Poll aggregator to get IMU data
	    writeCharsToSerialPort(com5, "$QI*", strlen("$QI*"));
	    readCharsFromSerialPort(com5, buffer, &bytes2, MAX_BUFF_SZ); 
	 
       
	    if(bytes2 > 1) { //only handle the IMU data if we have it
	      printf("IMU data str: \"%s\"\n",buffer);
	      status = parseImuMsg(buffer,&imuData);
	      if(VERBOSE) printf("Parse status %d\n",status);
	      imuIntToSI(&imuData);
	      if(VERBOSE) dumpImuData(&imuData);
	    }
	  }

	  if (0){ 			// always poll drive motor

	    // Poll  to get drive motor data incldata
	    writeCharsToSerialPort(com5, "$QD*", strlen("$QD*"));
	    readCharsFromSerialPort(com5, buffer, &bytes2,MAX_BUFF_SZ); 
	 
       
	    if(bytes2 > 1) { //only parse the motor data if we have it
	      printf("drive data str: \"%s\"\n",buffer);
	      status = parseDriveMsg(buffer,&motData);
	      if(VERBOSE) printf("Parse drive %d\n",status);
	      if(VERBOSE) dumpMotorData(&motData);
	      packetizeAndSendMotherShipData(com2, buffer, bytes2);
	    }
	 
	  }


	  if(1) { // always poll AGGRIGATOR 
	    printf("START AGGREGATOR TRANSACTION**********\n"); 
	    
	    //memset(gpsBuff,0,MAX_BUFF_SZ);
	    //gpsBuff[0] = 0;
	    //gpsBytes = 0;
      
	    memset(buffer,0,MAX_BUFF_SZ);

	    bytes2 = 0;
	    //Tell the agg that we want whatever gps data it has to give 
	    writeCharsToSerialPort(com2, AGGR_GPS_QUERY_CMD, strlen(AGGR_GPS_QUERY_CMD));
	    //usleep(20000);
  
	    if(VERBOSE) printf("\nbuffer before reading from com2=%s", buffer);
	    int status = processRawSerial(com2, buffer, &bytes2, MAX_BUFF_SZ, 
					  2, AGGR_DATA_XFER_ACK);
	    if(SWARM_SUCCESS != status){
	      printf("FAILED TO READ XXXXXXXXXXXXXXXX");
	    }
	    else{

	      if (VERBOSE) printf("\n RAW AGGREGATOR DATA STREAM is \"%s\" SIZE:%d\n",buffer,bytes2);
	      //        writeCharsToSerialPort(com2, testbuff, strlen(testbuff));
        
	      char* msgptr = NULL;
	      char* strtok_r_buff = NULL;
	      int msgcount = 0;
	      char msgBufCpy[MAX_BUFF_SZ + 1];
	      int messageType = 0;
	      strcpy(msgBufCpy, buffer);
       

	      if (VERBOSE) printf("\nBuffer to strtok **%s** for message\n",msgBufCpy);
	      msgptr = strtok_r(msgBufCpy, ";",&strtok_r_buff);

	      if (VERBOSE) printf("\nstrtok returned **%s** for first part of gps message \n",msgptr);
	      //We ARE being very trusing hear that we will get the first gps sentance  
	      strcpy(currentOrbLoc.gpsSentence,msgptr);
       
	      msgptr = strtok_r(NULL,";", &strtok_r_buff);
	      if (VERBOSE) printf("\nstrtok returned **%s** for the second part of gps message \n",msgptr);
	      //Here we are expecting the GPS velocity data
	      strcpy(currentOrbLoc.vtgSentence,msgptr);
	      //parseAndConvertGPSData(gpsBuff, &currentOrbLoc); 
	      msgptr = strtok_r(NULL,";", &strtok_r_buff);

	      if (VERBOSE) printf("\nstrtok returned **%s** for the xbee messages \n",msgptr);
	      while(msgptr != NULL)
		{
		  //do the work 
            
		  if (VERBOSE) printf("\nCALLING getMessageType: %s\n",msgptr);
		  messageType = getMessageType(msgptr);
		  if (VERBOSE) printf("\ngot message type **%d** \n",messageType);

		  switch(messageType)
		    {
		    case AGGR_MSG_TYPE_MOTHER_SHIP_SPU_POLL: 
              
		      fprintf(stderr, "\nHANDELING SPU POLL\n");
		      char spuPollData[MAX_BUFF_SZ + 1]; 

		      //			getAdConverterStatus(&adConverterStatus, AD_DEFAULT_MAX_VOLTAGE, AD_DEFAULT_PRECISION);
		      genSpuDump(spuPollData, MAX_BUFF_SZ, &currentOrbLoc, &adConverterStatus);

		      packetize(com2 , spuPollData, strlen(spuPollData));
               
		      break;
             
		    case AGGR_MSG_TYPE_TRAJECTORY: 
		      break;

		    case AGGR_MSG_TYPE_MOTHER_SHIP_LOC: 
		      break;

		    case AGGR_MSG_TYPE_EFFECTS: 
		      //Write the effects string to com3
		      writeCharsToSerialPort(com3, msgptr, strlen(msgptr));
		      break;

		    case AGGR_MSG_TYPE_MOTOR_CONTROL: 
		      printf("\nissuing m/c controller command");
		      writeCharsToSerialPort(com5, msgptr, strlen(msgptr));
		      readCharsFromSerialPort(com5, buffer, &bytes2,MAX_BUFF_SZ); 
		      buffer[bytes2+1]=0;
       
		      if(bytes2 > 1) { //only parse the motor data if we have it
			printf("\ndrive data str: \"%s\"\n",buffer);
			printf("\nnum bytes=%dl" , bytes2);
			status = parseDriveMsg(buffer,&motData);
			if(VERBOSE) printf("Parse drive %d\n",status);
			if(VERBOSE) dumpMotorData(&motData);
			packetize(com2, buffer, bytes2);
			printf("\n sending command %s", buffer );
			printf("\n done sending m/c response");
		      }
		      break;
		    default :
		      fprintf(stderr, "\n###########UNKNOWN MESSAGE TYPE######");
		    }
		  msgcount++; 
		  msgptr = strtok_r(NULL,AGGR_MESSAGE_DELIM_END, &strtok_r_buff);
		}
	    }//End poll Agregator
	    printf("END AGGREGATOR TRANSACTION**********\n"); 
	      
	    //        if (VERBOSE){ 
	    // 	 //printf("main loop tick %d\n",tenHzticks);
	    // 	 fflush(stdout);
	    //        }
	  }
	}
    }
} //END main() 

    
