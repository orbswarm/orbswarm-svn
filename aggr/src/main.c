#include <avr/io.h>		// include I/O definitions (port names, pin names, etc)
#include <avr/interrupt.h>	// include interrupt support
#include <stdio.h>
#include <string.h>
#include <stdlib.h>

#include "include/timer0.h"
#include "include/swarm_common_defines.h"
#include "include/uart.h"
#include "include/spu.h"
#include "include/gps.h"
#include "include/xbee.h"
#include "include/swarm_queues.h"

#define GPS_TIMER_VAL_MILLIS 200
//#define GPS_TIMER_VAL_MILLIS 1000

//#define DEBUG_MODE

void debug(const char *s) {
#ifdef DEBUG_MODE
	debugUART (s);
#endif
}

void info(const char *s) {
	debugUART(s);
}

void blinkLedPortB6(void) {
	PORTB = PORTB ^ (1 << PB6);
}

void blinkLedPortB7(void) {
	PORTB = PORTB ^ (1 << PB7);
}

unsigned char calculateCheckSum(char * msg) {
	unsigned char checksum=0;
	int i;
	for (i = 0; i < strlen(msg); i++) {
		if (0 == i)
			checksum = msg[i];
		else
			checksum ^= msg[i];
	}
	return checksum;
}
void setGpsModeInUBlox(void)
{
	loopTimer0(3000);
	//B5 62 06 08 06 00 FA 00 01 00 01 00 10 96
	info("\r\nsending UBX rate msg to set to 4Hz");
	unsigned char msg[] ={0xB5, 0x62, 0x06, 0x08,
			0x06, 0x00, 0xFA, 0x00,
			0x01,0x00, 0x01, 0x00,
			0x10,0x96};
	sendGPSABinaryMsg(msg, 14);
}

char dummyPop(int isInterruptCtx) {
	return 0;
}

void dummyHandler(char c, int isError) {
}

int main(void) {
	char gps_msg_buffer[MAX_GPS_PACKET_LENGTH];

	uart_init(handleXbeeSerial /*xbee handler */, handleSpuSerial, /*spu handler */
	handleGpsSerial, /*gps handler */
	popXbeeDataQ /*xbee pop */, popSpuDataQ /* spu pop */);
	//loopTimer0(2000);
	char strDebugMsg[1024];
	char sanitizedGpsString[MAX_GPS_PACKET_LENGTH];
	sprintf(strDebugMsg, "\r\n PORTB=%x", PORTB);
	info(strDebugMsg);

	initXbeeModule(pushSwarmMsgBus, blinkLedPortB7, 0);
	initGpsModule(blinkLedPortB6, 0);
	initSpuModule(pushSwarmMsgBus, 0, /*debug*/ 0);

	info("\r\ninit ");
	sei();
	//setGpsMode();
	setGpsModeInUBlox();
	DDRB = 0xff;
	setTimerInAsync(GPS_TIMER_VAL_MILLIS);
	while (1) {
		//debug("\r\n while start");
		struct SWARM_MSG msg = popSwarmMsgBus(0);
		if (msg.swarm_msg_type == eLinkXbeeMsg) {
			//queue up for sending to spu when asked for
			//But if send is in progress wait for it to be over
			while (isSpuSendInProgress())
				;
			debug("\r\npushing into spu data q");
			pushSpuDataQ(msg.swarm_msg_payload, 0);
			startAsyncSpuTransmit();
		} else if (msg.swarm_msg_type == eLinkSpuMsg) {
			sprintf(strDebugMsg, "\r\ngot message from spu=%s",
					msg.swarm_msg_payload);
			debug(strDebugMsg);
			if ('{' == msg.swarm_msg_payload[0] && '}'
					== msg.swarm_msg_payload[strlen(msg.swarm_msg_payload) - 1]) {
				while (isSpuSendInProgress())
					;
				sprintf(strDebugMsg, "\r\nstreaming data through xbee msg=%s",
						msg.swarm_msg_payload);
				debug(strDebugMsg);
				pushXbeeDataQ(msg.swarm_msg_payload, 0);
				startAsyncXBeeTransmit();
			}
		} else {
			//debug("\r\n invalid or null msg type");
		}
		if (isTimedOut()) {
			while (isSpuSendInProgress())
				;
			debug("\r\n Got request for GPS");
			pushSpuDataQ("\r\n{(", 0);
			getGpsGpggaMsg(gps_msg_buffer, 0);

			//Now remove trailing '*' because that confuses the lemon parser
			//stripChecksum(gps_msg_buffer + 1, sanitizedGpsString);
			sprintf(strDebugMsg, "\r\n [len=%d un santiized gga=%s]", strlen(gps_msg_buffer),
					gps_msg_buffer);
			debug(strDebugMsg);
			//sanitizedGpsString[0]=0;
			if(strlen(gps_msg_buffer)>6){
				strncpy(sanitizedGpsString, gps_msg_buffer +1, strlen(gps_msg_buffer)-6);
				sanitizedGpsString[strlen(gps_msg_buffer)-6]=0;
				sprintf(strDebugMsg, "\r\n [santiized gga=%s]", sanitizedGpsString);
				debug(strDebugMsg);
				pushSpuDataQ(sanitizedGpsString, 0);
			}

			pushSpuDataQ(";\r\n", 0);
			startAsyncSpuTransmit();

			getGpsGpvtgMsg(gps_msg_buffer, 0);
			//stripChecksum(gps_msg_buffer + 1, sanitizedGpsString);
			sprintf(strDebugMsg, "\r\n [len=%d un santiized vtg=%s]",strlen(gps_msg_buffer),
					gps_msg_buffer);
			debug(strDebugMsg);
			//sanitizedGpsString[0]=0;
			if(strlen(gps_msg_buffer)>6){
				strncpy(sanitizedGpsString, gps_msg_buffer +1, strlen(gps_msg_buffer)-6);
				sanitizedGpsString[strlen(gps_msg_buffer)-6]=0;
				sprintf(strDebugMsg, "\r\n [santiized vtg=%s]", sanitizedGpsString);
				debug(strDebugMsg);
				pushSpuDataQ(sanitizedGpsString, 0);
			}

			pushSpuDataQ(")}", 0);
			startAsyncSpuTransmit();
			setTimerInAsync(GPS_TIMER_VAL_MILLIS);
		}
		/*	else
		 debug("GPS has not timed out");*/
	}//end while
	//
	return 0;
}

