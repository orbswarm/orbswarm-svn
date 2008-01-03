#include <avr/io.h>		// include I/O definitions (port names, pin names, etc)
#include <avr/interrupt.h>	// include interrupt support
#include <stdio.h>

#include "include/timer0.h"
#include "include/swarm_common_defines.h"
#include "include/uart.h"
#include "include/spu.h"
#include "include/gps.h"
#include "include/xbee.h"
#include "include/swarm_queues.h"

//#define DEBUG_MODE

void debug(const char * s)
{
  #ifdef DEBUG_MODE
  	debugUART(s);
  #endif	
}

void unCondDebug(const char * s)
{
  	debugUART(s);
}

void blinkLedPortB6(void)
{
  PORTB = PORTB ^ (1<<PB6);
}

void blinkLedPortB7(void)
{
  PORTB = PORTB ^ (1<<PB7);
}

void setGpsMode(void)
{
  char strDebugMsg[1024];
  char ack[MAX_GPS_PACKET_LENGTH];
  char* msg=0;
  unCondDebug("\r\nReading init params if any");
  loopTimer0(1000);
  getPmtkMsg(ack, 0/*false*/);
  sprintf(strDebugMsg, "\r\nack=%s", ack);
  debug(strDebugMsg);

  debug("\r\nsending 313");
  msg="$PMTK313,1*2E\r\n";
  sendGPSAMsg(msg);
  loopTimer0(1000);
  getPmtkMsg(ack, 0/*false*/);
  sprintf(strDebugMsg, "\r\nack=%s", ack);
  debug(strDebugMsg);

  debug("\r\nsending 301");
  msg="$PMTK301,2*2D\r\n";
  sendGPSAMsg(msg);
  loopTimer0(1000);
  getPmtkMsg(ack, 0/*false*/);
  sprintf(strDebugMsg, "\r\nack=%s", ack);
  debug(strDebugMsg);

  debug("\r\nsending query");
  msg= "$PMTK401*37\r\n";
  sendGPSAMsg(msg);
  loopTimer0(1000);
  getPmtkMsg(ack, 0/*false*/);
  sprintf(strDebugMsg, "\r\nack=%s", ack);
  debug(strDebugMsg);

}

char dummyPop(int isInterruptCtx){return 0;}

void dummyHandler(char c,int isError, int isInterruptCtx){}

int main(void)
{
  char gps_msg_buffer[MAX_GPS_PACKET_LENGTH];
  
  uart_init(dummyHandler/*xbee handler*/,
    	    handleSpuSerial,/*spu handler*/
    	    handleGpsSerial,/*gps handler*/
	    dummyPop /*xbee pop*/,
	    dummyPop /* spu pop*/);
  //loopTimer0(2000);
  char strMsg[1024];
  sprintf(strMsg, "\r\n PORTB=%x", PORTB);
  unCondDebug(strMsg);
  
  DDRB = 0xff;
  PORTB = 0xff;

  //initXbeeModule(pushSwarmMsgBus,blinkLedPortB7, debug);
  initGpsModule(blinkLedPortB7, debug);
  initSpuModule(pushSwarmMsgBus, blinkLedPortB7, debug);
  unCondDebug("\r\ninit ");
  loopTimer0(2000);
  setGpsMode();
  blinkLedPortB6();
  sei();
  
  while(1){
    //
    struct SWARM_MSG msg = popSwarmMsgBus(0);
    if(msg.swarm_msg_type == eLinkXbeeMsg){
      //queue up for sending to spu when asked for
      //But if send is in progress wait for it to be over
      debug("\r\npushing into spu data q");
      while(isSpuSendInProgress())
	;
      pushSpuDataQ(msg.swarm_msg_payload, 0);
      startAsyncSpuTransmit();
    }
    else if(msg.swarm_msg_type == eLinkSpuMsg){
      //poor man's strcmp
      if(msg.swarm_msg_payload[0] == '$' &&
		msg.swarm_msg_payload[1] == 'A' &&
	 	msg.swarm_msg_payload[2] == 'g' &&
	 	msg.swarm_msg_payload[3] == '*')
		{
	  		blinkLedPortB7();
	  		while(isSpuSendInProgress())
	    		;
	  
	  		sendSpuMsg("!");
	  		getGpsGpggaMsg(gps_msg_buffer, 0);
	  		sendSpuMsg(gps_msg_buffer);
	  		sendSpuMsg(";");

 	  		getGpsGpvtgMsg(gps_msg_buffer, 0);
	  		sendSpuMsg(gps_msg_buffer);
	  		sendSpuMsg(";!");
		}
      else if(msg.swarm_msg_payload[0] == '$' &&
			msg.swarm_msg_payload[1] == 'A' &&
	 msg.swarm_msg_payload[2] == 's')
	{
	  debug("\r\nstreaming data through xbee");
	  pushXbeeDataQ(msg.swarm_msg_payload+3, 0);
	  startAsyncXBeeTransmit();
	} 
   }
    //
  } 
  return 0;
}
 
