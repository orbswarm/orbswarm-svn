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

volatile static struct SWARM_MSG s_swarmMsgBus[MAX_SWARM_MSG_BUS_SIZE];
volatile static unsigned long s_nSwarmMsgBusHeadIdx=0;
volatile static unsigned long s_nSwarmMsgBusTailIdx=0;

static void (* volatile s_debug)(const char*) =0;
volatile static long s_nSwarmMsgBusRecordSeqNo =0;

static int isDebug(void)
{
	if(0 == s_debug)
		return 0;
	else
		return 1;
}

void initSwarmQueues(void (*debug)(const char* c))
{
	s_debug=debug;
}

static void DEBUG(const char* s)
{
	if(s_debug != 0)
		(*s_debug)(s);
}

void pushSpuDataQ(const char* msg, int isInterruptCtx)
{
  while(*msg)
    {
      unsigned long nTmpHead;
      /*
       * It's ok to have the critical section inside the while loop
       * because only the main loop will be calling this method. If
       * that were not the case we would need to have it outside the while 
       * loop otherwise the messages would get jumbled up. 
       */ 
      ////////start critical section
      if(!isInterruptCtx)
      	cli();
      nTmpHead = ( s_spuDataQueueHeadIdx + 1) & SPU_Q_MASK;
      if (nTmpHead == s_spuDataQueueTailIdx)
      {
		//first pop oldest entry
		unsigned long nTmpTail;
	  	nTmpTail= (s_spuDataQueueTailIdx + 1) & SPU_Q_MASK;
  		s_spuDataQueueTailIdx=nTmpTail;
  		//the discarded element is s_spuDataQueue[nTmpTail];
  		s_spuDataQueue[nTmpHead]=*msg;
      	s_spuDataQueueHeadIdx=nTmpHead;
      }
      else
      {
      	s_spuDataQueue[nTmpHead]=*msg;
      	s_spuDataQueueHeadIdx=nTmpHead;
      }
      if(!isInterruptCtx)
      	sei();
      ////////end critical section
      msg++;
    }
  
}

char popSpuDataQ(int isInterruptCtx)
{
  unsigned long nTmpTail;
  char retChar=0;
  ////////start critical section
  if(!isInterruptCtx)
   	cli();
  if(s_spuDataQueueHeadIdx != s_spuDataQueueTailIdx)
    {
  		nTmpTail= (s_spuDataQueueTailIdx + 1) & SPU_Q_MASK;
  		s_spuDataQueueTailIdx=nTmpTail;
  		retChar=s_spuDataQueue[nTmpTail];
    }
    else
    {
    	if(isDebug())
    		DEBUG("SPU Q is empty");
    }
  if(!isInterruptCtx)
   	sei();
  ////////end critical section
  return retChar;
}

char popXbeeDataQ(int isInterruptCtx)
{
  unsigned long nTmpTail;
  char retChar=0;
  ////////start critical section
  if(!isInterruptCtx)
   	cli();
  if(s_xbeeDataQueueHeadIdx != s_xbeeDataQueueTailIdx)
    {
    	nTmpTail= (s_xbeeDataQueueTailIdx + 1) & XBEE_Q_MASK;
  		s_xbeeDataQueueTailIdx=nTmpTail;
  		retChar= s_xbeeDataQueue[nTmpTail];
    }
  else
  {
   	if(isDebug())
   		DEBUG("XBEE Q is empty");
  }
  if(!isInterruptCtx)
   	sei();
  ////////end critical section
  return retChar;
}

void pushXbeeDataQ(const char* msg, int isInterruptCtx)
{
  while(*msg)
    {
      unsigned long nTmpHead;
      /*
       * It's ok to have the critical section inside the while loop
       * because only the main loop will be calling this method. If
       * that were not the case we would need to have it outside the while 
       * loop otherwise the messages would get jumbled up. 
       */ 
      ////////start critical section
      if(!isInterruptCtx)
      	cli();
      nTmpHead = ( s_xbeeDataQueueHeadIdx + 1) & XBEE_Q_MASK;
      if(nTmpHead == s_xbeeDataQueueTailIdx)
      {
      	if(isDebug())
      		DEBUG("XBEE Q is full");
      	//first pop oldest entry
		unsigned long nTmpTail;
		nTmpTail= (s_xbeeDataQueueTailIdx + 1) & XBEE_Q_MASK;
  		s_xbeeDataQueueTailIdx=nTmpTail;
  		//discarded char is s_xbeeDataQueue[nTmpTail];
  		s_xbeeDataQueue[nTmpHead]=*msg;
      	s_xbeeDataQueueHeadIdx=nTmpHead;
      }
      else
	  {	
      	s_xbeeDataQueue[nTmpHead]=*msg;
      	s_xbeeDataQueueHeadIdx=nTmpHead;
	  }
      if(!isInterruptCtx)
      	sei();
      ////////end critical section
      msg++;
    }

}

