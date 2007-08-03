#include  <stdio.h>    /* Standard input/output definitions */
#include  <unistd.h>
#include  <sys/ioctl.h>
#include  <sys/types.h>
#include  <sys/time.h>
#include  <sys/select.h>
#include "../include/swarmserial.h"
#include "../include/swarmdefines.h"
#include "../include/swarmspuutils.h"

#define GPS_START_DELIM "\n{"
#define GPS_STOP_DELIM "}\n"

#define VERBOSE 1

int main(int argc, char *argv[]) 
{

  int com1=0; /* File descriptor for the port */
  int com2=0; /* File descriptor for the port */
  int com3=0, com4=0, com5=0; 	/* ditto */
  int n;
  int tenHzticks = 0;
  char buffer[MAX_BUFF_SZ + 1];

  char *gps_start_str = GPS_START_DELIM; // string to indicate start of GPS data
  char *gps_stop_str = GPS_STOP_DELIM; // string to indicate end of GPS data
  int bytes2 = 0;
  char *gpsQueryString = "$Ag*$";
  
  int             max_fd;
  fd_set          input;
  struct timeval tv;
  //  com1 = initSerialPort("/dev/ttyAM0", 38400);
  com2 = initSerialPort("/dev/ttyAM1", 38400);
  com4 = initSerialPort(COM4, 38400);
  com5 = initSerialPort(COM5, 38400);

  // need to find max fd for select()
  max_fd = (com2 > com1 ? com2 : com1) + 1;
  max_fd = (com3 > max_fd ? com3 : max_fd) + 1;
  max_fd = (com4 > max_fd ? com4 : max_fd) + 1;
  max_fd = (com5 > max_fd ? com5 : max_fd) + 1;


  // main loop
  while(1)
  {
    /* Initialize the input set */
     FD_ZERO(&input);
     FD_SET(com2, &input); // COM2 talks to the aggregator
     FD_SET(com4, &input); // COM4 talks to the GPS now
                           // (eventually it will be sound & LED: output only)
     FD_SET(com5, &input); // COM5 talks to the motor controller

     // set a 10 hz timeout for the main loop
     tv.tv_sec = 0; 
     tv.tv_usec = 100000; // 100 ms or 10 hz

     /* Do the select */
     n = select(max_fd, &input, NULL, NULL,&tv);

     /* test for errors */
     if (n <0){
       printf("Error during select\n");
       continue;
     }

     ///////////////////////////// MAIN LOOP //////////////////////////
     if(!n){ // no I/O activity, so handle main loop here
       
       ++tenHzticks;
       if(tenHzticks == 5) {
	 toggleSpuLed(SPU_LED_RED_ON);  
	      
       }

       if(tenHzticks == 10) {
	 tenHzticks = 0;
	 toggleSpuLed(SPU_LED_RED_OFF);  
       }


       // Poll aggregator to get IMU data
       writeCharsToSerialPort(com2, gpsQueryString, strlen(gpsQueryString));
       readCharsFromSerialPort(com2, buffer, &bytes2,MAX_BUFF_SZ); 
       //
	 buffer[bytes2+1] = '\0';
	 if(bytes2){
	 if (VERBOSE) printf("\n GPS sentence is \"%s\"\n",buffer);
	 }
       //
       //now parse GPS sentence
       //
       swarmGpsData * gpsdata = NULL;
       int status = SWARM_SUCCESS;
       gpsdata = (swarmGpsData*) malloc(sizeof(struct swarmGpsData)); 
       strcpy(gpsdata->gpsSentence,buffer);
       status = parseGPSSentence(gpsdata);
       if(status == SWARM_SUCCESS)
	 { 
	   if(VERBOSE)
	     printf("\n Parsed line %s \n",gpsdata->gpsSentence);
	   status = convertNMEAGpsLatLonDataToDecLatLon(gpsdata);
	   if(status == SWARM_SUCCESS)
	     {
	       if(VERBOSE)
		 printf("\n Decimal lat:%Lf lon:%Lf utctime:%s \n",gpsdata->latdd,gpsdata->londd,gpsdata->nmea_utctime);
          
	       decimalLatLongtoUTM(WGS84_EQUATORIAL_RADIUS_METERS, WGS84_ECCENTRICITY_SQUARED, gpsdata);
	        if(VERBOSE)
		  printf("Northing:%f,Easting:%f,UTMZone:%s\n",gpsdata->UTMNorthing,gpsdata->UTMEasting,gpsdata->UTMZone);
	     }
        
	 }
       else
	 printf("\n Failed GPS parse status=%i", status);
       free(gpsdata);
       //
       //end GPS parse
       
       if (VERBOSE){ 
	 printf("main loop tick %d\n",tenHzticks);
	 fflush(stdout);
       }




     }
     else { // we have I/O so deal with it 
       if (FD_ISSET(com2, &input)) // if serial input on COM2
	 {
	   
	   // Right now this is a straight pipe from COM2 to COM5
	   // so the zigbee can talk to the motor controller directly
	   //Read data from com2
	   readCharsFromSerialPort(com2, buffer, &bytes2,MAX_BUFF_SZ); 
	   buffer[bytes2+1] = '\0';
	   if(bytes2){
	     if (VERBOSE) printf("\n Read \"%s\" from  com2\n",buffer);
	     
	     //write data to com5
	     if (VERBOSE) printf("\nWriting back to com5 \n");
	   }	
	 }
       if(FD_ISSET(com4, &input)){ // if serial input on COM4
	 
	 // Right now COM4 is a straight pipe back to COM2
	 // so the GPS can send data back through the zigbee. 
	 // GPS data is encapsulated with GPS delimiter. 
	 // when aggregator is aggregating COM4 will be output for LEDs
	 
	 //Read data from com4
	 readCharsFromSerialPort(com4, buffer, &bytes2,MAX_BUFF_SZ); 
	 buffer[bytes2+1] = '\0';
	 if (VERBOSE) printf("\n Read the data: \"%s\" from serial port com4\n",buffer);
	 //FD_CLR(com4,&input);
	 
	 //write data to com2
	 if (VERBOSE) printf("\nWriting \"%s\" data back to com2 \n",buffer);
	 writeCharsToSerialPort(com2, gps_start_str, strlen(gps_start_str));
	 writeCharsToSerialPort(com2, buffer, bytes2);
	 writeCharsToSerialPort(com2, gps_stop_str, strlen(gps_stop_str));
       }
       
       if(FD_ISSET(com5, &input)){
	 // if input from motor controller (in response to status 
	 // commands), send it back to the zigbee (aggregator)
	 
	 //Read data from com5
	 readCharsFromSerialPort(com5, buffer, &bytes2,MAX_BUFF_SZ); 
	 buffer[bytes2+1] = '\0';
	 if (VERBOSE) printf("\n Read data: \"%s\"  com5\n",buffer);
	 //FD_CLR(com5,&input);
	 
	 if (VERBOSE) printf("\nWriting back to com2 \n");
	 writeCharsToSerialPort(com2, buffer, bytes2);
	 
       }
     }
  }
} //END main() 

    
