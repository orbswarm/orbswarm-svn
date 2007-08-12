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
	int i;
	
	while(1) {
		for(i = 0; i < 5; i++) {
			printf("channel %d : %3.3fV\r\n", i, get_ADC_channel(i, 3.3, 2)); 
		}
		
		puts("");
	}

	shutdownADC();

	return 0;
}

