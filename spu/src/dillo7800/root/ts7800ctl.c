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

int parsechans(const char *str, unsigned int len) {

	int chans=0, last_ch=-1, dash=0, i, j;
	//Determine which channels to sample
	for(i=0;i<len;i++) {
		if((str[i] >= '0' && str[i] <= '3') ||
		  (str[i] >= '6' && str[i] <= '7')) {
			if(dash) {
				dash = 0;
				if(last_ch < 0) {
					printf("Invalid format, Sample ADC channels CHANS, e.g. \"1-3,5\", \"2,3,5\"\n");
					return -1;
				}
		
				for(j=last_ch; j<=(str[i]-'0'); j++)
					chans |= (1<<j);		

			} else {
				last_ch = str[i] - '0';
				chans |= 1<<(str[i] - '0');
			}

		} else if(str[i] == '-') { dash=1;
		} else if((str[i] == ',') || (str[i] == ' ')){ ; 
		} else {
			printf("Invalid format, Sample ADC channels CHANS, e.g. \"1-3,5\", \"2,3,5\"\n");
			return -1;
		}
	}

	//channel 6 => bit 4
	if(chans & (1<<6)) {
		chans |= 1<<4;
		chans &= ~(1<<6);
	}

	//channel 7 => bit 5
	if(chans & (1<<7)) {
		chans |= 1<<5;
		chans &= ~(1<<7);
	}

	return chans;
}

void usage(char **argv) {
        fprintf(stderr, "Usage: %s [OPTION] ...\n"
	  "Modify state of TS-7800 hardware.\n"
	  "\n"
	  "General options:\n"
	  "  -s    seconds         Number of seconds to sleep for\n"
	  "  -f                    Feed the WDT for 8s\n"
	  "  -d                    Disable the WDT\n"
	  "  -r    CHANS           Sample ADC channels CHANS, e.g. \"1-3,5\", \"2,3,5\"" 
                                  "output raw data to standard out\n"
          "  -S    CHANS           Sample ADC channels CHANS, e.g. \"1-3,5\", \"2,3,5\"" 
                                  "output string parseable data to standard out\n"
	  "  -A    ADDR            Write DATA to ADDR in one time programmable memory\n"
	  "  -D    DATA            Write DATA to ADDR in one time programmable memory\n"
	  "  -n                    Red LED on\n"
	  "  -F                    Red LED off\n"
          "  -o                    Display one time programmable data\n"
          "  -m                    Display contents of non-volatile memory\n"
          "  -M                    Display MAC address\n"
          "  -O                    Display odometer(hrs board has been running for)\n"
          "  -B                    Display birthdate\n"     
//	  "  -V                    Verbose output\n"			
	  "  -h                    This help screen\n", argv[0]);
}

void static inline twi_write(unsigned char dat) {
	if(verbose) fprintf(stderr,"Writing data (0x%x)\n", dat);
	*data = dat;
	usleep(100);

	*control = (*control & ~IFLG) | ACK;
	usleep(100);
	while((*control & IFLG) == 0x0);        // Wait for an I2C event


	if((*status != ST_DATA_ACK) && (*status != MT_DATA_ACK )) {
		if(verbose) fprintf(stderr,"Slave didn't ACK data\n");
		exit(1);
	}

	if(verbose) fprintf(stderr,"Slave ACKed data\n");
}
	
