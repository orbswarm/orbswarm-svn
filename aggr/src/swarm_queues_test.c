
#include <stdio.h>
#include <string.h>
#include <avr/io.h>
#include <avr/interrupt.h>

#include "include/swarm_common_defines.h"
#include "include/swarm_queues.h"
#include "include/uart.h"

volatile static int s_IntMsgCounter=0;
volatile static int s_MainLoopMsgCounter=0;

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
	//debug("testSwarmMessageBus:START");
	//debug("testSwarmMessageBus:PUSH");
	struct SWARM_MSG msg;
	long i;
	char debugMsg[1024];
	for(i=0; i < MAX_SWARM_MSG_BUS_SIZE + 8 ; i++)
	{
		sprintf(debugMsg, "\r\npushing message=%d", s_MainLoopMsgCounter);
		//debug(debugMsg);
		msg.swarm_msg_type=eLinkLoopback; 
		sprintf(msg.swarm_msg_payload, "from main loop "
		"MESSAGE NUM=%d", s_MainLoopMsgCounter++);
		pushSwarmMsgBus(msg, 0);
	}
	sprintf(debugMsg, "\r\n last inserted id=%d", (s_MainLoopMsgCounter-1));
	debug(debugMsg);
	//debug("testSwarmMessageBus:POP"); 
	msg = popSwarmMsgBus(0);
	while(eLinkNullMsg != msg.swarm_msg_type)
	{
		sprintf(debugMsg, "\r\ngot MSG"
			"\r\n type=%d"
			"\r\n payload=%s", msg.swarm_msg_type, msg.swarm_msg_payload);
		debug(debugMsg);
		msg = popSwarmMsgBus(0);
	}	
	//debug("\r\nDone");
}
volatile uint16_t m_unitsOf1ms=0;
volatile uint16_t timecount=0;

ISR(SIG_OVERFLOW0)
{
  TCNT0=26;
  if(++timecount == m_unitsOf1ms)
    {
    	if(1)
    	{
    		struct SWARM_MSG msg;
			long i;
			for(i=0; i < MAX_SWARM_MSG_BUS_SIZE - 8; i++)
			{
				char debugMsg[1024];
				sprintf(debugMsg, "\r\nPUSHING MSG=%d", (s_IntMsgCounter +1));
				//debug(debugMsg);
				msg.swarm_msg_type=eLinkLoopback; 
				sprintf(msg.swarm_msg_payload, "from isr "
				"message num=%d", s_IntMsgCounter++);
				pushSwarmMsgBus(msg, 1);
			}
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

void testAnyncMessageBus(void)
{
	DDRB = 0xff;
	initTimer0(5);
	uart_init(dummyHandler, dummyHandler, dummyHandler, dummyPop, dummyPop);
	//initSwarmMsgBus(debug);
	sei();
	debug("----START");
	while(1)
	{
		//if(timecount == m_unitsOf1ms/2)
			testSwarmMessageBus();
		blinkLedPortB7();
	}
}

void testSyncMessageBus(void)
{
	DDRB = 0xff;
	//initTimer0(5);
	uart_init(dummyHandler, dummyHandler, dummyHandler, dummyPop, dummyPop);
	//initSwarmMsgBus(debug);
	sei();
	debug("----START");
	while(1)
	{
		testSwarmMessageBus();
	}
}

int main(void)
{
	//testSyncMessageBus();
	testAnyncMessageBus();
}
