
// ---------------------------------------------------------------------
//
//	File: spuutils.c
//      SWARM Orb SPU code http://www.orbswarm.com
//	general SPU utilities
//
//
//
//
//	Written by Matt C, Dillo, Niladri, Jesse Z, refactored by Jon F
// -----------------------------------------------------------------------
#include <stdio.h>
#include <unistd.h>   /* for )_RDONLY, etc. */
#include <sys/mman.h> /* for mmap, etc */
#include <fcntl.h>
#include <string.h> /* for strncpy, etc. */

#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>
#include <sys/types.h>
#include <sys/mman.h>
#include <assert.h>

#include "spuutils.h"

static int /*c,*/ devmem;
//static unsigned int otp_addr, otp_data, secs = 0;
static unsigned int /*val_addr=0, val_data=0,*/ *twi_regs, *regs;
//static unsigned int start_adc=0, raw=0;
//static unsigned int display_odom=0, did_something=0, display_bday=0;
//static unsigned int display_otp=0, display_mem=0, display_mac=0;
//static unsigned int len, odom, bday;
//static unsigned char str[80];
static volatile unsigned int *GREENLEDPORT/*, *PEDDR*/;
static volatile unsigned char *start;


//int setSpuLed(const unsigned int ledState){
//#ifdef LOCAL
//#warning "compiling spuutils.c for LOCAL use (not SPU)"
//	switch(ledState) {
//	case SPU_LED_RED_ON:
//		printf("SPU_LED_RED_ON\n");
//		break;
//	case SPU_LED_GREEN_ON:
//		printf("SPU_LED_GREEN_ON\n");
//		break;
//	case SPU_LED_BOTH_ON:
//		printf("SPU_LED_BOTH_ON\n");
//		break;
//	case SPU_LED_BOTH_OFF:
//		printf("SPU_LED_BOTH_OFF\n");
//		break;
//	case SPU_LED_RED_OFF:
//		printf("SPU_LED_RED_OFF\n");
//		break;
//	case SPU_LED_GREEN_OFF:
//		printf("SPU_LED_GREEN_OFF\n");
//		break;
//	default:
//		fprintf(stderr,"\nUNKNOWN LED STATE");
//	}
//	return(1);
//
//#else
//	volatile unsigned int *PEDR, *PEDDR;
//	int fd = open("/dev/mem", O_RDWR|O_SYNC);
//	if(fd <= 0) {
//		printf("/dev/mem open failed in setSPULed()\n");
//		return(0);
//	}
//
//	start =(unsigned char*) mmap(0, getpagesize(), PROT_READ|PROT_WRITE, MAP_SHARED, fd, 0x80840000);
//
//	if(start <= 0) {
//		printf("/dev/mem map failed in setSPULed()\n");
//		return(0);
//	}
//	PEDR = (unsigned int *)(start + 0x20);     // port e data
//	PEDDR = (unsigned int *)(start + 0x24);    // port e direction register
//
//	*PEDDR = 0xff;                             // all output (just 2 bits)
//
//	switch(ledState) {
//	case SPU_LED_RED_ON:
//		*PEDR |= 0x02;
//		break;
//	case SPU_LED_GREEN_ON:
//		*PEDR |= 0x01;
//		break;
//	case SPU_LED_BOTH_ON:
//		*PEDR |= 0x03;
//		break;
//	case SPU_LED_BOTH_OFF:
//		*PEDR &= 0xFC;
//		break;
//	case SPU_LED_RED_OFF:
//		*PEDR &= 0xFD;
//		break;
//	case SPU_LED_GREEN_OFF:
//		*PEDR &= 0xFE;
//		break;
//	default:
//		fprintf(stderr,"\nUNKNOWN LED STATE");
//	}
//	munmap(start,getpagesize());
//	close(fd);
//	return 0;
//#endif
//}

void cleanupSpuutils(void)
{
	munmap(start, getpagesize());
	close(devmem);
}

/* retrieve IP address using ioctl interface */
int getIP(const char *Interface, char *ip)
{
#ifdef LOCAL
  sprintf(ip,"%s","192.168.1.61");
  return(0);
#else
  int                  s;
  struct ifreq         ifr;
  int                  err;
  struct sockaddr_in * addr_in;

  s = socket( AF_INET, SOCK_DGRAM, IPPROTO_UDP );
  if ( -1 == s )
  {
    perror("Getting interface socket");
    close(s);
    return(-1);
  }

  if(Interface == NULL) Interface="eth0";

  strncpy( ifr.ifr_name, Interface, IFNAMSIZ );
  err = ioctl( s, SIOCGIFADDR, &ifr );
  if ( -1 == err )
  {
    perror("Getting IP address");
    close(s);
    return(-1);
  }
  addr_in = (struct sockaddr_in *)&ifr.ifr_addr;

  if(ip != NULL)
  {
    sprintf(ip,"%s",inet_ntoa(addr_in->sin_addr));
  }

  close(s);
  return(0);
#endif
}

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

int initSpuutils(void){
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
