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

	 // Poll motor controller to get IMU data
	 writeCharsToSerialPort(com5, "$QI*", strlen("$QI*"));
	 readCharsFromSerialPort(com5, buffer, &bytes2,MAX_BUFF_SZ); 
	 
       
	 if(bytes2 > 1) { //only handle the IMU data if we have it
	   printf("IMU data str: \"%s\"\n",buffer);
	   status = parseImuMsg(buffer,&imuData);
	   if(VERBOSE) printf("Parse status %d\n",status);
	   imuIntToSI(&imuData);
	   if(VERBOSE) dumpImuData(&imuData);
	 }
       }

       if (1){ 			// always poll drive motor

	 // Poll  to get drive motor data incldata
	 writeCharsToSerialPort(com5, "$QD*", strlen("$QD*"));
	 readCharsFromSerialPort(com5, buffer, &bytes2,MAX_BUFF_SZ); 
	 
       
	 if(bytes2 > 1) { //only parse the motor data if we have it
	   printf("IMU data str: \"%s\"\n",buffer);
	   status = parseDriveMsg(buffer,&motData);
	   if(VERBOSE) printf("Parse drive %d\n",status);
	   if(VERBOSE) dumpMotorData(&motData);
	 }
       }







       if(0) { // always poll AGGRIGATOR 
       printf("START AGGREGATOR TRANSACTION**********\n"); 

       //memset(gpsBuff,0,MAX_BUFF_SZ);
       //gpsBuff[0] = 0;
       //gpsBytes = 0;
      
       memset(buffer,0,MAX_BUFF_SZ);
       bytes2 = 0;
       //Tell the agg that we want whatever gps data it has to give 
       writeCharsToSerialPort(com2, AGGR_GPS_QUERY_CMD, strlen(AGGR_GPS_QUERY_CMD));
       usleep(20000);
       //readCharsFromSerialPortUntilAck(com2, gpsBuff, &gpsBytes, MAX_BUFF_SZ,100 , AGGR_DATA_XFER_ACK);
       readCharsFromSerialPortUntilAck(com2, buffer, &bytes2, MAX_BUFF_SZ, 100 , AGGR_DATA_XFER_ACK);
  
       //HEY NILADRI PUT CODE HEAR

       if (VERBOSE) printf("\n RAW AGGREGATOR DATA STREAM is \"%s\" SIZE:%d\n",buffer,bytes2);
     
       char* msgptr = NULL;
       int msgcount = 0;
       char msgBufCpy[MAX_BUFF_SZ + 1];
       int messageType = 0;
       strcpy(msgBufCpy, buffer);
       
       //fprintf(stderr, "\nBuffer to strtok **%s** for message\n",zigbeeBufCpy);
       fprintf(stderr, "\nBuffer to strtok **%s** for message\n",msgBufCpy);
       msgptr = strtok(msgBufCpy,AGGR_MESSAGE_DELIM_END);

       fprintf(stderr, "\nstrtok returned **%s** for first part of gps message \n",msgptr);
       //We ARE being very trusing hear that we will get the first gps sentance  
         strcpy(currentOrbLoc.gpsSentence,msgptr);
       
       msgptr = strtok(NULL,AGGR_MESSAGE_DELIM_END);
       fprintf(stderr, "\nstrtok returned **%s** for the second part of gps message \n",msgptr);
       //Here we are expecting the GPS velocity data
         strcpy(currentOrbLoc.vtgSentence,msgptr);

       //
       //we recieved data now parse GPS sentence
       //
       parseAndConvertGPSData(gpsBuff, &currentOrbLoc); 


       //Tell the agg that we want whatever zigbee data it has to give 
      
       //memset(buffer,0,MAX_BUFF_SZ);
       //bytes2 = 0;
       //writeCharsToSerialPort(com2, AGGR_ZIGBEE_QUERY_CMD, strlen(AGGR_ZIGBEE_QUERY_CMD));
       //usleep(20000);
       //readCharsFromSerialPortUntilAck(com2, buffer, &bytes2, MAX_BUFF_SZ, 100, AGGR_DATA_XFER_ACK);
       //fprintf(stderr,"\nZIGBEE RAW DATA :%s NUMBYTES: %d\n",buffer, bytes2);
        

         //We should be done with gps data so now we loop through the zigbee messages 

         msgptr = strtok(NULL,AGGR_MESSAGE_DELIM_END);
         while(msgptr != NULL)
         {
           //do the work 
            
           fprintf(stderr, "\nCALLING getMessageType: %s\n",msgptr);
           messageType = getMessageType(msgptr);
           fprintf(stderr, "\ngot message type **%d** \n",messageType);
             
           switch(messageType)
           {
             case AGGR_MSG_TYPE_MOTHER_SHIP_SPU_POLL: 
              
               		fprintf(stderr, "\nHANDELING SPU POLL\n");
               		char spuPollData[MAX_BUFF_SZ + 1]; 

			getAdConverterStatus(&adConverterStatus, AD_DEFAULT_MAX_VOLTAGE, AD_DEFAULT_PRECISION);
			genSpuDump(spuPollData, MAX_BUFF_SZ, &currentOrbLoc, &adConverterStatus);
               
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
               writeCharsToSerialPort(com5, msgptr, strlen(msgptr));
               break;
           }
           msgcount++; 
           msgptr = strtok(NULL,AGGR_MESSAGE_DELIM_END);
         }
         printf("END AGGREGATOR TRANSACTION**********\n"); 
       }//End poll Agregator
       

       if (VERBOSE){ 
	 //printf("main loop tick %d\n",tenHzticks);
	 fflush(stdout);
       }
     }
   }
} //END main() 

    
