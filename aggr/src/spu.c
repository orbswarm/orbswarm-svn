#include "include/swarm_common_defines.h"
#include "include/spu.h"
#include <stdio.h>

enum ESpuStraightSerialRxStates {
  eSpuStraightSerialRxInit=0x00,
  eSpuStraightSerialRxStartMsg,
  eSpuStraightSerialRxPayload
};

volatile static int s_nSpuRxState=eSpuStraightSerialRxInit;
volatile static int s_isSpuRxError=0;
volatile static unsigned long s_nSpuRxStateByteNum=0;
volatile static struct SWARM_MSG s_spuRxPacket;
static void (* volatile s_debugCallback)(void)=0;
static void (* volatile s_debug)(const char* debugMsg)=0;
static void (* volatile s_pushSwarmMsgBus)(struct SWARM_MSG msg, 
											int isInterruptCtx);

void initSpuModule( void (*pushSwarmMsgBus)(struct SWARM_MSG msg, 
											int isInterruptCtx), 
			void (*debugCallback)(void),
		    void (*debug)(const char*) )
{
  debug ("\r\ninitSpuModule");
  s_debugCallback = debugCallback;
  s_debug = debug;
  s_pushSwarmMsgBus = pushSwarmMsgBus;
}

static void debugCallback(void)
{
  if(0 != s_debugCallback)
    (*s_debugCallback)();
}

static void debug(const char* debugMsg)
{
  if(0 != s_debug)
    (*s_debug)(debugMsg);
}

/**
 *  This method is to initialize the state machine and not the whole module.
 * To initialize the entire module, at startup, call initSpuModule()
 */
static void initSpuMsgStart(char c)
{
  s_nSpuRxState=eSpuStraightSerialRxStartMsg;
  s_nSpuRxStateByteNum=0;
  s_isSpuRxError=0;
  s_spuRxPacket.swarm_msg_type=eLinkSpuMsg;
  s_spuRxPacket.swarm_msg_payload[s_nSpuRxStateByteNum]=c;
}

/*int isDebug()
{
	return 0 == s_debug; 
}*/

void handleSpuSerial(char c, int isError)
{
	//char strDebugMsg[1024];
  //if it's an error flag and discard till the start of the next message
  
  if(isError){
    s_isSpuRxError=isError;
	debugCallback();    
    return;
  }
  switch(s_nSpuRxState){
  case eSpuStraightSerialRxInit:
    if('$'==c){
      initSpuMsgStart(c);
    }
    break;
  case eSpuStraightSerialRxStartMsg:
    if('$' ==c){
      //empty message
      initSpuMsgStart(c);
    }
    else{
      s_nSpuRxState=eSpuStraightSerialRxPayload;
      s_nSpuRxStateByteNum++;
      s_spuRxPacket.swarm_msg_payload[s_nSpuRxStateByteNum]=c;
    }
    break;
  case eSpuStraightSerialRxPayload:
     if('$'==c)
     {
		 //if not error first dispatch old message
	 	if(!s_isSpuRxError){
	   		s_spuRxPacket.swarm_msg_payload[s_nSpuRxStateByteNum+1]='\0';
//	   		debug("pushing in Q");
	   		(*s_pushSwarmMsgBus)(s_spuRxPacket, 1);
	 	}
	 	initSpuMsgStart(c);
     }
     else
     {
	 	//check for max size
	 	s_nSpuRxStateByteNum++;
	 	if(s_nSpuRxStateByteNum <= MAX_SWARM_MSG_LENGTH )
	   		s_spuRxPacket.swarm_msg_payload[s_nSpuRxStateByteNum]=c;
//	   	else
//	   		debug("maximum size exceeded for message");
	 	//else ignore till the end
     }
     break;
   default:
     break;
  }  
}
