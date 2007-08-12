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
  
  swarmGpsData trajPoints[50];

  com2 = initSerialPort("/dev/ttyAM1", 38400); //Aggregator
  com3 = initSerialPort(COM3, 38400); //Lighting and Effects
  com5 = initSerialPort(COM5, 38400); //Motor Controller

  // main loop
  while(1)
  {
     ///////////////////////////// MAIN LOOP //////////////////////////
     // set a 10 hz timeout for the main loop
     tv.tv_sec = 0; 
     tv.tv_usec = 100000; // 100 ms or 10 hz

     FD_ZERO(&fdset);

     n = select(max_fd, &fdset, NULL, NULL,&tv);

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



      // Poll aggregator to get IMU data
       printf("START GPS TRANSACTION**********\n"); 
       writeCharsToSerialPort(com5, "$QI*", strlen("$QI*"));
       readCharsFromSerialPort(com5, buffer, &bytes2,MAX_BUFF_SZ); 

       
       if(bytes2 > 1) { //only handle the IMU data if we have it
	 printf("IMU data str: \"%s\"\n",buffer);
	 status = parseImuMsg(buffer,&imuData);
	 if(VERBOSE) printf("Parse status %d\n",status);
	 imuIntToSI(&imuData);
	 if(VERBOSE) dumpImuData(&imuData);
       }


       // Poll aggregator to get IMU data
       printf("START GPS TRANSACTION**********\n"); 




       //Tell the agg that we want whatever gps data it has to give 
       writeCharsToSerialPort(com2, AGGR_GPS_QUERY_CMD, strlen(AGGR_GPS_QUERY_CMD));
       readCharsFromSerialPortUntilAck(com2, buffer, &bytes2, MAX_BUFF_SZ, 10, AGGR_DATA_XFER_ACK);

       if (VERBOSE) printf("\n GPS sentence is \"%s\"\n",buffer);
      
         //
         //we recieved data now parse GPS sentence
         //
       parseAndConvertGPSData(buffer, &currentOrbLoc); 

       //Tell the agg that we want whatever zigbee data it has to give 
       writeCharsToSerialPort(com2, AGGR_ZIGBEE_QUERY_CMD, strlen(AGGR_ZIGBEE_QUERY_CMD));

       readCharsFromSerialPortUntilAck(com2, buffer, &bytes2, MAX_BUFF_SZ, 10, AGGR_DATA_XFER_ACK);
       if(bytes2 > 1){  //only handle the zigbee data if we have it one byte means only ! 
         //First we have to tokenize the recieved buffer into individual messages
         char* msgptr = NULL;
         int msgcount = 0;
         char zigbeeBufCpy[MAX_BUFF_SZ + 1];
         int messageType = 0;
         strcpy(zigbeeBufCpy, buffer);
         
         fprintf(stderr, "\nBuffer to strtok **%s** for message type\n",zigbeeBufCpy);
         char msgdelim[1];   
         msgdelim[0] = AGGR_MESSAGE_DELIM_END;
         msgptr = strtok(zigbeeBufCpy,msgdelim);
         fprintf(stderr, "\nstrtok returned **%s** for message type\n",msgptr);
         while(msgptr != NULL)
         {
           //do the work 
            
           fprintf(stderr, "\nCALLING getMessageType\n",msgptr);
           messageType = getMessageType(msgptr);
           fprintf(stderr, "\ngot message type **%d** \n",messageType);
             
           switch(messageType)
           {
             case AGGR_MSG_TYPE_MOTHER_SHIP_SPU_POLL: 
              
               fprintf(stderr, "\nHANDELING SPU POLL\n");
               char spuPollData[MAX_BUFF_SZ + 1]; 
               genSpuDump(spuPollData, MAX_BUFF_SZ, &currentOrbLoc);
               packetizeAndSendMotherShipData(com2, spuPollData, strlen(spuPollData));
               
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
           msgptr = strtok(NULL,SWARM_NMEA_GPS_DATA_DELIM);
         }
       
       } //End recieved zigbee data if statement

       if (VERBOSE){ 
	 printf("main loop tick %d\n",tenHzticks);
	 fflush(stdout);
       }
     }
   }
} //END main() 

    
