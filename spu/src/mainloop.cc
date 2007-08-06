#include  <stdio.h>    /* Standard input/output definitions */
#include  <unistd.h>
#include  <sys/ioctl.h>
#include  <sys/types.h>
#include  <sys/time.h>
#include  <sys/select.h>
#include "../include/swarmserial.h"
#include "../include/swarmdefines.h"
#include "../include/swarmspuutils.h"

#define VERBOSE 1

int main(int argc, char *argv[]) 
{

  int com1=0; /* File descriptor for the port */
  int com2=0; /* File descriptor for the port */
  int com3=0, com4=0, com5=0; 	/* ditto */
  int tenHzticks = 0;
  char buffer[MAX_BUFF_SZ + 1];
  int bytes2 = 0;
  struct timeval tv;
  int status = SWARM_SUCCESS;
  int             max_fd;
  fd_set          fdset;
  int n =0; 

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

       //Tell the agg that we want whatever gps data it has to give 
       writeCharsToSerialPort(com2, AGGR_GPS_QUERY_CMD, strlen(AGGR_GPS_QUERY_CMD));
       readCharsFromSerialPort(com2, buffer, &bytes2,MAX_BUFF_SZ); 

       if(bytes2 > 1) //only handle the gps data if we have it
       {  
         if(buffer[bytes2] != AGGR_DATA_XFER_ACK)
         { 
           //We didn't get all of the data so we keep reading until we do
           int total_gps_bytes = 0;
           total_gps_bytes = bytes2;
           while(1)  
           {
             printf("CALLING GPS READ A SECOND TIME\n"); 
             readCharsFromSerialPort(com2, &buffer[total_gps_bytes], &bytes2,MAX_BUFF_SZ - total_gps_bytes); 
             total_gps_bytes += bytes2;
             printf("NUM GPS BYTES READ: %d\n",total_gps_bytes); 

             if(buffer[total_gps_bytes] == AGGR_DATA_XFER_ACK)
               break; //we got it all so get out of the read loop 
           }
            bytes2 = total_gps_bytes; 
         } 
         buffer[bytes2+1] = '\0';
	 if (VERBOSE) printf("\n GPS sentence is \"%s\"\n",buffer);
      
         //
         //we recieved data now parse GPS sentence
         //

         char* sentptr = NULL;
         int gpscnt = 0;
         char gpsBufCpy[MAX_BUFF_SZ + 1];
         strcpy(gpsBufCpy, buffer);
         char gpsdelim[1];   
          gpsdelim[0] = AGGR_MESSAGE_DELIM_END;
         sentptr = strtok(gpsBufCpy,gpsdelim);
         while(sentptr != NULL)
         {
           switch(gpscnt)
           {
             case 0: 
               status = SWARM_SUCCESS;
               strcpy(currentOrbLoc.gpsSentence,sentptr);
               status = parseGPSSentence(&currentOrbLoc);
               if(status == SWARM_SUCCESS)
               { 
        	   if(VERBOSE)
        	     printf("\n Parsed line %s \n",currentOrbLoc.gpsSentence);
        	   status = convertNMEAGpsLatLonDataToDecLatLon(&currentOrbLoc);
        	   if(status == SWARM_SUCCESS)
        	     {
        	       if(VERBOSE)
        		 printf("\n Decimal lat:%lf lon:%lf utctime:%s \n",currentOrbLoc.latdd,currentOrbLoc.londd,currentOrbLoc.nmea_utctime);
                  
        	       decimalLatLongtoUTM(WGS84_EQUATORIAL_RADIUS_METERS, WGS84_ECCENTRICITY_SQUARED, &currentOrbLoc);
                     if(VERBOSE)
                       printf("Northing:%f,Easting:%f,UTMZone:%s\n",currentOrbLoc.UTMNorthing,currentOrbLoc.UTMEasting,currentOrbLoc.UTMZone);
        	     }
               }
               else
               {
        	 if(VERBOSE)
        	   printf("\n Failed GPS parse status=%i", status);
               }
             break;//end GPS Lat/Lon parse

             case 1:   // Parse the gps velocity data 
               status = SWARM_SUCCESS;
               strcpy(currentOrbLoc.vtgSentence,sentptr);
               status = parseGPSSentence(&currentOrbLoc);
               if(status == SWARM_SUCCESS)
               { 
        	 if(VERBOSE)
        	   printf("\n Parsed line %s \n",currentOrbLoc.vtgSentence);
               }
               else
               {
	         if (VERBOSE)
        	   printf("\n Failed VTG gps parse status=%i", status);
               }
             break;//end GPS VTG data parse 
           }
           gpscnt++; 
           sentptr = strtok(NULL,SWARM_NMEA_GPS_DATA_DELIM);
         }
       }
       
       //Tell the agg that we want whatever zigbee data it has to give 
       writeCharsToSerialPort(com2, AGGR_ZIGBEE_QUERY_CMD, strlen(AGGR_ZIGBEE_QUERY_CMD));
       readCharsFromSerialPort(com2, buffer, &bytes2,MAX_BUFF_SZ); 
       if(bytes2 > 1){  //only handle the zigbee data if we have it one byte means only ! 
         buffer[bytes2+1] = '\0';

	 if (VERBOSE)
           printf("\n Raw Zigbee data from Aggregator \"%s\"\n",buffer);

         //Now we examine the last character of the read data to see if we have gotten all of the 
         //data in the first read
         if(buffer[bytes2] != AGGR_DATA_XFER_ACK)
         { 
           //We didn't get all of the data so we keep reading until we do
           int total_zigbee_bytes = 0;
           total_zigbee_bytes = bytes2;
           while(1)  
           {
             printf("CALLING ZIGBEE READ A SECOND TIME\n"); 
             readCharsFromSerialPort(com2, &buffer[total_zigbee_bytes], &bytes2,MAX_BUFF_SZ - total_zigbee_bytes); 
             total_zigbee_bytes += bytes2;
             printf("NUM ZIGBEE BYTES READ: %d\n",total_zigbee_bytes); 

             if(buffer[total_zigbee_bytes] == AGGR_DATA_XFER_ACK)
               break; //we got it all so get out of the read loop 
           }
            bytes2 = total_zigbee_bytes; 
         } 
         
         //Now we have all of the data so lets do some work

         //First we have to tokenize the recieved buffer into individual messages
         char* msgptr = NULL;
         int msgcount = 0;
         char zigbeeBufCpy[MAX_BUFF_SZ + 1];
         int messageType = 0;
         strcpy(zigbeeBufCpy, buffer);
         char msgdelim[1];   
         msgdelim[0] = AGGR_MESSAGE_DELIM_END;
         msgptr = strtok(zigbeeBufCpy,msgdelim);
         //fprintf(stderr, "\nstrtok returned **%s** for gps sentence type\n",gpsdata->gpsSentenceType);
         while(msgptr != NULL)
         {
           //do the work 
           messageType = getMessageType(msgptr);
           switch(messageType)
           {
             case AGGR_MSG_TYPE_MOTHER_SHIP_SPU_POLL: 
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

    
