#include <string.h>
#include <stdio.h>
#include <avr/interrupt.h>
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
  	//DEBUG("pushSwarmMsgBus:START");
  	unsigned long nTmpHead;
  	//////critical section start
  	if(!isInterruptCtx)
  		cli();
  	nTmpHead = ( s_nHeadIdx + 1) & SWARM_MSG_BUS_MASK;
  	if(nTmpHead != s_nTailIdx)
	{
  		s_queue[nTmpHead]=msg;
  		
  		char debugMsg[1024];
  		sprintf(debugMsg, "\r\n old head idx=%ld, new head idx=%ld, tail idx=%ld", 
  			s_nHeadIdx,nTmpHead, s_nTailIdx);
  		DEBUG(debugMsg);
  		
  		s_nHeadIdx=nTmpHead;
	}
	else
	{
		DEBUG("\r\n PUSH Q FULL");
		//first pop oldest message
		unsigned long nTmpTail;
  		nTmpTail= (s_nTailIdx + 1) & SWARM_MSG_BUS_MASK; 
  		s_nTailIdx=nTmpTail;
  		//discarded message is s_queue[nTmpTail];
  		//now push new msg
  		s_queue[nTmpHead]=msg;
  		s_nHeadIdx=nTmpHead;
	}
  	if(!isInterruptCtx)
		sei();
	//////critical section end
	return;
}


struct SWARM_MSG popSwarmMsgBus(int isInterruptCtx)
{
  	DEBUG("popSwarmMsgBus:START");
  	struct SWARM_MSG msg;
  	msg.swarm_msg_type=eLinkNullMsg; 
  	unsigned long nTmpTail;
  	//////critical section start
  	if(!isInterruptCtx)
  		cli();
  
	char debugMsg[1024];
	sprintf(debugMsg, "\r\n tail_idx=%ld, head_ix=%ld", s_nTailIdx, s_nHeadIdx);
	DEBUG(debugMsg);		
	if(s_nHeadIdx != s_nTailIdx)
	{
  		nTmpTail= (s_nTailIdx + 1) & SWARM_MSG_BUS_MASK;
  		
  		sprintf(debugMsg, "\r\n old tail idx=%ld, new tail idx=%ld"
  		",head idx=%ld", 
  			s_nTailIdx,nTmpTail, s_nHeadIdx);
  		DEBUG(debugMsg);
  		
  		s_nTailIdx=nTmpTail;
  		msg= s_queue[nTmpTail];
	}
	else
		DEBUG("\r\npop Q is empty");
	//else msg remains in it's initialized state i.e null
	if(!isInterruptCtx)
		sei();
	//////critical section end
	return msg;
}
