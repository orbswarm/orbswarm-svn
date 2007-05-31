#include "uart.h"

volatile unsigned int qCntr, sendCntr;
volatile unsigned int isSendInProgress=0;
volatile unsigned char queue[100];

ISR(SIG_UART_TRANS)
{
  if(qCntr != sendCntr)
    UDR=queue[sendCntr++];
  else
     PORTB = 1 << PB5;
}

void sendmsg(char *s)
{
  qCntr=0;
  sendCntr=1;
  queue[qCntr++] = 0x0d;
  queue[qCntr++] = 0x0a;
  while(*s)
    queue[qCntr++] = *s++;
  UDR=queue[0];
}


void (*_handle)(char) ;

void loop(void)
{
    while(1){
    //check RXC status bit in USR
    if(UCSRA & (1<< RXC))
      {
	PORTB = 0;
	char ch =UDR;
	(*_handle)(ch);

      }
  }
}

int init(void (*handle)(char) )
{
  //enable rx, tx and tx complete interrupt
  UCSRB = (1<<RXEN) | (1<<TXEN) | (1<< TXCIE);
  //baud rate = 19200
  UBRRL = 0x33;
  DDRB=0xff;
  PORTB = 1 << PB5;
  sei();
  _handle=handle;
  return 0;
}
