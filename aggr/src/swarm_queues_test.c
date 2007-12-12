
#include <stdio.h>
#include <string.h>
#include <avr/io.h>
#include <avr/interrupt.h>

#include "include/swarm_common_defines.h"
#include "include/swarm_queues.h"
#include "include/uart.h"

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
	debug("testSwarmMessageBus:START");
	struct SWARM_MSG msg;
	long i;
	for(i=0; i < MAX_SWARM_MSG_BUS_SIZE + 1000; i++)
	{
		msg.swarm_msg_type=eLinkLoopback; 
		sprintf(msg.swarm_msg_payload, "message num=%ld", i);
		pushSwarmMsgBus(msg, 0);
	}
	 
	msg = popSwarmMsgBus(0);
	while(eLinkNullMsg != msg.swarm_msg_type)
	{
		char strDebugMsg[1024];
		sprintf(strDebugMsg, "\r\ngot MSG"
			"\r\n type=%d"
			"\r\n payload=%s", msg.swarm_msg_type, msg.swarm_msg_payload);
		debug(strDebugMsg);
		msg = popSwarmMsgBus(0);
	}	
}
volatile uint16_t m_unitsOf1ms=0;
volatile uint16_t timecount=0;

ISR(SIG_OVERFLOW0)
{
  TCNT0=26;
  if(++timecount == m_unitsOf1ms)
    {
    		struct SWARM_MSG msg;
			long i;
			for(i=0; i < MAX_SWARM_MSG_BUS_SIZE; i++)
			{
				msg.swarm_msg_type=eLinkLoopback; 
				sprintf(msg.swarm_msg_payload, "message num=%ld", i);
				pushSwarmMsgBus(msg, 1);
			}
    	
		timecount=0;
    }
}


void initTimer0(uint16_t units1ms)
{
  timecount=0;
  m_unitsOf1ms=units1ms;
  TCNT0=26;
  TCCR0B = TCCR0B | ((1<<CS01) | (1<<CS00));
  TIMSK0= TIMSK0 | (1<<TOIE0);
}

int main(void)
{
	DDRB = 0xff;
	initTimer0(1000);
	uart_init(dummyHandler, dummyHandler, dummyHandler, dummyPop, dummyPop);
	//initSwarmMsgBus(debug);
	sei();
	debug("----START");
	while(1)
	{
		testSwarmMessageBus();
		blinkLedPortB7();
	}
}
