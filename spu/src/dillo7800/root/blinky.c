#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>
#include <sys/types.h>
#include <sys/mman.h>
#include <fcntl.h>
#include <assert.h> 
#include <string.h>

//EWD: Clean up this header file...
#include "twsi.h"

volatile unsigned int *data, *control, *status, *led;
unsigned int verbose = 0, addr;

void static inline twi_write(unsigned char dat) {
        if(verbose) fprintf(stderr,"Writing data (0x%x)\n", dat);
        *data = dat;
        usleep(100);

        *control = (*control & ~IFLG) | ACK;
        usleep(100);
        while((*control & IFLG) == 0x0);        // Wait for an I2C event


        if((*status != ST_DATA_ACK) && (*status != MT_DATA_ACK )) {
                if(verbose) fprintf(stderr,"Slave didn't ACK data\n");
        }

        if(verbose) fprintf(stderr,"Slave ACKed data\n");
}

unsigned char static inline twi_read() {


        if(verbose) fprintf(stderr, "Waiting for data from master\n");
        *control = (*control & ~IFLG) | ACK;
        while((*control & IFLG) == 0x0);        // Wait for an I2C event
        if((*status != SR_DATA_ACK) && (*status != MR_DATA_ACK)) {
                if(verbose) fprintf(stderr, "Error reading data from master(0x%x)\n", *status);

        }

        return (unsigned char)*data;
}

void static inline twi_select(unsigned char addr, unsigned char dir) {
        unsigned int timeout = 0;
        if(verbose) fprintf(stderr,"Attempting to send start signal\n");
        *control = START|TWSIEN;        // Send start signal
        usleep(1);
        while((*control & IFLG) == 0)
                if(timeout++ > 10000) {
                        if(verbose) fprintf(stderr, "Timedout\n");
                        *control = STOP|TWSIEN; // Send stop signal
                        if(verbose) fprintf(stderr,"Sent Stop signal\n");
                }
        if((*status != MT_START) && (*status != MT_REP_START)) {
                if(verbose) fprintf(stderr,"Couldn't send start signal(0x%x)\n",
                  *status);
                *control = STOP|TWSIEN; // Send stop signal
                if(verbose) fprintf(stderr,"Sent Stop signal\n");
        }

        if(verbose) fprintf(stderr,"Sent start signal succesfully\n"
          "Attempting to select slave\n");
        *data = addr | dir;     // Send SLA+W/R
        usleep(1);

        *control = (*control & ~IFLG) | ACK;
        usleep(1);

        while((*control & IFLG) == 0) ;

        if((*status != MT_SLA_ACK) && (*status != MR_SLA_ACK)) {
                if(verbose) fprintf(stderr,"Slave didn't ACK select signal(0x%x)\n", *status);
                *control = STOP|TWSIEN; // Send stop signal
                if(verbose) fprintf(stderr,"Sent Stop signal\n");

        }
        if(verbose) fprintf(stderr,"Succesfully selected slave\n");
}

int main(int argc, char **argv) {
	int c, devmem;
	unsigned int otp_addr, otp_data, secs = 0;
	unsigned int val_addr=0, val_data=0, *twi_regs, *regs;
	unsigned int start_adc=0, raw=0;
	unsigned int display_odom=0, did_something=0, display_bday=0;
	unsigned int display_otp=0, display_mem=0, display_mac=0;
	unsigned int len, odom, bday;
	unsigned char str[80];

	devmem = open("/dev/mem", O_RDWR|O_SYNC);
	assert(devmem != -1);
	twi_regs = (unsigned int *)mmap(0, getpagesize(), PROT_READ|PROT_WRITE,
	  MAP_SHARED, devmem, 0xF1011000);

	regs = (unsigned int *)mmap(0, getpagesize(), PROT_READ|PROT_WRITE,
	  MAP_SHARED, devmem,  0xe8000000);

	led = &regs[GLED];
        control = &twi_regs[CONTROL];
        data = &twi_regs[DATA];
        status = &twi_regs[STATUS];

       for (;;) {
		twi_select(AVR_ADDR, WRITE);
		twi_write(LED);
/*		*control = STOP|TWSIEN; // Send stop signal
		if(verbose) fprintf(stderr,"Sent Stop signal\n");
			did_something=1;
*/
	        sleep(1);
		
		twi_select(AVR_ADDR, WRITE);
		twi_write(0);
/*
		*control = STOP|TWSIEN; // Send stop signal
		if(verbose) fprintf(stderr,"Sent Stop signal\n");
			did_something=1;
*/
	}

}
