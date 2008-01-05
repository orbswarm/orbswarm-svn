#include <avr/io.h>		// include I/O definitions (port names, pin names, etc)
#include <avr/interrupt.h>	// include interrupt support
#include "include/timer0.h"

void blinkLedPortB6(void)
{
  PORTB = PORTB ^ (1<<PB6);
}

void blinkLedPortB7(void)
{
  PORTB = PORTB ^ (1<<PB7);
}


void testUart0(void)
{
    //Set up SPU on USART0
  //Asynchronous UART, no parity, 1 stop bit, 8 data bits, 38400 baud
  loopTimer0(1000);
  DDRB=0xff;
  //
  UCSR0B = (1<<RXCIE0) | (1<<RXEN0) | (1<<TXEN0);
  UCSR0C = (1<<UCSZ01) | (1<< UCSZ00);
  UBRR0 = 11;
  //
  loopTimer0(5000);
  while(1)
    {
      loopTimer0(1000);
      blinkLedPortB7();
      while(!(UCSR0A & (1<<UDRE0)))
			;
      UDR0 = 'B';
    }
}

int main(){
	sei();
  	testUart0();
  	return 0;
}
