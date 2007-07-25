#include <avr/io.h>
#include <uart.h>
#include <spu.h>
#include <timer0.h>
#include <swarm_messaging.h>
#include <xbee.h>
#include <gps.h>
#include <packet_type.h>

volatile static struct SWARM_MSG s_lastNmeaPosition;

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

void spuHandler(unsigned char c, int n)
{
  lightLedPortB6();
  //anything from the SPU goes out XBee
  unsigned char msg[2];
  msg[0] = c;
  msg[1] = '\0';
  sendXBeeMsg(msg);
}

int main(void)
{
  DDRB = 0xff;
  PORTB = 0xff;
  //initXbeeModule(lightLedPortB6, sendDebugMsg);
  //initGpsModule(lightLedPortB6, sendDebugMsg);
  
  uart_init(handleXbeeSerial,
    	    spuHandler,
    	    handleGpsSerial);
  sei();
  lightLedPortB6();
  while(1){
    struct SWARM_MSG msg = popQ();
    if(msg.swarm_msg_type == eLinkMotorControl){
      //send to spu
      sendSpuMsg(msg.swarm_msg_payload);
      lightLedPortB7();
    }
    else if(msg.swarm_msg_type == eLinkNMEA){
      //send to spu
      sendSpuMsg(msg.swarm_msg_payload);
      lightLedPortB7();
      /*
       * Inspect msg to see if this is a message that tells us
       * about our position and store it if it is
       */
      
    }
  } 
  return 0;
}
