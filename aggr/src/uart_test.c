#include <avr/io.h>      // this contains all the IO port definitions
#include <avr/interrupt.h>
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

static char dummyPop(int isIntCtx)
{
/*	loopTimer0(1000);
	lightLedPortB7();*/
	return 'z';
}

void dummyHandler(char c, int isError){}

void testHandler(char c, int isError)
{
	if(isError){
		sendSpuMsg("\r\nerrah");
	}
	else if('B' == c){
    	
    	blinkLedPortB7();
		/*sendSpuMsg("\r\ngot char=");
    	char msg[2];
    	msg[0]=c;
    	msg[1]=0;
    	sendSpuMsg(msg);*/
	}
}

int main(void)
{
  DDRB = 0xff;
  //PORTB = 0xff;
  uart_init(dummyHandler, testHandler, dummyHandler, dummyPop, dummyPop);
  sei();
  sendSpuMsg("\r\nuart_test:START");
  //startAsyncSpuTransmit();
    while(1)
      {
	loopTimer0(1);
	blinkLedPortB6();
	//stopAsyncSpuTransmit();
	//sendSpuMsg("HELLO");
	//startAsyncSpuTransmit();
      }

}
