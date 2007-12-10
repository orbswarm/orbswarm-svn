#include "include/uart.h"
#include "include/timer0.h"

void lightLedPortB6(void)
{
  PORTB = PORTB ^ (1<<PB6);
}

void lightLedPortB7(void)
{
  PORTB = PORTB ^ (1<<PB7);
}

char dummyPop(void)
{
	return 'A';
}

void dummyHandler(char c, int isError){}

void testHandler(char c, int isError)
{
	if(isError)
		sendSpuMsg("\r\nerrah");
	else if('B' == c){
    	lightLedPortB7();
    	sendSpuMsg("\r\ngot char=");
    	char msg[2];
    	msg[0]=c;
    	msg[1]=0;
    	sendSpuMsg(msg);
	}
}

int main(void)
{
  DDRB = 0xff;
  PORTB = 0xff;
  uart_init(dummyHandler, testHandler, dummyHandler, dummyPop, dummyPop,dummyPop);
  sei();
  sendSpuMsg("\r\nhello");
  //startSpuTransmit();
    while(1)
      {
	loopTimer0(1000);
	lightLedPortB6();
	//sendSpuMsg("hello");
      }

}
