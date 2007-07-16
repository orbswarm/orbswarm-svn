#include <avr/io.h>		// include I/O definitions (port names, pin names, etc)
#include <avr/interrupt.h>	// include interrupt support
#include "uart.h"

static void (*  _handleXBeeRecv)(unsigned char, int) ;
static void (*  _handleSpuRecv)(unsigned char, int) ;
static void (* _handleGpsARecv)(unsigned char, int) ;

ISR(SIG_USART3_RECV)
{
  int nErrors=0;
  /*
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
  */
  (*_handleXBeeRecv)(UDR3, nErrors);
}

ISR(SIG_USART0_RECV)
{
  int nErrors=0;
  /*
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
  */
  (*_handleSpuRecv)(UDR0, nErrors);
}

ISR(SIG_USART1_RECV)
{
  int nErrors=0;
  (* _handleGpsARecv)(UDR1, nErrors);
}

void sendGPSAMsg(const unsigned char *s)
{
  while(*s)
    {
      ///turn UDRE bit off first - init
      UCSR1A = UCSR1A & (~(1<<UDRE1));
      UDR1 = *s++;
      while(1)
	{
	  //Now wait for byte to be sent
	  if((UCSR1A<<(8-UDRE1))>>7)
	    break;
	}
    }
}

void sendXBeeMsg(const unsigned char *s)
{
  while(*s)
    {
      ///turn UDRE bit off first - init
      UCSR3A = UCSR3A & (~(1<<UDRE3));
      UDR3 = *s++;
      while(1)
	{
	  //Now wait for byte to be sent
	  if((UCSR3A<<(8-UDRE3))>>7)
	    break;
	}
    }
}

void sendSpuMsg(const unsigned char *s)
{
  while(*s)
    {
      UCSR0A = UCSR0A & (~(1<<UDRE0));
      UDR0 = *(s++);
      while(1)
	{
	  //loopTimer0(100);
	  if((UCSR0A<<(8-UDRE0))>>7)
	    break;
	}
    }
}

void sendDebugMsg(const char *s)
{
  sendXBeeMsg((unsigned char*)s);
}

int uart_init(void (*handleXBeeRecv)(unsigned char c, int isError),
	      void (*handleSpuRecv)(unsigned char c, int isErrror),
	      void (*handleGpsARecv)(unsigned char c, int isErrror))
{
  //Set up XBee on USART3
  //Asynchronous UART, no parity, 1 stop bit, 8 data bits, 38400 baud
  UCSR3B = (1<<RXCIE3) | (1<<RXEN3) | (1<<TXEN3);
  UCSR3C = (1<<UCSZ31) | (1<< UCSZ30);
  UBRR3 = 23;
  _handleXBeeRecv=handleXBeeRecv;
  //Set up SPU on USART0
  //Asynchronous UART, no parity, 1 stop bit, 8 data bits, 38400 baud
  UCSR0B = (1<<RXCIE0) | (1<<RXEN0) | (1<<TXEN0);
  UCSR0C = (1<<UCSZ01) | (1<< UCSZ00);
  UBRR0 = 23;
  _handleSpuRecv  = handleSpuRecv;
  //Set up GPSA
  UCSR1B = (1<<RXCIE1) | (1<<RXEN1) | (1<<TXEN1);
  UCSR1C = (1<<UCSZ11) | (1<< UCSZ10);
  UBRR1 = 23;
  _handleGpsARecv = handleGpsARecv;
  //enable interrupts in main routine only
  sei();
  return 0;
}
