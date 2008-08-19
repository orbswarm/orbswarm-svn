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

static int /*c,*/ devmem;
//static unsigned int otp_addr, otp_data, secs = 0;
static unsigned int /*val_addr=0, val_data=0,*/ *twi_regs, *regs;
//static unsigned int start_adc=0, raw=0;
//static unsigned int display_odom=0, did_something=0, display_bday=0;
//static unsigned int display_otp=0, display_mem=0, display_mac=0;
//static unsigned int len, odom, bday;
//static unsigned char str[80];
static volatile unsigned int *GREENLEDPORT/*, *PEDDR*/;
static unsigned char *start;

int init(void){
	//init for RED led
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

	//init for GREEN led
	start =(unsigned char*) mmap(0, getpagesize(), PROT_READ|PROT_WRITE, MAP_SHARED,
			devmem, 0xE8000000);

	if(start <= 0) {
		printf("/dev/mem map failed in setSPULed()\n");
		return(0);
	}
	GREENLEDPORT = (unsigned int *)(start + 0x08);
	return 1;
}

void blinkGreen(void){
	*GREENLEDPORT  ^= (1ul<<GREEN_LED_BIT);
}

void blinkRed(void) {
	fprintf(stderr, "1");
	twi_select(AVR_ADDR, READ);
	unsigned char byteVal = twi_read();
	fprintf(stderr, "byte val=%2X", byteVal);
	byteVal ^= 1 << 2;

	twi_select(AVR_ADDR, WRITE);
	fprintf(stderr, "2");
	twi_write(byteVal);
	fprintf(stderr, "3");
	twi_write(0x00);
	fprintf(stderr, "4");
}

//int test(int argc, char *argv[]) {
//	init();
//	while(1){
//		fprintf(stderr, "start loop");
//		blinkGreen();
//		blinkRed();
//		sleep(1);
//	}
//	return 0;
//}
