#ifndef SWARM_QUEUES_H_
#define SWARM_QUEUES_H_

#include "swarm_common_defines.h"

#define MAX_SPU_MSG_QUEUE_SIZE 1024
#define SPU_Q_MASK (MAX_SPU_MSG_QUEUE_SIZE -1)
#if (MAX_SPU_MSG_QUEUE_SIZE & SPU_Q_MASK)
     #error SPU queue size not a power of 2
#endif

#define MAX_XBEE_MSG_QUEUE_SIZE 256
#define XBEE_Q_MASK (MAX_XBEE_MSG_QUEUE_SIZE -1)
#if (MAX_XBEE_MSG_QUEUE_SIZE & XBEE_Q_MASK)
     #error XBEE queue size not a power of 2
#endif

#define MAX_SWARM_MSG_BUS_SIZE 16
#define SWARM_MSG_BUS_MASK (MAX_SWARM_MSG_BUS_SIZE -1)
#if (MAX_SWARM_MSG_BUS_SIZE & SWARM_MSG_BUS_MASK)
     #error SWARM queue size not a power of 2
#endif

volatile static char s_spuDataQueue[MAX_SPU_MSG_QUEUE_SIZE];
volatile static unsigned long s_spuDataQueueHeadIdx=0;
volatile static unsigned long s_spuDataQueueTailIdx=0;

/*
 * This call will block if Q is full
 */
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

/*
 * This call will return null is Q is empty
 */
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

volatile static char s_xbeeDataQueue[MAX_XBEE_MSG_QUEUE_SIZE];
volatile static unsigned long s_xbeeDataQueueHeadIdx=0;
volatile static unsigned long s_xbeeDataQueueTailIdx=0;

/*
 * This call will return null is Q is empty
 */
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

/*
 * This call will block if Q is full
 */
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


volatile static struct SWARM_MSG s_queue[MAX_SWARM_MSG_BUS_SIZE];
volatile static unsigned long s_nHeadIdx=0;
volatile static unsigned long s_nTailIdx=0;

void pushSwarmMsgBus(struct SWARM_MSG msg)
{
  unsigned long nTmpHead;
  nTmpHead = ( s_nHeadIdx + 1) & SWARM_MSG_BUS_MASK;
  /*
  while(nTmpHead == s_nTailIdx)
    ;
  */
  if(nTmpHead == s_nTailIdx)
    return;
  s_queue[nTmpHead]=msg;
  s_nHeadIdx=nTmpHead;
}


struct SWARM_MSG popSwarmMsgBus(void)
{
  unsigned long nTmpTail;
  while(s_nHeadIdx == s_nTailIdx)
    ;
  nTmpTail= (s_nTailIdx + 1) & SWARM_MSG_BUS_MASK;
  s_nTailIdx=nTmpTail;
  return s_queue[nTmpTail];
}
#endif /*SWARM_QUEUES_H_*/