#include  <stdio.h>    /* Standard input/output definitions */
#include  <unistd.h>
#include  <sys/ioctl.h>
#include  <sys/types.h>
#include  <sys/time.h>
#include  <sys/select.h>
#include "../include/swarmserial.h"
#include "../include/swarmdefines.h"

#define GPS_START_DELIM "\n{"
#define GPS_STOP_DELIM "}\n"

int main(int argc, char *argv[]) 
{
  int com1=0; /* File descriptor for the port */
  int com2=0; /* File descriptor for the port */
  int com3=0, com4=0, com5=0; 	/* ditto */
  int n;
  char buff[MAX_BUFF_SZ + 1];
  int bytes;
  char buff2[MAX_BUFF_SZ + 1];

  char procbuf[MAX_BUFF_SZ +1];

  char *gps_start_str = GPS_START_DELIM; // string to indicate start of GPS data
  char *gps_stop_str = GPS_STOP_DELIM; // string to indicate end of GPS data
  int bytes2 = 0;
  int             max_fd;
  fd_set          input;
  struct timeval tv;

  //  com1 = initSerialPort("/dev/ttyAM0", 38400);
  com2 = initSerialPort("/dev/ttyAM1", 38400);
  //com3 = initSerialPort(COM3, 38400);
  com4 = initSerialPort(COM4, 38400);
  com5 = initSerialPort(COM5, 38400);

  max_fd = (com2 > com1 ? com2 : com1) + 1;
  max_fd = (com3 > max_fd ? com3 : max_fd) + 1;
  max_fd = (com4 > max_fd ? com4 : max_fd) + 1;
  max_fd = (com5 > max_fd ? com5 : max_fd) + 1;

  while(1)
  {
         /* Initialize the input set */
     FD_ZERO(&input);
     FD_SET(com2, &input);
     FD_SET(com4, &input);
     FD_SET(com5, &input);

     tv.tv_sec = 10; // set timeouts for select()
     tv.tv_usec = 0;


     /* Do the select */
     n = select(max_fd, &input, NULL, NULL,&tv);

     /* See if there was an error */

     if (n <0){
       printf("Error during select\n");
       continue;
     }
     if(!n){
       printf("No data within 10 secs.");
     }
     else
     {
      if (FD_ISSET(com2, &input))
      {
        //Read data from com2
        readCharsFromSerialPort(com2, buff2, &bytes2,MAX_BUFF_SZ); 
        buff2[bytes2+1] = '\0';
        printf("\n Read \"%s\" from  com2\n",buff2);


	parseLSMP(buff2,(sizeof(buff2)),com5,com2); 
	/* 
        printf("\nWriting back to com5 \n");
        //writeCharsToSerialPort(com5, buff2, bytes2);
	 */
      }

      /* We have input */
      if(FD_ISSET(com4, &input))
      {
        //Read data from com5
        readCharsFromSerialPort(com4, buff2, &bytes2,MAX_BUFF_SZ); 
        buff2[bytes2+1] = '\0';
        printf("\n Read the data: \"%s\" from serial port com4\n",buff2);
	//FD_CLR(com4,&input);

        //write data to com2
        printf("\nWriting \"%s\" data back to com2 \n",buff2);
        writeCharsToSerialPort(com2, gps_start_str, strlen(gps_start_str));
        writeCharsToSerialPort(com2, buff2, bytes2);
        writeCharsToSerialPort(com2, gps_stop_str, strlen(gps_stop_str));
      }

      /* We have input */
      if(FD_ISSET(com5, &input))
      {
        //Read data from com5
        readCharsFromSerialPort(com5, buff2, &bytes2,MAX_BUFF_SZ); 
        buff2[bytes2+1] = '\0';
        printf("\n Read data: \"%s\"  com5\n",buff2);
	//FD_CLR(com5,&input);

	parseLSMP(buff2,(sizeof(buff2)),com5,com2); 
        //write data to com2
        //printf("\nWriting back to com2 \n");
        //writeCharsToSerialPort(com2, buff2, bytes2);

      }
    }
  }
} //END main() 

    
