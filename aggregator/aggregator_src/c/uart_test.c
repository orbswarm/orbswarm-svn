#include <avr/io.h>		// include I/O definitions (port names, pin names, etc)
#include <avr/interrupt.h>	// include interrupt support


int testUart3(void)
{
    //Set up SPU on USART0
  //Asynchronous UART, no parity, 1 stop bit, 8 data bits, 38400 baud
  loopTimer0(1000);
  DDRB=0xff;
  //
  UCSR3B = (1<<RXCIE3) | (1<<RXEN3) | (1<<TXEN3);
  UCSR3C = (1<<UCSZ31) | (1<< UCSZ30);
  UBRR3 = 23;
  //

  while(1)
    {
      loopTimer0(1000);
      PORTB = PORTB ^ (1<<PB6);
      UCSR3A = UCSR3A & (~(1<<UDRE3));
      UDR3 = 'B';
      /*
      while(1)
	{
	  loopTimer0(1000);
	  if((UCSR0A<<(8-UDRE0))>>7)
	    break;
	}
      */
    }
//

}

int testUart0(void)
{
    //Set up SPU on USART0
  //Asynchronous UART, no parity, 1 stop bit, 8 data bits, 38400 baud
  loopTimer0(1000);
  DDRB=0xff;
  //
  UCSR0B = (1<<RXCIE0) | (1<<RXEN0) | (1<<TXEN0);
  UCSR0C = (1<<UCSZ01) | (1<< UCSZ00);
  UBRR0 = 23;
  //

  while(1)
    {
      loopTimer0(1000);
      PORTB = PORTB ^ (1<<PB6);
      UCSR0A = UCSR0A & (~(1<<UDRE0));
      UDR0 = 'B';
      /*
      while(1)
	{
	  loopTimer0(1000);
	  if((UCSR0A<<(8-UDRE0))>>7)
	    break;
	}
      */
    }
//

}

int main(){
  testUart3();
}