unsigned char static inline twi_read() {


	if(verbose) fprintf(stderr, "Waiting for data from master\n");
	*control = (*control & ~IFLG) | ACK;
	while((*control & IFLG) == 0x0);	// Wait for an I2C event 
	if((*status != SR_DATA_ACK) && (*status != MR_DATA_ACK)) {
		if(verbose) fprintf(stderr, "Error reading data from master(0x%x)\n", *status);
		exit(1);
	
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
			exit(-1);
		}
	if((*status != MT_START) && (*status != MT_REP_START)) {
		if(verbose) fprintf(stderr,"Couldn't send start signal(0x%x)\n",
		  *status);
		*control = STOP|TWSIEN; // Send stop signal
		if(verbose) fprintf(stderr,"Sent Stop signal\n");
		exit(-1);
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
		exit(-1);
		
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

	if(argc == 1) {
		usage(argv);
		return 1;
	}

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

//EWD: If time remains modify me to getopt_long()
	while ((c = getopt(argc, argv, "s:fdr:S:A:D:nFVoOmMB")) != -1) {

		switch(c) {
			case 's':
				secs = strtoul(optarg, NULL, 0);
				if((secs > 0) && (secs <= (65535 * 8))){
					if((secs % 8) > 0)  secs=(secs/8)+1;
					else secs=(secs/8);
				} else if(secs > (65535 * 8))
					printf("Invalid sleep time,"
					  "maximum sleep time is 524288\n");
				did_something=1;
				break;
		
			case 'f':
				twi_select(AVR_ADDR, WRITE);
				twi_write(WDT_8s);
				*control = STOP|TWSIEN; // Send stop signal
				if(verbose) fprintf(stderr,"Sent Stop signal\n");
				did_something=1;
				break;

			case 'd':
				twi_select(AVR_ADDR, WRITE);
				twi_write(WDT_OFF);
				*control = STOP|TWSIEN; // Send stop signal
				if(verbose) fprintf(stderr,"Sent Stop signal\n");
				did_something=1;
				break;

			case 'r':
				start_adc=1;
				raw=1;
				for(len=0;len<80;len++) 
					if(optarg[len]=='\0') break;

				strncpy(str, optarg, len);
				break;

			case 'S':
				start_adc=1;
				for(len=0;len<80;len++)
					if(optarg[len]=='\0') break;
				strncpy(str, optarg, len);
				break;
			
			case 'A':
				otp_addr = strtoul(optarg, NULL, 0);
				if(otp_addr < 256) val_addr = 1;
				else fprintf(stderr, "Invalid address," 
				  " valid address are 0-63\n");
				break;

			case 'D':
				otp_data = strtoul(optarg, NULL, 0);
				if(otp_data < 256) val_data = 1;
				else fprintf(stderr, "Invalid data,"
				  " valid data is 0-255");
				break;
			
			case 'n':
				twi_select(AVR_ADDR, WRITE);
				twi_write(LED);
				*control = STOP|TWSIEN; // Send stop signal
				if(verbose) fprintf(stderr,"Sent Stop signal\n");
				did_something=1;
				break;

			case 'F':
				twi_select(AVR_ADDR, WRITE);
				twi_write(0);
				*control = STOP|TWSIEN; // Send stop signal
				if(verbose) fprintf(stderr,"Sent Stop signal\n");
				did_something=1;
				break;

			case 'V':
				verbose = 1;
				break;

			case 'M':
				display_mac = 1;
				did_something=1;
				break;

			case 'O':
				display_odom = 1;
				did_something=1;
				break;

			case 'B':
				display_bday = 1;
				did_something=1;
				break;

			case 'o':
				display_otp = 1;
				did_something=1;
				addr=6;
				break;

			case 'm':
				display_mem = 1;
				did_something=1;
				addr=70;
				break;

			case 'h':	
			default:
				usage(argv);
				return 1;
		}

	}

	if(start_adc) {

		volatile unsigned int *fpga, *sram;
		unsigned int i;
		int chans;
		did_something = 1;

		fpga = (unsigned int *)mmap(0, 0x1004, PROT_READ|PROT_WRITE,
		  MAP_SHARED, devmem, 0xE8100000);
        	sram = &fpga[SRAM];

	//	for(i=0;i<1024;i++) sram[i] = 0xFFFFFFFF; 
		chans = parsechans(str, len);
		if(chans < 0) return -1;

		twi_select(AVR_ADDR, WRITE);
		twi_write(chans | 0x40);
		*control = STOP|TWSIEN; // Send stop signal

		if(verbose) fprintf(stderr,"Sent Stop signal\n");

//EWD: Implement this in a more elegant way. That is write out all FF's then print values as they are available.
//2KSPS => a new sample is available every 500uS
usleep(1500000);

		for(i=0;i<1024;i++) {
			if(raw) printf("0x%x\n", sram[i]);
		}
	}

	if(secs > 0) {
		twi_select(AVR_ADDR, WRITE);
		twi_write(SLEEP);
		twi_write((secs >> 8) & 0xFF);	//MSB
		twi_write(secs & 0xFF);		//LSB
		*control = STOP|TWSIEN; 	//Send stop signal
		if(verbose) fprintf(stderr,"Sent Stop signal\n");
	}
	
	if(val_data && val_addr) {
//EWD: Do some error checking to ensure this is a valid address, we don't need to
//check if the byte has already been programmed. As we can silently fail...

		did_something=1;
		printf("OTP: writing 0x%x to 0x%x\n", otp_data, otp_addr);	
		twi_select(AVR_ADDR, WRITE);
		twi_write(OTP_W);
		twi_write(otp_addr);
		twi_write(otp_data);
		usleep(1);
		*control = STOP|TWSIEN; 	//Send stop signal
		if(verbose) fprintf(stderr,"Sent Stop signal\n");
	}

	if(display_odom) {

		twi_select(AVR_ADDR, WRITE);

		twi_write(OTP_R);
		twi_write(134);
		twi_select(AVR_ADDR, READ);

		odom = (twi_read() << 24);
		odom |= (twi_read() << 16);
		odom |= (twi_read() << 8);
		odom |= twi_read();	
		usleep(1);
		*control = STOP|TWSIEN; 	//Send stop signal

		if(verbose) fprintf(stderr,"SENT Stop signal\n");
		odom = 0xFFFFFFFF - odom;
		printf("TS-7800 has been running for %d hours\n", odom);	
	}

	if(display_bday) {

		twi_select(AVR_ADDR, WRITE);

		twi_write(OTP_R);
		twi_write(145);
		twi_select(AVR_ADDR, READ);

		bday = (twi_read() << 24);
		bday |= (twi_read() << 16);
		bday |= (twi_read() << 8);
		bday |= twi_read();	
		usleep(1);
		*control = STOP|TWSIEN; 	//Send stop signal

		if(verbose) fprintf(stderr,"SENT Stop signal\n");
		printf("TS-7800 was born on %02d/%02d/%04d\n", (bday>>24), (bday>>16)&0xFF, bday&0xFFFF);	
	}

	if(display_mac) {

		unsigned char mac[6];
		twi_select(AVR_ADDR, WRITE);
		twi_write(OTP_R);
		twi_write(0);
		twi_select(AVR_ADDR, READ);
		mac[5] = twi_read();
		mac[4] = twi_read();
		mac[3] = twi_read();
		mac[2] = twi_read();	
		usleep(1);
		*control = STOP|TWSIEN; 	//Send stop signal
		if(verbose) fprintf(stderr,"SENT Stop signal\n");

		twi_select(AVR_ADDR, WRITE);
		twi_write(OTP_R);
		twi_write(4);
		twi_select(AVR_ADDR, READ);
		mac[1] = twi_read();
		mac[0] = twi_read(); 
		twi_read(); twi_read();	//We don't care about the other two bytes
		usleep(1);
		*control = STOP|TWSIEN; 	//Send stop signal
		if(verbose) fprintf(stderr,"SENT Stop signal\n");

		printf("HWaddr %02x:%02x:%02x:%02x:%02x:%02x\n", mac[5],mac[4],mac[3],mac[2],mac[1],mac[0]);
	}

	if(display_mem || display_otp) {

		unsigned char dat[4];
		unsigned char start=addr;

		twi_select(AVR_ADDR, WRITE);

		for(;addr<(start+64);addr+=4) {

			twi_select(AVR_ADDR, WRITE);

			twi_write(OTP_R);
			twi_write(addr);
			twi_select(AVR_ADDR, READ);

			dat[0] = twi_read();
			dat[1] = twi_read();
			dat[2] = twi_read();
			dat[3] = twi_read();	
			usleep(1);
			*control = STOP|TWSIEN; 	//Send stop signal

			if(verbose) fprintf(stderr,"Sent Stop signal\n");
			fwrite(&dat[0], sizeof(unsigned int), 1, stdout);
			fflush(stdout);
		}
	}


	if(!did_something) usage(argv);

	return 0;
}
