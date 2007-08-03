//#include <stdlib.h>
#include <swarm_messaging.h>
#include "include/gps.h"
#include <packet_type.h>


volatile static int gps_rx_state=eGpsStraightSerialRxInit;
volatile static int gps_rx_is_error=0;
volatile static unsigned long gps_rx_state_byte_num=0;
volatile static struct SWARM_MSG gps_rx_packet;
static void (*_debugCallback)(void)=0;
static void (*_debug)(const char* debugMsg)=0;

void initGpsModule(void (*debugCallback)(void),
		    void (*debug)(const char*) )
{
  _debugCallback=debugCallback;
  _debug=debug;
}

static void debugCallback(void)
{
  if(0 != _debugCallback)
    (*_debugCallback)();
}

static void debug(const char* debugMsg)
{
  if(0 != _debug)
    (*_debug)(debugMsg);
}

static void initGpsMsgStart(unsigned char c)
{
  gps_rx_state_byte_num=0;
  gps_rx_is_error=0;
  gps_rx_packet.swarm_msg_type=eLinkNMEA;
  gps_rx_packet.swarm_msg_payload[gps_rx_state_byte_num]=c;
}

 //we are interested in only these messages
  //$GPGGA,000243.000,3743.980645,N,12222.595411,W,1,8,1.18,28.298,M,-25.322,M,,*52
void dummyHandleGpsSerial(unsigned char c, int isError)
{
  debugCallback();
  debug("dummy");
}

void handleGpsSerial(unsigned char c, int isError)
{
  //if it's an error flag and discard till the start of the next message
  if(isError){
    gps_rx_is_error=isError;
    return;
  }
  switch(gps_rx_state){
  case eGpsStraightSerialRxInit:
    if('$'==c){
      initGpsMsgStart(c);
      gps_rx_state=eGpsStraightSerialRxPayload;
    }
    break;
  case eGpsStraightSerialRxPayload://$G
    if('$'==c){
      initGpsMsgStart(c);
      gps_rx_state=eGpsStraightSerialRxPayload;
    }
    else if('G' ==c)
      {
	gps_rx_state_byte_num++;
	gps_rx_packet.swarm_msg_payload[gps_rx_state_byte_num]=c;
	gps_rx_state=eGpsStraightSerialRxPayloadG;
      }
    else
      gps_rx_state=eGpsStraightSerialRxDiscard;
    break;
  case eGpsStraightSerialRxPayloadG://$GP
    if('$'==c){
      initGpsMsgStart(c);
      gps_rx_state=eGpsStraightSerialRxPayload;
    }
    else if('P' ==c)
      {
	gps_rx_state_byte_num++;
	gps_rx_packet.swarm_msg_payload[gps_rx_state_byte_num]=c;
	gps_rx_state=eGpsStraightSerialRxPayloadGP;
      }
    else
      gps_rx_state=eGpsStraightSerialRxDiscard;
    break;
  case eGpsStraightSerialRxPayloadGP://$GPG
    if('$'==c){
      initGpsMsgStart(c);
      gps_rx_state=eGpsStraightSerialRxPayload;
    }
    else if('G' ==c)
      {
	gps_rx_state_byte_num++;
	gps_rx_packet.swarm_msg_payload[gps_rx_state_byte_num]=c;
	gps_rx_state=eGpsStraightSerialRxPayloadGPG;
      }
    else
      gps_rx_state=eGpsStraightSerialRxDiscard;
    break;
  case eGpsStraightSerialRxPayloadGPG://$GPGG
    if('$'==c){
      initGpsMsgStart(c);
      gps_rx_state=eGpsStraightSerialRxPayload;
    }
    else if('G' ==c)
      {
	gps_rx_state_byte_num++;
	gps_rx_packet.swarm_msg_payload[gps_rx_state_byte_num]=c;
	gps_rx_state=eGpsStraightSerialRxPayloadGPGG;
      }
    else
      gps_rx_state=eGpsStraightSerialRxDiscard;
    break;
  case eGpsStraightSerialRxPayloadGPGG://$GPGGA
    if('$'==c){
      initGpsMsgStart(c);
      gps_rx_state=eGpsStraightSerialRxPayload;
    }
    else if('A' ==c)
      {
	gps_rx_state_byte_num++;
	gps_rx_packet.swarm_msg_payload[gps_rx_state_byte_num]=c;
	gps_rx_state=eGpsStraightSerialRxPayloadGPGGA;
      }
    else
      gps_rx_state=eGpsStraightSerialRxDiscard;
    break;
  case eGpsStraightSerialRxPayloadGPGGA:
     if('$'==c)
       {
	 //if not error first dispatch old message
	 if(!gps_rx_is_error){
	   gps_rx_packet.swarm_msg_length[0] = 
	     (unsigned char)(gps_rx_state_byte_num>>8);
	   gps_rx_packet.swarm_msg_length[1] = (unsigned char)gps_rx_state_byte_num;
	   gps_rx_packet.swarm_msg_payload[gps_rx_state_byte_num+1]='\0';
	   pushQ(gps_rx_packet);
	 }
	 initGpsMsgStart(c);
         gps_rx_state=eGpsStraightSerialRxPayload;
  
       }
     else
       {
	 //check for max size
	 gps_rx_state_byte_num++;
	 if(gps_rx_state_byte_num <= MAX_SWARM_MSG_LENGTH )
	   gps_rx_packet.swarm_msg_payload[gps_rx_state_byte_num]=c;
	 //else ignore till the end
       }
     break;
  case eGpsStraightSerialRxDiscard:
    if('$' ==c){
      initGpsMsgStart(c);
      gps_rx_state=eGpsStraightSerialRxPayload;
    }
    break;
  }

}

void handleGpsSerialBuf(const char* buf, long nNumBytes)
{
  for(int i=0; i < nNumBytes; i++)
    {
      handleGpsSerial((unsigned char)buf[i], 0);
    }
}

