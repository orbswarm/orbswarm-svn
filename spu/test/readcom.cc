// ---------------------------------------------------------------------
// 
//	File: readcom.cc
//      SWARM Orb SPU code http://www.orbswarm.com
//	Simple test of SPU serial port by echoing data read from it to stdout
//
//
//      usage: readcom <port> 
//      where <port> is an integer, eg readcom 2 to read  COM2
//
//	Written by Jonathan Foote (Head Rotor at rotorbrain.com)
// -----------------------------------------------------------------------
#include  <stdio.h>    /* Standard input/output definitions */
#include  <unistd.h>
#include  <sys/ioctl.h>
#include  <sys/types.h>
#include  <sys/time.h>
#include  <sys/select.h>
#include "../include/swarmserial.h"
#include "../include/swarmdefines.h"

// readcom.c: test com port N (given on command line)
// echo any input to stdout
// usage: readcom n to read port COMn

#define MAXCOMPORTS 5
int main(int argc, char *argv[]) 
{
  int com=0; /* File descriptor for the port */
  int n;
  char buff[MAX_BUFF_SZ + 1];
  int bytes;
  int             max_fd;
  /* this array holds the device paths of the com ports */
  /* (#defined in swarmserial.h) */
  static char *comdev[5] = {COM1,COM2,COM3,COM4,COM5};
  fd_set          input;
  struct timeval tv;

  if (argc != 2) {
    fprintf(stderr, "usage: readcom <port>\n where <port> is an integer\n");
    return(-1);
  }    
  n = atoi(argv[1]) - 1;
  if ((n < 0) || (n >= MAXCOMPORTS)) {
    fprintf(stderr, "COM port %d out of range\n",n+1);
    return(-1);
  }
  else {
    fprintf(stderr, "opening COM%d (%s)\n",n+1,comdev[n]);
  }

  com = initSerialPort(comdev[n], 38400);

  max_fd = com;

  while(1){
    bytes=0;
    readCharsFromSerialPort(com, buff, &bytes,MAX_BUFF_SZ); 
    if(bytes) {
      buff[bytes+1] = '\0';
      printf("\n Read \"%s\" from  COM%d\n",buff,n+1);
    }
  }
  while(1)
  {
         /* Initialize the input set */
     FD_ZERO(&input);
     FD_SET(com, &input);

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
       printf("No data within 10 seconds. waiting...\n");
     }
     else
     {
      if (FD_ISSET(com, &input))
      {
        //Read data from com2
        readCharsFromSerialPort(com, buff, &bytes,MAX_BUFF_SZ); 
        buff[bytes+1] = '\0';
        printf("\n Read \"%s\" from  COM%d\n",buff,n+1);
       }
    }
  }
} //END main() 

    
