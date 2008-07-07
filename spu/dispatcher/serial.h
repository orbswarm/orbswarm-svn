// ---------------------------------------------------------------------
// 
//	File: swarmserial.h
//      SWARM Orb SPU code http://www.orbswarm.com
//      prototypes and #defs for swarm serial com routines
//
// -----------------------------------------------------------------------


#include <stdio.h>    /* Standard input/output definitions */
#include <stdlib.h> 
#include <stdint.h>   /* Standard types */
#include <string.h>   /* String function definitions */
#include <unistd.h>   /* UNIX standard function definitions */
#include <fcntl.h>    /* File control definitions */
#include <errno.h>    /* Error number definitions */
#include <termios.h>  /* POSIX terminal control definitions */
#include <sys/ioctl.h>


#ifndef SWARM_SERIAL_H 
#define SWARM_SERIAL_H 

/* defines for TS-7800 serial ports */

#define COM1 "/dev/ttyS0"
#define COM2 "/dev/ttyS1"
#define COM3 "/dev/ttts4"
#define COM4 "/dev/ttts5"
#define COM5 "/dev/ttts6"

/*define 7260 serial ports */

//#define COM1 "/dev/ttyAM0"
//#define COM2 "/dev/ttyAM1"
//#define COM3 "/dev/ttyTS0"
//#define COM4 "/dev/ttyTS1"
//#define COM5 "/dev/ttyTS2"

int initSerialPort(const char* port, int baud);

int initSerialPortBlocking(const char* port, int baud);

int readCharsFromSerialPortBlkd(int port_fd, char* buff, int maxBufSz);

int readCharsFromSerialPort(int port_fd, char* buff, int maxBufSz);

int writeCharsToSerialPort(int port_fd, char* buff,
                                        int numBytesToWrite);

void flushSerialPort(int port_fd);


#endif

