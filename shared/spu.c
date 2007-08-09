#include <swarm_messaging.h>
#include "include/spu.h"
#include <packet_type.h>
#include <stdlib.h>

volatile static int spu_rx_state=eSpuStraightSerialRxInit;
volatile static int spu_rx_is_error=0;
volatile static unsigned long spu_rx_state_byte_num=0;
volatile static struct SWARM_MSG spu_rx_packet;
static void (* volatile _debugCallback)(void)=0;
static void (* volatile _debug)(const char* debugMsg)=0;

void initSpuModule(void (*debugCallback)(void),
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

static void initSpuMsgStart(char c)
{
  spu_rx_state=eSpuStraightSerialRxStartMsg;
  spu_rx_state_byte_num=0;
  spu_rx_is_error=0;
  spu_rx_packet.swarm_msg_type=eLinkSpuMsg;
  spu_rx_packet.swarm_msg_payload[spu_rx_state_byte_num]=c;
}

void handleSpuSerial(char c, int isError)
{
  debug("state=");
  char stateStr[2];
  debug(itoa(spu_rx_state, stateStr, 10));
  char demsg[2];
  demsg[0]=c;
  demsg[1]='\0';
  debug(demsg);
  //if it's an error flag and discard till the start of the next message
  if(isError){
    //debugCallback();
    spu_rx_is_error=isError;
    return;
  }
  switch(spu_rx_state){
  case eSpuStraightSerialRxInit:
    debug("init");
    if('$'==c){
      debugCallback();
      debug("init1");
      initSpuMsgStart(c);
    }
    break;
  case eSpuStraightSerialRxStartMsg:
    debug("serail start");
    if('$' ==c){
      //empty message
      initSpuMsgStart(c);
    }
    else{
      debug("payload start");
      spu_rx_state=eSpuStraightSerialRxPayload;
      spu_rx_state_byte_num++;
      spu_rx_packet.swarm_msg_payload[spu_rx_state_byte_num]=c;
    }
    break;
  case eSpuStraightSerialRxPayload:
     debug("payload");
     if('$'==c)
       {
	 //if not error first dispatch old message
	 if(!spu_rx_is_error){
	   spu_rx_packet.swarm_msg_length[0] = 
	     (char)(spu_rx_state_byte_num>>8);
	   spu_rx_packet.swarm_msg_length[1] = (char)spu_rx_state_byte_num;
	   spu_rx_packet.swarm_msg_payload[spu_rx_state_byte_num+1]='\0';
	   debug("pushing in Q");
	   pushQ(spu_rx_packet);
	 }
	 initSpuMsgStart(c);
       }
     else
       {
	 //check for max size
	 spu_rx_state_byte_num++;
	 if(spu_rx_state_byte_num <= MAX_SWARM_MSG_LENGTH )
	   spu_rx_packet.swarm_msg_payload[spu_rx_state_byte_num]=c;
	 //else ignore till the end
       }
     break;
    default:
      break;
  }
}
