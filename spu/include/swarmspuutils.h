#include<unistd.h>
#include<sys/types.h>
#include<sys/mman.h>
#include<stdio.h>
#include<fcntl.h>
#include<string.h>
#include <sys/stat.h>			/* declare the 'stat' structure	*/
#include <time.h>
#include <errno.h>
#include "swarmdefines.h"
#include "swarmserial.h"
#include <stdlib.h>
#include <math.h>

#include <sys/socket.h>
#include <net/if.h>
#include <net/ethernet.h>
#include <arpa/inet.h>
#include  <sys/ioctl.h>

/* This is a collection of general utility methods for working with the spu */
#ifndef SWARM_SPU_UTILS_H 
#define SWARM_SPU_UTILS_H 
/************************
 * Sets the indicated spu led state as defined in swarmdefines.h
 */
int toggleSpuLed(const unsigned int ledState);  
//void set_led(int led);
int resetOrbMCU(void);

//pass NULL for log dir to use default path defined in swarmdefines.h
int spulog(char* msg, int msgSize, char* logdir);

int getMessageType(char* message);

void genSpuDump(char* logBuffer, int maxBufSz, swarmGpsData *gpsData, spuADConverterStatus *adConverterStatus, swarmMotorData* motorData);

/*******************************************************************************
 * 
 *******************************************************************************/
int getMessageForDelims(char* msgBuff, int maxMsgSz, int* msgSize, char* input,
                                    int inputSz, char startDelim, char endDelim, bool incDelimsInMsg); 

/* retrieve IP address via ioctl interface */
int getIP(const char *Interface, char *ip);

#endif
