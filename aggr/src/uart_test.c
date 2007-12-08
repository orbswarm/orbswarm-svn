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

char dummyPop(void){return 'A';}

void dummyHander(char c, int isError)
{
	if('B' == c)
    	lightLedPortB6();
}

int main(void)
{
  DDRB = 0xff;
  PORTB = 0xff;
  uart_init(dummyHander, dummyHander, dummyHander, dummyPop, dummyPop,dummyPop);
  sei();
    while(1)
      {
	loopTimer0(2000);
	lightLedPortB6();
      }

}
