
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

void testSwarmMessageBusMainloop(void)
{
	struct SWARM_MSG msg;
	char debugMsg[1024];
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
static volatile uint16_t m_unitsOf1ms=0;
static volatile uint16_t timecount=0;
static void (*volatile _timerHandler)(void)=0;

ISR(SIG_OVERFLOW0)
{
	if(0!= _timerHandler)
		(*_timerHandler)();
}

void testAsyncMessageBusIntHandler(void)
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


void initTimer0(uint16_t units1ms, void (*intHandler)(void))
{
  timecount=0;
  m_unitsOf1ms=units1ms;
  _timerHandler=intHandler;
  TCNT0=26;
  TCCR0B = TCCR0B | ((1<<CS01) | (1<<CS00));
  TIMSK0= TIMSK0 | (1<<TOIE0);
}

void testAnyncMessageBus(void)
{
	DDRB = 0xff;
	initTimer0(25,testAsyncMessageBusIntHandler);
	uart_init(dummyHandler, dummyHandler, dummyHandler, dummyPop, dummyPop);
	initSwarmQueues(debug);
	sei();
	debug("----START");
	while(1)
	{
		//if(timecount == m_unitsOf1ms/2)
			testSwarmMessageBusMainloop();
		blinkLedPortB7();
	}
}

void testSyncMessageBus(void)
{
	DDRB = 0xff;
	//initTimer0(5);
	uart_init(dummyHandler, dummyHandler, dummyHandler, dummyPop, dummyPop);
	initSwarmQueues(debug);
	sei();
	debug("----START");
	while(1)
	{
		testSwarmMessageBusMainloop();
	}
}

void testXbeeQueueInAsyncModeMainloop(void)
{
	long i;
	for(i=0; i < MAX_XBEE_MSG_QUEUE_SIZE - 8; i++)
	{
		debug("\r\nPUSHING MSG=hello world");
		pushXbeeDataQ("hello world", 0/*false*/);
	}
}

void testXbeeQueueInAsyncModeIntHandler(void)
{
//	debug("testXbeeQueueInAsyncModeIntHandler:START");
  char debugMsg[1024];
  TCNT0=26;
  if(++timecount == m_unitsOf1ms)
    {
    	if(1)
    	{
			char msg = popXbeeDataQ(1);
			sprintf(debugMsg, "\r\ngot char=%c", msg);
			debug(debugMsg);
			while(0 != msg)
			{
				msg =popXbeeDataQ(1); 
				sprintf(debugMsg, "\r\ngot char=%c", msg);
				debug(debugMsg);
			}	
		}
		timecount=0;
    }
}

void testXbeeQueueInAsyncMode(void)
{
	DDRB =0xff;
	initTimer0(500, testXbeeQueueInAsyncModeIntHandler);
	uart_init(dummyHandler, dummyHandler, dummyHandler, dummyPop, 
		dummyPop);
	initSwarmQueues(debug);
	startAsyncXBeeTransmit();
	sei();
	debug("------START");
	while(1){
		testXbeeQueueInAsyncModeMainloop();
		blinkLedPortB7();
	}
}

int main(void)
{
	//testSyncMessageBus();
	//testAnyncMessageBus();
	testXbeeQueueInAsyncMode();
	return 0;
}

