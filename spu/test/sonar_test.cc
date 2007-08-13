#include <unistd.h>
#include <sys/types.h>
#include <sys/mman.h>
#include <stdio.h>
#include <stdlib.h>
#include <fcntl.h>
#include <assert.h>

//#include "include/swarmspuutils.h"
#include "../include/swarmspuutils.h"
#include "../include/adconverter.h"

#define DATA_PAGE 0x80840000
#define CALIB_LOC    2027          //location of calibration values

int main(void)
{
	startupADC();
	
	while(1) {
			printf("Sonar : %3.3f inches\r\n", get_sonar()); 
	}

	shutdownADC();

	return 0;
}

