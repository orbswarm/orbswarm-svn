#include "include/swarm_common_defines.h"
#include "include/spu.h"
#include <stdio.h>

enum ESpuStraightSerialRxStates {
  eSpuStraightSerialRxInit=0x00,
  eSpuStraightSerialRxStartMsg,
  eSpuStraightSerialRxPayload
};

volatile static int spu_rx_state=eSpuStraightSerialRxInit;
volatile static int spu_rx_is_error=0;
volatile static unsigned long spu_rx_state_byte_num=0;
volatile static struct SWARM_MSG spu_rx_packet;
static void (* volatile _debugCallback)(void)=0;
static void (* volatile _debug)(const char* debugMsg)=0;
static void (* volatile _pushSwarmMsgBus)(struct SWARM_MSG msg, 
											int isInterruptCtx);

void initSpuModule( void (*pushSwarmMsgBus)(struct SWARM_MSG msg, 
											int isInterruptCtx), 
			void (*debugCallback)(void),
		    void (*debug)(const char*) )
{
  _debugCallback=debugCallback;
  _debug=debug;
  _pushSwarmMsgBus=pushSwarmMsgBus;
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

/**
 *  This method is to initialize the state machine and not the whole module.
 * To initialize the entire module, at startup, call initSpuModule()
 */
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
	char strDebugMsg[1024];
	sprintf(strDebugMsg, "\r\nhandleSpuSerial.state=%d", spu_rx_state);
	debug(strDebugMsg);
	sprintf(strDebugMsg, "\r\nhandleSpuSerial.char=%c",c); 
  debug(strDebugMsg);
  //if it's an error flag and discard till the start of the next message
  if(isError){

    debug("error flag set");

    spu_rx_is_error=isError;
    return;
  }
  switch(spu_rx_state){
  case eSpuStraightSerialRxInit:
    if('$'==c){
      debugCallback();
      initSpuMsgStart(c);
    }
    break;
  case eSpuStraightSerialRxStartMsg:
    if('$' ==c){
      //empty message
      initSpuMsgStart(c);
    }
    else{
      spu_rx_state=eSpuStraightSerialRxPayload;
      spu_rx_state_byte_num++;
      spu_rx_packet.swarm_msg_payload[spu_rx_state_byte_num]=c;
    }
    break;
  case eSpuStraightSerialRxPayload:
     if('$'==c)
     {
		 //if not error first dispatch old message
	 	if(!spu_rx_is_error){
	   		spu_rx_packet.swarm_msg_payload[spu_rx_state_byte_num+1]='\0';
	   		debug("pushing in Q");
	   		(*_pushSwarmMsgBus)(spu_rx_packet, 1/*true*/);
	 	}
	 	initSpuMsgStart(c);
     }
     else
     {
	 	//check for max size
	 	spu_rx_state_byte_num++;
	 	if(spu_rx_state_byte_num <= MAX_SWARM_MSG_LENGTH )
	   		spu_rx_packet.swarm_msg_payload[spu_rx_state_byte_num]=c;
	   	else
	   		debug("maximum size exceeded for message");
	 	//else ignore till the end
     }
     break;
   default:
     break;
  }
}
