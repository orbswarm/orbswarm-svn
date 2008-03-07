#include <avr/io.h>		// include I/O definitions (port names, pin names, etc)
#include <avr/interrupt.h>	// include interrupt support
#include <stdio.h>
#include <string.h>

#include "include/timer0.h"
#include "include/swarm_common_defines.h"
#include "include/uart.h"
#include "include/spu.h"
#include "include/gps.h"
#include "include/xbee.h"
#include "include/swarm_queues.h"

#define GPS_TIMER_VAL_MILLIS 200

//#define DEBUG_MODE

void
debug (const char *s)
{
#ifdef DEBUG_MODE
  debugUART (s);
#endif
}

void
info (const char *s)
{
  debugUART (s);
}

void
blinkLedPortB6 (void)
{
  PORTB = PORTB ^ (1 << PB6);
}

void
blinkLedPortB7 (void)
{
  PORTB = PORTB ^ (1 << PB7);
}

void
setGpsMode (void)
{
  char strDebugMsg[1024];
  char ack[MAX_GPS_PACKET_LENGTH];
  char *msg = 0;
  info ("\r\nReading init params if any");
  loopTimer0 (2000);
  getPmtkMsg (ack, 0 /*false */ );
  sprintf (strDebugMsg, "\r\nack=%s", ack);
  info (strDebugMsg);

  info ("\r\nsending 313");
  msg = "$PMTK313,1*2E\r\n";
  sendGPSAMsg (msg);
  loopTimer0 (2000);
  getPmtkMsg (ack, 0 /*false */ );
  sprintf (strDebugMsg, "\r\nack=%s", ack);
  info (strDebugMsg);

  info ("\r\nsending 301");
  msg = "$PMTK301,2*2D\r\n";
  sendGPSAMsg (msg);
  loopTimer0 (2000);
  getPmtkMsg (ack, 0 /*false */ );
  sprintf (strDebugMsg, "\r\nack=%s", ack);
  info (strDebugMsg);

  info ("\r\nsending query");
  msg = "$PMTK401*37\r\n";
  sendGPSAMsg (msg);
  loopTimer0 (2000);
  getPmtkMsg (ack, 0 /*false */ );
  sprintf (strDebugMsg, "\r\nack=%s", ack);
  info (strDebugMsg);

}

char
dummyPop (int isInterruptCtx)
{
  return 0;
}

void
dummyHandler (char c, int isError)
{
}

static void stripChecksum(const char* gpsStr, char* destStr)
{
	size_t idx=strlen(gpsStr);
	for(; idx>=0; idx--)
	{
		if(','==gpsStr[idx])
		break;	
	}
	strncpy(destStr, gpsStr, idx-1);
}

int
main (void)
{
  char gps_msg_buffer[MAX_GPS_PACKET_LENGTH];

  uart_init (handleXbeeSerial /*xbee handler */ ,
	     handleSpuSerial,	/*spu handler */
	     handleGpsSerial,	/*gps handler */
	     popXbeeDataQ /*xbee pop */ ,
	     popSpuDataQ /* spu pop */ );
  //loopTimer0(2000);
  char strMsg[1024];
  sprintf (strMsg, "\r\n PORTB=%x", PORTB);
  info (strMsg);

  initXbeeModule (pushSwarmMsgBus, blinkLedPortB6, debug);
  initGpsModule (blinkLedPortB7, debug);
  initSpuModule (pushSwarmMsgBus, 0, debug);
  
  info ("\r\ninit ");
  sei ();
  setGpsMode();
  /*
     while(1){
     debug("\r\n sleep start");
     loopTimer0(20);
     debug("\r\n getting GPGGA msg");
     getGpsGpggaMsg(gps_msg_buffer, 0);
     sprintf(strMsg, "\r\n GPGGA=%s", gps_msg_buffer);
     debug(strMsg);
     }
   */
//
  DDRB = 0xff;
  setTimerInAsync(GPS_TIMER_VAL_MILLIS);
  while (1)
    {
      //debug("\r\n while start");
      struct SWARM_MSG msg = popSwarmMsgBus (0);
      if (msg.swarm_msg_type == eLinkXbeeMsg)
	{
	  //queue up for sending to spu when asked for
	  //But if send is in progress wait for it to be over
	  while (isSpuSendInProgress ())
	    ;
	  debug ("\r\npushing into spu data q");
	  pushSpuDataQ (msg.swarm_msg_payload, 0);
	  startAsyncSpuTransmit ();
	}
      else if (msg.swarm_msg_type == eLinkSpuMsg)
	{
	  if (0 == strncmp (msg.swarm_msg_payload, "$Ag*", 4))
	    {
	      while (isSpuSendInProgress ())
		;
	      debug ("\r\n Got request for PS");
	      sendSpuMsg ("!");
	      getGpsGpggaMsg (gps_msg_buffer, 0);
	      sendSpuMsg (gps_msg_buffer+1);
	      sendSpuMsg (";");

	      getGpsGpvtgMsg (gps_msg_buffer, 0);
	      sendSpuMsg (gps_msg_buffer+1);
	      sendSpuMsg (";!");
	    }
	  else if (0 == strncmp (msg.swarm_msg_payload, "$As", 3))
	    {
	      while (isSpuSendInProgress ())
		;
	      sprintf (strMsg, "\r\nstreaming data through xbee msg=%s",
		       msg.swarm_msg_payload + 3);
	      debug (strMsg);
	      pushXbeeDataQ (msg.swarm_msg_payload + 3, 0);
	      startAsyncXBeeTransmit ();
	    }
	}
      else
	{
	  //debug("\r\n invalid or null msg type");
	}
	if(isTimedOut())
	{
		char sanitizedGpsString[MAX_GPS_PACKET_LENGTH];
		
		while (isSpuSendInProgress ())
		;
	    debug ("\r\n Got request for PS");
	   	pushSpuDataQ ("{(", 0);
	    getGpsGpggaMsg (gps_msg_buffer, 0);
	    stripChecksum(gps_msg_buffer+1, sanitizedGpsString);
	    pushSpuDataQ (sanitizedGpsString, 0);
	    pushSpuDataQ (";", 0);
	    startAsyncSpuTransmit();

	    getGpsGpvtgMsg (gps_msg_buffer, 0);
	    stripChecksum(gps_msg_buffer+1, sanitizedGpsString);
	    pushSpuDataQ (sanitizedGpsString, 0);
	    pushSpuDataQ (")}", 0);
	    startAsyncSpuTransmit();
		setTimerInAsync(GPS_TIMER_VAL_MILLIS);
	}
	else
		debug("GPS has not timed out");
   }//end while
  //
  return 0;
}

