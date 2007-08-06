#include "include/swarm_messaging.h"

/**
 * Circular queue implementation borrowed from  ../orb_esc/src/UART.c by Pete
 */

//Circular Q to hold all the messages
volatile static struct SWARM_MSG s_queue[MAX_SWARM_MSG_QUEUE_SIZE];
volatile static unsigned long s_nHeadIdx=0;
volatile static unsigned long s_nTailIdx=0;

void pushQ(struct SWARM_MSG msg)
{
  unsigned long nTmpHead;
  nTmpHead = ( s_nHeadIdx + 1) & SWARM_Q_MASK;
  /*
  while(nTmpHead == s_nTailIdx)
    ;
  */
  if(nTmpHead == s_nTailIdx)
    return;
  s_queue[nTmpHead]=msg;
  s_nHeadIdx=nTmpHead;
}


struct SWARM_MSG popQ(void)
{
  unsigned long nTmpTail;
  while(s_nHeadIdx == s_nTailIdx)
    ;
  nTmpTail= (s_nTailIdx + 1) & SWARM_Q_MASK;
  s_nTailIdx=nTmpTail;
  return s_queue[nTmpTail];
}
