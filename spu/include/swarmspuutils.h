#include<unistd.h>
#include<sys/types.h>
#include<sys/mman.h>
#include<stdio.h>
#include<fcntl.h>
#include<string.h>
#include "swarmdefines.h"

/* This is a collection of general utility methods for working with the spu */
#ifndef SWARM_SPU_UTILS_H 
#define SWARM_SPU_UTILS_H 
/************************
 * Sets the indicated spu led state as defined in swarmdefines.h
 */
int toggleSpuLed(const unsigned int ledState);  
//void set_led(int led);
int resetOrbMCU(void);

#endif
