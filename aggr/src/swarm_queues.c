#include <string.h>
#include <stdio.h>
#include "include/swarm_common_defines.h"
#include "include/swarm_queues.h"

volatile static char s_spuDataQueue[MAX_SPU_MSG_QUEUE_SIZE];
volatile static unsigned long s_spuDataQueueHeadIdx=0;
volatile static unsigned long s_spuDataQueueTailIdx=0;

volatile static char s_xbeeDataQueue[MAX_XBEE_MSG_QUEUE_SIZE];
volatile static unsigned long s_xbeeDataQueueHeadIdx=0;
volatile static unsigned long s_xbeeDataQueueTailIdx=0;

volatile static struct SWARM_MSG s_queue[MAX_SWARM_MSG_BUS_SIZE];
volatile static unsigned long s_nHeadIdx=0;
volatile static unsigned long s_nTailIdx=0;

static void (* _debug)(const char*) =0;

void initSwarmMsgBus(void (*debug)(const char* c))
{
	_debug=debug;
}

static void DEBUG(const char* s)
{
	if(_debug != 0)
		(*_debug)(s);
}

void pushSpuDataQ(const char* msg)
{
  while(*msg)
    {
      unsigned long nTmpHead;
      nTmpHead = ( s_spuDataQueueHeadIdx + 1) & SPU_Q_MASK;
      if (nTmpHead == s_spuDataQueueTailIdx)
		return;
      s_spuDataQueue[nTmpHead]=*msg++;
      s_spuDataQueueHeadIdx=nTmpHead;
    }
  
}

char popSpuDataQ(void)
{
  unsigned long nTmpTail;
  if(s_spuDataQueueHeadIdx == s_spuDataQueueTailIdx)
    {
       return 0;
    }
  nTmpTail= (s_spuDataQueueTailIdx + 1) & SPU_Q_MASK;
  s_spuDataQueueTailIdx=nTmpTail;
  return s_spuDataQueue[nTmpTail];
}

char popXbeeDataQ(void)
{
  unsigned long nTmpTail;
  if(s_xbeeDataQueueHeadIdx == s_xbeeDataQueueTailIdx)
    {
       return 0;
    }
  nTmpTail= (s_xbeeDataQueueTailIdx + 1) & XBEE_Q_MASK;
  s_xbeeDataQueueTailIdx=nTmpTail;
  return s_xbeeDataQueue[nTmpTail];
}

void pushXbeeDataQ(const char* msg)
{
  while(*msg)
    {
      unsigned long nTmpHead;
      nTmpHead = ( s_xbeeDataQueueHeadIdx + 1) & XBEE_Q_MASK;
      if(nTmpHead == s_xbeeDataQueueTailIdx)
		return;
      s_xbeeDataQueue[nTmpHead]=*msg++;
      s_xbeeDataQueueHeadIdx=nTmpHead;
    }

}

static volatile int s_nSwarmMsgBusLock=0;

void pushSwarmMsgBus(struct SWARM_MSG msg, int isInterruptCtx)
{
  unsigned long nTmpHead;
  while(1){
  	if(s_nSwarmMsgBusLock == 0)
  	{
  	  	s_nSwarmMsgBusLock++;
  		if(1==s_nSwarmMsgBusLock)
	  	{
		  nTmpHead = ( s_nHeadIdx + 1) & SWARM_MSG_BUS_MASK;
		  while(nTmpHead == s_nTailIdx){
		  	unsigned long nTmpTail;
		  	nTmpTail= (s_nTailIdx + 1) & SWARM_MSG_BUS_MASK;
		  	s_nTailIdx=nTmpTail;
		  }
		  s_queue[nTmpHead]=msg;
		  s_nHeadIdx=nTmpHead;
		  s_nSwarmMsgBusLock--;
		  return;
	  	}
	  	s_nSwarmMsgBusLock--;
	  	return;
  	}
  	else if(isInterruptCtx)
  		return;
  }
}


struct SWARM_MSG popSwarmMsgBus(int isInterruptCtx)
{
	char debugMsg[1024];
  	DEBUG("popSwarmMsgBus:START");
  struct SWARM_MSG msg;
  msg.swarm_msg_type=eLinkNullMsg;
  strcpy(msg.swarm_msg_payload, "Null msg");
  while(1)
  {
  	  if(s_nSwarmMsgBusLock == 0)
  	  {
  	  	sprintf(debugMsg, "lock is 0 s_nSwarmMsgBusPopLock=%d",s_nSwarmMsgBusLock);
  	  	DEBUG(debugMsg);
  	  	s_nSwarmMsgBusLock++;
  		if(1==s_nSwarmMsgBusLock)
  		{ 
		  unsigned long nTmpTail;
		  if(s_nHeadIdx == s_nTailIdx){
		  	s_nSwarmMsgBusLock--;
		    return msg;
		  }
		  nTmpTail= (s_nTailIdx + 1) & SWARM_MSG_BUS_MASK;
		  s_nTailIdx=nTmpTail;
		  struct SWARM_MSG tempMsg =s_queue[nTmpTail]; 
		  s_nSwarmMsgBusLock--;
		  return tempMsg;
  		}
  		s_nSwarmMsgBusLock--;
  	  }
  	  else
  	  {
  	  	sprintf(debugMsg, "lock is not 0 s_nSwarmMsgBusPopLock=%d",s_nSwarmMsgBusLock);
  	  	DEBUG(debugMsg);
  	  	if(isInterruptCtx)
  	  		return msg;
  	  }
  }
}
