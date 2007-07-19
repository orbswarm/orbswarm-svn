#include <stdlib.h>
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
  gps_rx_state=eGpsStraightSerialRxStartMsg;
  gps_rx_state_byte_num=0;
  gps_rx_is_error=0;
  gps_rx_packet.swarm_msg_type=eLinkNMEA;
  gps_rx_packet.swarm_msg_payload[gps_rx_state_byte_num]=c;
}

void handleGpsSerial(unsigned char c, int isError)
{
  debugCallback();
  //debug("in handle");
  //if it's an error flag and discard till the start of the next message
  if(isError){
    //debugCallback();
    gps_rx_is_error=isError;
    return;
  }
  switch(gps_rx_state){
  case eGpsStraightSerialRxInit:
    if('$'==c){
      //debugCallback();
      initGpsMsgStart(c);
    }
    break;
  case eGpsStraightSerialRxStartMsg:
    if('$' ==c){
      //empty message
      initGpsMsgStart(c);
    }
    else{
      gps_rx_state=eGpsStraightSerialRxPayload;
      gps_rx_state_byte_num++;
      gps_rx_packet.swarm_msg_payload[gps_rx_state_byte_num]=c;
    }
    break;
  case eGpsStraightSerialRxPayload:
     if('$'==c)
       {
	 //if not error first dispatch old message
	 if(!isError){
	   gps_rx_packet.swarm_msg_length[0] = 
	     (unsigned char)(gps_rx_state_byte_num>>8);
	   gps_rx_packet.swarm_msg_length[1] = (unsigned char)gps_rx_state_byte_num;
	   gps_rx_packet.swarm_msg_payload[gps_rx_state_byte_num+1]='\0';
	   pushQ(gps_rx_packet);
	 }
	 initGpsMsgStart(c);
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
     //default:
    //debugCallback();
  }
  //debugCallback();
}

void handleGpsSerialBuf(const char* buf, long nNumBytes)
{
  for(int i=0; i < nNumBytes; i++)
    {
      handleGpsSerial((unsigned char)buf[i], 0);
    }
}
