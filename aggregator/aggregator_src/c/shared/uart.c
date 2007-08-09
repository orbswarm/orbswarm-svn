#include <avr/io.h>		// include I/O definitions (port names, pin names, etc)
#include <avr/interrupt.h>	// include interrupt support
#include "uart.h"

static void (*  _handleXBeeRecv)(char, int) ;
static void (*  _handleSpuRecv)(char, int) ;
static void (* _handleGpsARecv)(char, int) ;
static char (* _getXBeeOutChar)(void);
static char (* _getSpuOutChar)(void);
volatile static int s_isSpuSendInProgress=0;
volatile static int s_isXbeeSendInProgress=0;

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

ISR(SIG_USART1_RECV)
{
 
  int nErrors=0;
  if((UCSR1A<<(8-FE1))>>7)
    {
      nErrors=1;
      //flip error bit back
      UCSR1A=UCSR1A ^ (1<<FE1);
    }
  if((UCSR1A<<(8-DOR1))>>7)
    {
      nErrors=1;
      //flip error bit back
      UCSR1A=UCSR1A ^ (1<<DOR1);
    }

  (*_handleGpsARecv)(UDR1, nErrors);
  
}

ISR(SIG_USART3_DATA)
{
  PORTB = PORTB ^ (1<<PB7);
  char c = (*_getXBeeOutChar)();
  if(c !=0 ){
    UDR3 = c;
  }
  else{//end of q
    UCSR3B &= ~(1 << UDRIE3);//turn off interrupt
    s_isXbeeSendInProgress=0;
  }

}

ISR(SIG_USART0_DATA)
{
  PORTB = PORTB ^ (1<<PB7);
  char c = (*_getSpuOutChar)();
  if(c !=0 ){
    UDR0 = c;
  }
  else{//end of q
    UCSR0B &= ~(1 << UDRIE0);//turn off interrupt
    s_isSpuSendInProgress=0;
  }
}

void sendGPSAMsg(const char *s)
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

/* void sendXBeeMsg(const unsigned char *s) */
/* { */
/*   while(*s) */
/*     { */
/*       ///turn UDRE bit off first - init */
/*       UCSR3A = UCSR3A & (~(1<<UDRE3)); */
/*       UDR3 = *s++; */
/*       while(1) */
/* 	{ */
/* 	  //Now wait for byte to be sent */
/* 	  if((UCSR3A<<(8-UDRE3))>>7) */
/* 	    break; */
/* 	} */
/*     } */
/* } */

/* void sendSpuMsg(const char *s) */
/* { */
/*   while(*s) */
/*     { */
/*       UCSR0A = UCSR0A & (~(1<<UDRE0)); */
/*       UDR0 = *(s++); */
/*       while(1) */
/* 	{ */
/* 	  //loopTimer0(100); */
/* 	  if((UCSR0A<<(8-UDRE0))>>7) */
/* 	    break; */
/* 	} */
/*     } */
/* } */

void sendGPSBMsg(const char *s)
{
  while(*s)
    {
      UCSR2A = UCSR2A & (~(1<<UDRE2));
      UDR2 = *(s++);
      while(1)
	{
	  if((UCSR2A<<(8-UDRE2))>>7)
	    break;
	}
    }
}


void sendDebugMsg(const char *s)
{
  //sendXBeeMsg (s);
  //sendSpuMsg(s);
}

void startXBeeTransmit(void)
{
  if(!s_isXbeeSendInProgress){
    s_isXbeeSendInProgress=1;
    UCSR3B |=  (1 <<UDRIE3);//enable interrupt
    UCSR3A |= (1 <<UDRE3);//set 'data register empty' bit to 1(buffer empty)
  }
}

void startSpuTransmit(void)
{
  if(!s_isSpuSendInProgress){
    s_isSpuSendInProgress=1;
    UCSR0B |=  (1 <<UDRIE0);//enable interrupt
    UCSR0A |= (1 <<UDRE0);//set 'data register empty' bit to 1(buffer empty)
  }
}

int isSpuSendInProgress(void)
{
  return s_isSpuSendInProgress;
}


int isXBeeSendInProgress(void)
{
  return s_isXbeeSendInProgress;
}

int uart_init( void (*handleXBeeRecv)(char c, int isError),
	       void (*handleSpuRecv)(char c, int isErrror),
	       void (*handleGpsARecv)(char c, int isErrror),
	       char (*getXBeeOutChar)(void),
	       char (*getSpuOutChar)(void))
{
  //Set up XBee on USART3
  //Asynchronous UART, no parity, 1 stop bit, 8 data bits, 38400 baud
  //
  UCSR3B = (1<<RXCIE3) | (1<<RXEN3) | (1<<TXEN3) ;
  UCSR3C = (1<<UCSZ31) | (1<< UCSZ30);
  UBRR3 = 23;
  _handleXBeeRecv=handleXBeeRecv;
  _getXBeeOutChar=getXBeeOutChar;
  //
  //
  //Set up SPU on USART0
  //Asynchronous UART, no parity, 1 stop bit, 8 data bits, 38400 baud
  UCSR0B = (1<<RXCIE0) | (1<<RXEN0) | (1<<TXEN0);
  UCSR0C = (1<<UCSZ01) | (1<< UCSZ00);
  UBRR0 = 23;
  _handleSpuRecv  = handleSpuRecv;
  _getSpuOutChar= getSpuOutChar;
  //
  
  //Set up GPSA
  //
  UCSR1B = (1<<RXCIE1) | (1<<RXEN1) | (1<<TXEN1);
  UCSR1C = (1<<UCSZ11) | (1<< UCSZ10);
  UBRR1 = 23;
  _handleGpsARecv = handleGpsARecv;
  //


  //set up GPSB
  //UCSR2B = (1<<RXCIE2) | (1<<RXEN2) | (1<<TXEN2);
  //UCSR2C = (1<<UCSZ21) | (1<< UCSZ20);
  //UBRR2 = 23;
  //insert handler here later

  //sei();
  return 0;
}
