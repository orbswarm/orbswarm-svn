#include <stdio.h>
#include "include/swarm_common_defines.h"
#include "include/xbee.h"

enum EXbeeStraightSerialRxStates {
  eXbeeStraightSerialRxInit,
  eXbeeStraightSerialRxStartMsg,
  eXbeeStraightSerialRxPayload
};

volatile static int s_nXbeeRxState=eXbeeStraightSerialRxInit;
volatile static int s_xbeeRxIsError=0;
volatile static unsigned long s_nXbeeRxStateByteNum=0;
volatile static struct SWARM_MSG s_xbeeRxPacket;
static void (*volatile s_debugCallback)(void)=0;
static void (*volatile s_debug)(const char* debugMsg)=0;
static void (*volatile s_pushSwarmMsgBus)(struct SWARM_MSG msg, int isInterruptCtx)=0;

void initXbeeModule( void (*pushSwarmMsgBus)(struct SWARM_MSG msg, int isInterruptCtx),
			void (*debugCallback)(void),
		    void (*debug)(const char*) )
{
  s_debugCallback=debugCallback;
  s_debug=debug;
  s_pushSwarmMsgBus=pushSwarmMsgBus;
}

static void debugCallback(void)
{
  if(0 != s_debugCallback)
    (*s_debugCallback)();
}

/*static void debug(const char* debugMsg)
{
  if(0 != s_debug)
    (*s_debug)(debugMsg);
}*/

static void initXbeeMsgStart(char c)
{
  s_nXbeeRxState=eXbeeStraightSerialRxStartMsg;
  s_nXbeeRxStateByteNum=0;
  s_xbeeRxIsError=0;
  if('{' ==c)
    s_xbeeRxPacket.swarm_msg_type=eLinkXbeeMsg;
  s_xbeeRxPacket.swarm_msg_payload[s_nXbeeRxStateByteNum]=c;
}

void handleXbeeSerial(char c, int isError)
{
/*	char strDebugMsg[1024];
	sprintf(strDebugMsg, "\r\nhandleXbeeSerial.state=%d", s_nXbeeRxState);
	debug(strDebugMsg);
	sprintf(strDebugMsg, "\r\nhandleXbeeSerial.char=%c",c);*/ 
  //if it's an error flag and discard till the start of the next message
  if(isError){
    //debug("\nerror flag set");
    s_xbeeRxIsError=isError;
    debugCallback();
    return;
  }
  switch(s_nXbeeRxState){
  case eXbeeStraightSerialRxInit:
    if('{' ==c){
      initXbeeMsgStart(c);
    }
    break;
  case eXbeeStraightSerialRxStartMsg:
    if('{'==c){
      //empty message
      initXbeeMsgStart(c);
    }
    else{
      s_nXbeeRxState=eXbeeStraightSerialRxPayload;
      s_nXbeeRxStateByteNum++;
      s_xbeeRxPacket.swarm_msg_payload[s_nXbeeRxStateByteNum]=c;
    }
    break;
  case eXbeeStraightSerialRxPayload:
    if('{'==c){
      //empty message
      initXbeeMsgStart(c);
    }
    else if('}'==c)
    {
		s_xbeeRxPacket.swarm_msg_payload[++s_nXbeeRxStateByteNum]=c;
		 //if not error first dispatch old message
	 	if(!s_xbeeRxIsError){
	   		s_xbeeRxPacket.swarm_msg_payload[s_nXbeeRxStateByteNum+1]='\0';
	   		(*s_pushSwarmMsgBus)(s_xbeeRxPacket, 1);
	 	}
	 	s_nXbeeRxState=eXbeeStraightSerialRxInit;
     }
     else
     {
	 	//check for max size
	 	s_nXbeeRxStateByteNum++;
	 	if(s_nXbeeRxStateByteNum 
	    	<= (MAX_SWARM_MSG_LENGTH-1)/*leave 1 byte for the new line */ )
	   		s_xbeeRxPacket.swarm_msg_payload[s_nXbeeRxStateByteNum]=c;
/*	   	else
	   		debug("maximum size exceeded for message");*/
	 //else ignore till the end
     }
     break;
    default:
     break;    
  }
}

