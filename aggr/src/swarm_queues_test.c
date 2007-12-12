
#include <stdio.h>
#include <string.h>
#include <avr/io.h>
#include <avr/interrupt.h>

#include "include/swarm_common_defines.h"
#include "include/swarm_queues.h"
#include "include/uart.h"
#include "include/timer0.h"

void blinkLedPortB6(void)
{
  PORTB = PORTB ^ (1<<PB6);
}

void blinkLedPortB7(void)
{
  PORTB = PORTB ^ (1<<PB7);
}


static char dummyPop(void)
{
	return 'z';
}

void dummyHandler(char c, int isError){}

void testSwarmMessageBus(void)
{
	struct SWARM_MSG msg;
	long i;
	for(i=0; i < MAX_SWARM_MSG_BUS_SIZE + 1000; i++)
	{
		msg.swarm_msg_type=eLinkLoopback; 
		sprintf(msg.swarm_msg_payload, "message num=%ld", i);
		pushSwarmMsgBus(msg);
	}
	 
	msg = popSwarmMsgBus();
	while(eLinkNullMsg != msg.swarm_msg_type)
	{
		char strDebugMsg[1024];
		sprintf(strDebugMsg, "\r\ngot MSG"
			"\r\n type=%d"
			"\r\n payload=%s", msg.swarm_msg_type, msg.swarm_msg_payload);
		debug(strDebugMsg);
		msg = popSwarmMsgBus();
	}	
}

int main(void)
{
	DDRB = 0xff;
	uart_init(dummyHandler, dummyHandler, dummyHandler, dummyPop, dummyPop);
	sei();	
	while(1)
	{
		loopTimer0(1000);
		testSwarmMessageBus();
		blinkLedPortB7();
	}
}
