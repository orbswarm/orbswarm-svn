#include <avr/io.h>
#include <uart.h>
#include <spu.h>
#include <timer0.h>
#include <swarm_messaging.h>
#include <xbee.h>
#include <gps.h>
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

void dummy_spu_handler(unsigned char c, int n)
{
  lightLedPortB6();
}

int main(void)
{
  DDRB = 0xff;
  PORTB = 0xff;
  initXbeeModule(lightLedPortB6, sendDebugMsg);
  initGpsModule(lightLedPortB6, sendDebugMsg);
  //initXbeeModule(lightLedPortB7, 0);
    uart_init(handleXbeeSerial,
    	    dummy_spu_handler,
    	    handleGpsSerial);
  //    uart_init(dummy_spu_handler,
  //  	    dummy_spu_handler,
  //  	    dummy_spu_handler);
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
    }
  } 
  return 0;
}
