#include <avr/io.h>		// include I/O definitions (port names, pin names, etc)
#include <avr/interrupt.h>	// include interrupt support
#include "uart.h"

static void (*_handleXBeeRecv)(unsigned char, int) ;
static void (*_handleSpuRecv)(unsigned char, int) ;

ISR(SIG_USART3_RECV)
{
  int nErrors=0;
  if((UCSR3A<<(8-FE3))>>7)
    {
      nErrors=1;
      //flip error bit back
      UCSR3A=UCSR3A ^ (1<<FE3);
    }
  if((UCSR3A<<(8-DOR3))>>7)
    {
      nErrors=1;
      //flip error bit back
      UCSR3A=UCSR3A ^ (1<<DOR3);
    }
  (*_handleXBeeRecv)(UDR3, nErrors);
}

ISR(SIG_USART0_RECV)
{
  int nErrors=0;
  if((UCSR0A<<(8-FE0))>>7)
    {
      nErrors=1;
      //flip error bit back
      UCSR0A=UCSR0A ^ (1<<FE0);
    }
  if((UCSR0A<<(8-DOR0))>>7)
    {
      nErrors=1;
      //flip error bit back
      UCSR0A=UCSR0A ^ (1<<DOR0);
    }
  (*_handleSpuRecv)(UDR0, nErrors);
}

void sendXBeeMsg(unsigned char *s)
{
  while(*s)
    {
      UDR3 = *s++;
      while(1)
	{
	  //Now wait for byte to be sent
	  if(UCSR3A & (1<<UDRE3))
	    break;
	}
    }
}

void sendSpuMsg(unsigned char *s)
{
  while(*s)
    {
      UDR0 = *s++;
      while(1)
	{
	  //Now wait for byte to be sent
	  if(UCSR0A & (1<<UDRE0))
	    break;
	}
    }
}

int uart_init(void (*handleXBeeRecv)(unsigned char c, int isError),
	 void (*handleSpuRecv)(unsigned char c, int isErrror))
{
  //Set up XBee on USART3
  //Asynchronous UART, no parity, 1 stop bit, 8 data bits, 9600 baud
  UCSR3B = (1<<RXCIE3) | (1<<RXEN3) | (1<<TXEN3);
  UCSR3C = (1<<UCSZ31) | (1<< UCSZ32);
  UBRR3 = 95;
  _handleXBeeRecv=handleXBeeRecv;
  //Set up SPU on USART0
  //Asynchronous UART, no parity, 1 stop bit, 8 data bits, 9600 baud
  UCSR0B = (1<<RXCIE0) | (1<<RXEN0) | (1<<TXEN0);
  UCSR0C = (1<<UCSZ01) | (1<< UCSZ02);
  UBRR0 = 95;
  _handleSpuRecv  = handleSpuRecv;
  sei();
  return 0;
}
