// ---------------------------------------------------------------------
// 
//	File: testcom.cc
//      SWARM Orb SPU code http://www.orbswarm.com
//	Simple terminal-like program echoes stdin to selected com port
//      echoes back response; use to test/debug stuff on serial ports
//
//      usage: testcom <port> 
//      where <port> is an integer, eg writecom 2 to write to COM2
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

// testcom.c: test com port N (given on command line)
// usage: testcom n to I/O to port COMn

#define MAXCOMPORTS 5
int main(int argc, char *argv[]) 
{
  int com=0; /* File descriptor for the port */
  int n=0;
  int count=0;
  char buff[MAX_BUFF_SZ + 1];
  int bytes;
 
  /* this array holds the device paths of the com ports */
  /* (#defined in swarmserial.h) */
  static char *comdev[5] = {COM1,COM2,COM3,COM4,COM5};

  if (argc != 2) {
    fprintf(stderr, "usage: testcom <port>\n where <port> is an integer\n");
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

  while(1){
    fgets(buff,MAX_BUFF_SZ,stdin);
    //printf("Write %d to COM%d\n",count++,n+1);
    writeCharsToSerialPort(com, buff, strlen(buff));
    //printf("Got %s",buff);
    usleep(100000);
    readCharsFromSerialPort(com, buff, &bytes, MAX_BUFF_SZ);
    if(bytes>0)
      printf("%s",buff);
  }
} //END main() 

    