void pushSwarmMsgBus(struct SWARM_MSG msg, int isInterruptCtx)
{
	if(isDebug())
  		DEBUG("pushSwarmMsgBus:START");
  	unsigned long nTmpHead;
  	char debugMsg[1024];
  	//////critical section start
  	if(!isInterruptCtx)
  		cli();
  	nTmpHead = ( s_nSwarmMsgBusHeadIdx + 1) & SWARM_MSG_BUS_MASK;
  	if(nTmpHead != s_nSwarmMsgBusTailIdx)
	{
		if(isDebug())
		{
	  		sprintf(debugMsg, "\r\n old head idx=%ld, new head idx=%ld, tail idx=%ld", 
	  			s_nSwarmMsgBusHeadIdx,nTmpHead, s_nSwarmMsgBusTailIdx);
	  		DEBUG(debugMsg);
		}

  		s_swarmMsgBus[nTmpHead]=msg;  		
  		s_nSwarmMsgBusHeadIdx=nTmpHead;
	}
	else
	{
		if(isDebug())
			DEBUG("\r\n PUSH Q FULL");
		//first pop oldest message
		unsigned long nTmpTail;
  		nTmpTail= (s_nSwarmMsgBusTailIdx + 1) & SWARM_MSG_BUS_MASK; 
  		s_nSwarmMsgBusTailIdx=nTmpTail;
  		//discarded message is s_queue[nTmpTail];
  		//now push new msg
  		s_swarmMsgBus[nTmpHead]=msg;
  		s_nSwarmMsgBusHeadIdx=nTmpHead;
	}
	s_nSwarmMsgBusRecordSeqNo++;
  	if(!isInterruptCtx)
		sei();
	//////critical section end
	return;
}


struct SWARM_MSG popSwarmMsgBus(int isInterruptCtx)
{
	char debugMsg[1024];
	if(isDebug())
  		DEBUG("popSwarmMsgBus:START");
  	int nTries=0;
  	while(1)
  	{
  		struct SWARM_MSG msg;
  		msg.swarm_msg_type=eLinkNullMsg; 
  		unsigned long nTmpTail;
  	
	  	//////critical section start
	  	if(!isInterruptCtx)
	  		cli();
	  	long nRecordSeq=s_nSwarmMsgBusRecordSeqNo;
	  	unsigned long nSwarmMsgBusTailIdx=s_nSwarmMsgBusTailIdx;
	  	unsigned long nSwarmMsgBusHeadIdx=s_nSwarmMsgBusHeadIdx;
	  	if(!isInterruptCtx)	
	  		sei();
	  	//////critical section end 
	  	
	  	nTries++;
	  	if(isDebug())
	  	{
			sprintf(debugMsg, "\r\n tail_idx=%ld, head_ix=%ld", nSwarmMsgBusTailIdx, nSwarmMsgBusHeadIdx);
			DEBUG(debugMsg);
	  	}		
		if(nSwarmMsgBusHeadIdx != nSwarmMsgBusTailIdx)
		{
	  		nTmpTail= (nSwarmMsgBusTailIdx + 1) & SWARM_MSG_BUS_MASK;
	  		
	  		if(isDebug())
	  		{
		  		sprintf(debugMsg, "\r\n old tail idx=%ld, new tail idx=%ld"
		  		",head idx=%ld", 
		  			nSwarmMsgBusTailIdx,nTmpTail, nSwarmMsgBusHeadIdx);
		  		DEBUG(debugMsg);
	  		}
	  		
	  		nSwarmMsgBusTailIdx=nTmpTail;
	  		msg= s_swarmMsgBus[nTmpTail];
		}
		else//else msg remains in it's initialized state i.e null
		{
			if(isDebug())
				DEBUG("\r\npop Q is empty");
		}
		
		//////critical section start
		if(!isInterruptCtx)
			cli();
		if(s_nSwarmMsgBusRecordSeqNo == nRecordSeq)
		{
	  		s_nSwarmMsgBusTailIdx=nSwarmMsgBusTailIdx;
	  		s_nSwarmMsgBusHeadIdx=nSwarmMsgBusHeadIdx;
			s_nSwarmMsgBusRecordSeqNo++;
			if(!isInterruptCtx)
				sei();
			//////critical section end
			return msg;
		}
		else
		{
			if(isDebug())
			{
				sprintf(debugMsg, "\r\ndiscarding stale message, try %d", nTries);
				DEBUG(debugMsg);
			}
		} 
		
		if(!isInterruptCtx)
			sei();//and continue to the while loop				
  	}
}
