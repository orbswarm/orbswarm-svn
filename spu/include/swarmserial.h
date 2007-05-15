#include <stdio.h>    /* Standard input/output definitions */
#include <stdlib.h> 
#include <stdint.h>   /* Standard types */
#include <string.h>   /* String function definitions */
#include <unistd.h>   /* UNIX standard function definitions */
#include <fcntl.h>    /* File control definitions */
#include <errno.h>    /* Error number definitions */
#include <termios.h>  /* POSIX terminal control definitions */
#include <sys/ioctl.h>
#include <getopt.h>
#include "../include/swarmdefines.h"

#ifndef SWARM_SERIAL_H 
#define SWARM_SERIAL_H 

int initSerialPort(const char* port, int baud);

void readCharsFromSerialPort(int port_fd, char* buff, 
                            int* numBytesRead, int maxBufSz);

int writeCharsToSerialPort(int port_fd, char* buff,
                                        int numBytesToWrite);

#endif

