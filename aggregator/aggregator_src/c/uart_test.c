#include <avr/io.h>
#include <uart.h>
#include <spu.h>
#include <timer0.h>
#include <swarm_messaging.h>
#include <packet_type.h>

void lightLedPortB6(void)
{
  //PORTB = PORTB  | (1 << PB0);
  //PORTB  = PORTB & (~(1<<PB6));
  PORTB = PORTB ^ (1<<PB6);
}

void lightLedPortB7(void)
{
  //  PORTB = PORTB | (1 << PB1);
  //PORTB  = PORTB & (~(1<<PB7));
  PORTB = PORTB ^ (1<<PB7);
}

/*
void dummy_spu_handler(unsigned char c, int n)
{
  //sendDebugMsg("got char");
  unsigned char debugMsg[1];
  debugMsg[0]=c;
  //sendDebugMsg(debugMsg);
  if('B' == c)
    lightLedPortB6();
  struct SWARM_MSG msg;
  msg.swarm_msg_type=eLinkLoopback;
  long nLen=1;
  msg.swarm_msg_length[0]=(unsigned char)(nLen >> 8);
  msg.swarm_msg_length[1]=(unsigned char)nLen;
  msg.swarm_msg_payload[0]=c;
  pushQ(msg);
  //sendDebugMsg("pushed in Q");
}
*/

void dummy_spu_handler(unsigned char c, int n)
{
   if('B' == c)
    lightLedPortB6();
}

/*
int main(void)
{
  DDRB = 0xff;
  PORTB = 0xff;
  uart_init(dummy_spu_handler, 
	    dummy_spu_handler);
  //
  sei();
  while(1)
    {
      //show that we are alive and looping
      //loopTimer0(5000);
      //lightLedPortB7();
      struct SWARM_MSG msg = popQ();
 
      char debugMsg[2];
      ///msg.swarm_msg_payload[0]='B';
      debugMsg[0]=msg.swarm_msg_payload[0];
      debugMsg[1]='\0';
      sendDebugMsg("popped q");
      sendDebugMsg(debugMsg);
    }
  return 0;
}
*/


//
int main(void)
{
  DDRB = 0xff;
  PORTB =0xff;
  unsigned int baud =23;
  //  UBRR0H = (unsigned char)(baud>>8);		
  //  UBRR0L = (unsigned char)baud;			
  //  UCSR0B = (1<<RXEN0) | (1<<TXEN0);
  //  UCSR0C = (3<< UCSZ00);
  //  UCSR0C = (1<<UCSZ01) | (1<< UCSZ00);
  UCSR1B = (1<<RXCIE1) | (1<<RXEN1) | (1<<TXEN1);
  UCSR1C = (1<<UCSZ11) | (1<< UCSZ10);
  UBRR1 = 23;
  sei();
  while(1){
    loopTimer0(1000);
    while(!(UCSR1A<<(8-UDRE1))>>7)
      ;
    UCSR1A = UCSR1A & (~(1<<UDRE1));
    UDR1 = 'A';
    //
  }
  return 0;
}
//
