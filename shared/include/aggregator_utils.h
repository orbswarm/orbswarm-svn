#include <swarm_messaging.h>
#include <xbee.h>
#include <gps.h>
#include <packet_type.h>
//#include <string.h>
#include <swarm_common_defines.h>

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
  while(*msg && '*' != *msg )
    {
 
      unsigned long nTmpHead;
      nTmpHead = ( s_xbeeDataQueueHeadIdx + 1) & XBEE_Q_MASK;
      if(nTmpHead == s_xbeeDataQueueTailIdx)
	return;
      s_xbeeDataQueue[nTmpHead]=*msg++;
      s_xbeeDataQueueHeadIdx=nTmpHead;
    }

}


