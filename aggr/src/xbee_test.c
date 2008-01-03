#include <stdio.h>
#include <string.h>
#include <avr/io.h>
#include <avr/interrupt.h>

#include "include/swarm_common_defines.h"
#include "include/xbee.h"
#include "include/uart.h"
#include "include/swarm_queues.h"

static volatile uint16_t m_unitsOf1ms=0;
static volatile uint16_t timecount=0;

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
		char* msg="{hello world}";
		while(*msg)
		{
			handleXbeeSerial(*msg++, 0/*No error*/, 1/*interrupt handler ctx*/);	
		}
		timecount=0;
    }
}

void mainLoop(void)
{
	char strDebugMsg[1024];
	while(1)
	{
		struct SWARM_MSG msg = popSwarmMsgBus(0/*false*/);
		if(eLinkNullMsg != msg.swarm_msg_type)
		{
			sprintf(strDebugMsg, "\r\n got message, type=%d"
			" ,payload=%s", msg.swarm_msg_type, msg.swarm_msg_payload);
			debugUART(strDebugMsg);
		}
		else
		{
			debugUART("\r\n got null message");
		}		
	}
}

int main(void)
{
	DDRB = 0xff;
	initTimer0(25);
	initXbeeModule(pushSwarmMsgBus, blinkLedPortB7, debugUART);
	uart_init(dummyHandler, dummyHandler, dummyHandler, dummyPop, dummyPop);
	//initSwarmQueues(debug);
	sei();
	debugUART("----START");
	while(1)
	{
		mainLoop();
	}
	return 0;
}
