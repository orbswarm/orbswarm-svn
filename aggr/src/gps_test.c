#include <stdio.h>
#include <string.h>
#include <avr/io.h>
#include <avr/interrupt.h>

#include "include/swarm_common_defines.h"
#include "include/gps.h"
#include "include/uart.h"

static volatile uint16_t m_unitsOf1ms=0;
static volatile uint16_t timecount=0;

const char *const s_strGpggaMsg="$GPGGA,093702.600,3743.983356,N,"
			"12222.601505,W,1,9,1.04,1.033,M,-25.322,M,,*64";
const char *const s_strGpvtgMsg="$GPVTG,0.00,T,,M,0.000,N,0.001,K,A*3";

void blinkLedPortB6(void)
{
  PORTB = PORTB ^ (1<<PB6);
}

void blinkLedPortB7(void)
{
  PORTB = PORTB ^ (1<<PB7);
}

static char dummyPop(void){return 0;}

void dummyHandler(char c, int isError){}

void initTimer0(uint16_t units1ms)
{
  timecount=0;
  m_unitsOf1ms=units1ms;
  TCNT0=26;
  TCCR0B = TCCR0B | ((1<<CS01) | (1<<CS00));
  TIMSK0= TIMSK0 | (1<<TOIE0);
}

ISR(SIG_OVERFLOW0)
{
  TCNT0=26;
  if(++timecount == m_unitsOf1ms)
    {
		char const* msg=s_strGpggaMsg;
		while(*msg)
			handleGpsSerial(*msg++, 0/*No error*/, 1 /* and interrupt handler ctx*/);
		msg=s_strGpvtgMsg;	
		while(*msg)
			handleGpsSerial(*msg++, 0/*No error*/, 1 /* and interrupt handler ctx*/);
		timecount=0;
    }
}

void mainLoop(void)
{
	char strDebugMsg[1024];
	while(1)
	{
		char strMsg[MAX_GPS_PACKET_LENGTH+1];
		getGpsGpggaMsg(strMsg,0 /*false*/);		
		sprintf(strDebugMsg, "\r\ngpgga=%s", strMsg);
		debug(strDebugMsg);
		
		getGpsGpvtgMsg(strMsg, 0 /*interrupt ctx=false*/);
		sprintf(strDebugMsg, "\r\ngpvtg=%s", strMsg);
		debug(strDebugMsg);		
	}
}

int main(void)
{
	DDRB = 0xff;
	initTimer0(25);
	initGpsModule(blinkLedPortB7, debug);
	uart_init(dummyHandler, dummyHandler, dummyHandler, dummyPop, dummyPop);
	//initSwarmQueues(debug);
	sei();
	debug("----START");
	while(1)
	{
		mainLoop();
	}
	return 0;
}
