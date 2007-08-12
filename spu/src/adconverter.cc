/*

Routines for reading from the Ad converter on the SPU

*/
#include<unistd.h>
#include<sys/types.h>
#include<sys/mman.h>
#include<stdio.h>
#include<fcntl.h>
#include<string.h>
#include <sys/stat.h>			/* declare the 'stat' structure	*/
#include <time.h>
#include <errno.h>
#include <stdlib.h>
#include <math.h>
#include <assert.h>

#include "../include/adconverter.h"
#include "../include/peekpoke.h"
#include "../include/eeprog.h"
#include "../include/ep93xx_adc.h"

//nclude "../include/swarmspuutils.h"

/* globals  - used to generate vals from mmap in order to access the AD port(s)  */
static unsigned long dr_page, adc_page, syscon_page, pld_page;
static int __adc_devmem;
/*
startup routine for using the AD converter. This will return a handle to mmap'ed address space, which will need  to have close() called on it when 
access to the AD converter is no longer needed
*/	
void startupADC() {
	int __adc_devmem = open("/dev/mem", O_RDWR|O_SYNC);
	assert(__adc_devmem != -1);

	dr_page = (unsigned long)mmap(0, getpagesize(), PROT_READ|PROT_WRITE, MAP_SHARED, __adc_devmem, DATA_PAGE);
	assert(&dr_page != MAP_FAILED);
	
	spistart = (unsigned long)mmap(0, getpagesize(), PROT_READ|PROT_WRITE, MAP_SHARED, __adc_devmem, SPI_PAGE);
	assert(&spistart != MAP_FAILED);

	adc_page = (unsigned long)mmap(0, getpagesize(), PROT_READ|PROT_WRITE, MAP_SHARED, __adc_devmem, ADC_PAGE);
	assert(&adc_page != MAP_FAILED);

	syscon_page = (unsigned long)mmap(0, getpagesize(), PROT_READ|PROT_WRITE, MAP_SHARED, __adc_devmem, SYSCON_PAGE);
	assert(&syscon_page != MAP_FAILED);

	pld_page = (unsigned long)mmap(0, getpagesize(), PROT_READ|PROT_WRITE, MAP_SHARED, __adc_devmem, PLD_PAGE);
	assert(&pld_page != MAP_FAILED);

	//alarm(10);  //this program has 10 seconds to complete

	init_ADC(adc_page, syscon_page);

	//return __adc_devmem;
}

// ends the ADC session
void shutdownADC() {
	close(__adc_devmem);
}


/*
reads in calibration values from the eeprom for use in the retrieval routine. So far this has not been needed by the SPU code.
returns a rank 2 array of values for use by get_ADC_channel() , et alia
*/
void calibrate_ADC(int **stored_cal) {
#ifdef DO_CALIBRATION
	int i;
	int virgin = TRUE;
	unsigned char buffer[20];
	int addr;
	
	/* intialize the eeprom */
	POKE16(pld_page, (PEEK16(pld_page) & ~CS_MSK)); //disable CS
	POKE32((spistart + SSPCR1), 0x10);  //turn on transmit
	while ((PEEK32(spistart + 0xc) & 0x10) == 0x10); // wait for unbusy
	while ((PEEK32(spistart + 0xc) & 0x5) != 0x1);   // empty FIFO's
	POKE16(pld_page, (PEEK16(pld_page) | CS_MSK));   //enable CS

	POKE32(spistart, 0xc7);                          // set SPI mode 3, 8bit
	POKE32(spistart + 0x10, 0x2);                    // divide clk by 2
	POKE32(spistart + 0x4, 0x0);                     // stop transmit

	while((ee_rdsr(dr_page) & 0x1) == 0x1);//wait for unbusy

	ee_wren(dr_page);
	ee_wrsr(dr_page, 0x0);              // make eeprom R/W

	while((ee_rdsr(dr_page) & 0x1) == 0x1);//wait for unbusy

	/* Read in the calibration values */
	printf("Calibration Values = \n[ ");

	addr = CALIB_LOC;
	for(i = 0; i < 20; i++)
	{
		ee_read_byte(dr_page, addr, &buffer[i]);
		
		//check if board has stored calibration values
		if(buffer[i] != 0xFF) {
			virgin = FALSE;
		}
		
		addr++;
	}
	
	//convert to 16 bit values
	j = 0;
	ch = 0;
	for(i = 0; i < 20; i = i + 2)
	{
		if(i == 10)
		{
			ch = 0;
			j = 1;
		}

		stored_cal[ch][j] = (buffer[i] | (buffer[i+1] << 8));
		printf("0x%x ", stored_cal[ch][j]);
		ch++;
	}
	printf(" ]\n");

	if(virgin == TRUE)
		printf("No calibration values found...\n");
	else
		printf("Board has been calibrated by Technologic Systems...\n");

	#endif

	// read code was here in the sample code
	
#ifdef DO_CALIBRATION
	//make eeprom RO
	ee_wren(dr_page);
	ee_wrsr(dr_page, 0x1c);
#endif	
	
	
} // calibrate_adc


/******************************************************************************
*DESCRIPTION: In eeprom 2027->2046 are the calibration values. 2027->2036
*are the zero volt calibration values and 2037->2046 are the 2.5V calibration
*values. Each calibration value is 16 bits written using little endian
*
* maxVoltage defaults to 3.3. in the sample code
*******************************************************************************/
double get_ADC_channel(int channel, double maxVoltage, int numOfSamples)
{
	double val;
	double full_scale;
	register int j;
	register int avg;
	//int ch;
	int adc_result[numOfSamples]; // read results
	int stored_cal[5][2];  //stored calibration values
	int virgin = TRUE;

	calibrate_ADC((int**)stored_cal);

	//Read the adc values for all 5 channels
	read_7xxx_adc((int*)adc_result, channel, numOfSamples);

	//Convert to voltage
	//for(i = 0; i < 5; i++)
	{
		avg = 0;
		
		full_scale = (((((double)(stored_cal[channel][1] + 0x10000) - stored_cal[channel][0]) / 2.5 ) * maxVoltage)); 

		for(j = 0; j < numOfSamples; j++) {
			avg += adc_result[j];
		}

		avg /= numOfSamples;
			
		if(avg < 0x7000) {
			avg += 0x10000;
		}

		if(virgin == TRUE)  //use approximation
		{
			avg -= 0x9E58;
			val = ((double)avg * maxVoltage) / 0xC350;
		}
		else                //use calibration values
		{
			avg -= stored_cal[channel][0];
			val = ((double)avg * maxVoltage) / full_scale;
		}
	}
		
   return val;
}



/************************************************************************
 *DESCRIPTION: Read all five of the EP93xx onboard ADC. Discard the first
 *two samples then save the next numOfSamples.
 *note - there is no wait if just one sample is specified
 ***********************************************************************/
static void read_7xxx_adc(int *adc_result, int channel, int numOfSamples)
{
	register int j;
	int	cur_ch;

	cur_ch = ((int[]){ADC_CH0, ADC_CH1, ADC_CH2, ADC_CH3, ADC_CH4 })[channel];

	for(j = 0; j < numOfSamples; j++)  // used to iterate ten times, we care less about spikes and bounce and more about speed RMT 8/9/2007
	{
		if (numOfSamples > 1) {
			usleep(10000); 
		}
		
		adc_result[j] = read_channel(adc_page, cur_ch);
	}
} // read_7xxx_adc

