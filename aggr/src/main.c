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

void setGpsMode(void) {
	char strDebugMsg[1024];
	char ack[MAX_GPS_PACKET_LENGTH];
	char *msg;
	char cmd[64];
	info("\r\nReading init params if any");
	loopTimer0(2000);
	getPmtkMsg(ack, 0 /*false */);
	sprintf(strDebugMsg, "\r\nack=%s", ack);
	info(strDebugMsg);

	info("\r\nsending 313");
	/*
	 * 313 - Enable to search a SBAS satellite or not.
	 * ‘0’ = Disable
	 * ‘1’ = Enable
	 */
	msg = "PMTK313,1";
	sprintf(cmd, "$%s*%x\r\n", msg, calculateCheckSum(msg));
	sendGPSAMsg(cmd);
	loopTimer0(2000);
	getPmtkMsg(ack, 0 /*false */);
	sprintf(strDebugMsg, "\r\nack=%s", ack);
	info(strDebugMsg);

	info("\r\nsending 301");
	/*
	 * 301 - DGPS correction data source mode.
	 * '0’: No DGPS source
	 * ‘1’: RTCM
	 * ‘2’: WAAS
	 *
	 */
	msg = "PMTK301,2";
	sprintf(cmd, "$%s*%x\r\n", msg, calculateCheckSum(msg));
	sendGPSAMsg(cmd);
	loopTimer0(2000);
	getPmtkMsg(ack, 0 /*false */);
	sprintf(strDebugMsg, "\r\nack=%s", ack);
	info(strDebugMsg);

	info("\r\nsending dgps query");
	/*
	 * Query DGPS mode
	 */
	msg = "PMTK401";
	sprintf(cmd, "$%s*%x\r\n", msg, calculateCheckSum(msg));
	sendGPSAMsg(cmd);
	loopTimer0(2000);
	getPmtkMsg(ack, 0 /*false */);
	sprintf(strDebugMsg, "\r\nack=%s", ack);
	info(strDebugMsg);

	info("\r\nsending sbas query");
	/*
	 * Query SBAS mode
	 */
	msg = "PMTK413";
	sprintf(cmd, "$%s*%x\r\n", msg, calculateCheckSum(msg));
	sendGPSAMsg(cmd);
	loopTimer0(2000);
	getPmtkMsg(ack, 0 /*false */);
	sprintf(strDebugMsg, "\r\nack=%s", ack);
	info(strDebugMsg);

}

char dummyPop(int isInterruptCtx) {
	return 0;
}

void dummyHandler(char c, int isError) {
}

static void stripChecksum(const char* gpsStr, char* destStr) {
	size_t idx;
	for (idx = 0; idx < MAX_GPS_PACKET_LENGTH; idx++)
		destStr[idx] = 0;
	idx = strlen(gpsStr);
	for (; idx >= 0; idx--) {
		if (',' == gpsStr[idx]) {
			strncpy(destStr, gpsStr, idx);
			return;
		}
	}
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

	initXbeeModule(pushSwarmMsgBus, blinkLedPortB6, debug);
	initGpsModule(blinkLedPortB7, debug);
	initSpuModule(pushSwarmMsgBus, 0, debug);

	info("\r\ninit ");
	sei();
	setGpsMode();
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
			pushSpuDataQ("{(", 0);
			getGpsGpggaMsg(gps_msg_buffer, 0);
			stripChecksum(gps_msg_buffer + 1, sanitizedGpsString);
			pushSpuDataQ(sanitizedGpsString, 0);
			//pushSpuDataQ(gps_msg_buffer+1, 0);

			pushSpuDataQ(";", 0);
			startAsyncSpuTransmit();

			getGpsGpvtgMsg(gps_msg_buffer, 0);
			stripChecksum(gps_msg_buffer + 1, sanitizedGpsString);
			pushSpuDataQ(sanitizedGpsString, 0);
			//pushSpuDataQ(gps_msg_buffer+1, 0);

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

