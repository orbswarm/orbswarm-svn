#include <aggregator_utils.h>
#include <avr/io.h>
#include <uart.h>
#include <spu.h>
#include <timer0.h>

void debug(char * s)
{
  sendDebugMsg(s);
}



void lightLedPortB6(void)
{
  PORTB = PORTB ^ (1<<PB6);
}

void lightLedPortB7(void)
{
  PORTB = PORTB ^ (1<<PB7);
}

void setGpsMode(void)
{
  volatile char ack[MAX_GPS_PACKET_LENGTH];
  char * msg=0;
  debug("\r\nReading init params if any");
  loopTimer0(3000);
  getPmtkMsg(ack);
  debug(";ack=");
  debug(ack);

  debug("\r\nsending 313");
  msg="$PMTK313,1*2E\r\n";
  sendGPSAMsg(msg);
  loopTimer0(3000);
  getPmtkMsg(ack);
  debug(";ack=");
  debug(ack);

  debug("\r\nsending 301");
  msg="$PMTK301,2*2D\r\n";
  sendGPSAMsg(msg);
  loopTimer0(3000);
  getPmtkMsg(ack);
  debug(";ack=");
  debug(ack);

  debug("\r\nsending query");
  msg= "$PMTK401*37\r\n";
  sendGPSAMsg(msg);
  loopTimer0(3000);
  getPmtkMsg(ack);
  debug(";ack=");
  debug(ack);

}


int main(void)
{
  DDRB = 0xff;
  PORTB = 0xff;
  char gps_msg_buffer[MAX_GPS_PACKET_LENGTH];
  
  //initXbeeModule(lightLedPortB7, sendDebugMsg);
  //initGpsModule(lightLedPortB7, sendDebugMsg);
  //initSpuModule(lightLedPortB7, sendDebugMsg);
  //sendDebugMsg("init ");
  //
  uart_init(handleXbeeSerial,
    	    handleSpuSerial,
    	    handleGpsSerial,
	    popXbeeDataQ,
	    popSpuDataQ,
	    popSpuGpsDataQ);
  debug("\ninit ");
  setGpsMode();
  lightLedPortB6();
  sei();
  
  while(1){
    /*
    struct SWARM_MSG msg = popQ();
    if(msg.swarm_msg_type == eLinkMotorControl ||
	    msg.swarm_msg_type == eLinkArt){
      //queue up for sending to spu when asked for
      //But if send is in progress wait for it to be over
      debug("\r\npushing into spu data q");
      while(isSpuSendInProgress())
	;
      pushSpuDataQ(msg.swarm_msg_payload);
      startSpuTransmit();
    }
    else if(msg.swarm_msg_type == eLinkSpuMsg){
      //poor man's strcmp
      if(msg.swarm_msg_payload[0] == '$' &&
	msg.swarm_msg_payload[1] == 'A' &&
	 msg.swarm_msg_payload[2] == 'g' &&
	 msg.swarm_msg_payload[3] == '*')
	{
	  lightLedPortB7();
	  while(isSpuSendInProgress())
	    ;
	  
	  sendSpuMsg("!");
	  getGpsGpggaMsg(gps_msg_buffer);
	  sendSpuMsg(gps_msg_buffer);
	  sendSpuMsg(";");

 	  getGpsGpvtgMsg(gps_msg_buffer);
	  sendSpuMsg(gps_msg_buffer);
	  sendSpuMsg(";!");

      else if(msg.swarm_msg_payload[0] == '$' &&
	msg.swarm_msg_payload[1] == 'A' &&
	 msg.swarm_msg_payload[2] == 's')
	{
	  debug("\r\nstreaming data through xbee");
	  pushXbeeDataQ(msg.swarm_msg_payload+3);
	  startXBeeTransmit();
	} 
    }
    */
  } 
  //
  return 0;
}
 
