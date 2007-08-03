#include <avr/io.h>
#include <uart.h>
#include <spu.h>
#include <timer0.h>
#include <swarm_messaging.h>
#include <xbee.h>
#include <gps.h>
#include <packet_type.h>
#include <string.h>

#define MAX_SPU_MSG_QUEUE_SIZE 16
#define SPU_Q_MASK (MAX_SPU_MSG_QUEUE_SIZE -1)
#if (MAX_SPU_MSG_QUEUE_SIZE & SPU_Q_MASK)
     #error SPU queue size not a power of 2
#endif

//need not be volatile since we will access it only from the main() loop
volatile static struct SWARM_MSG s_spuDataQueue[MAX_SPU_MSG_QUEUE_SIZE];
volatile static unsigned long s_spuDataQueueHeadIdx=0;
volatile static unsigned long s_spuDataQueueTailIdx=0;

static struct SWARM_MSG s_lastGPGGAMsg;

/*
 * This call will block if Q is full
 */
void pushSpuDataQ(struct SWARM_MSG msg)
{
  unsigned long nTmpHead;
  nTmpHead = ( s_spuDataQueueHeadIdx + 1) & SWARM_Q_MASK;
  while(nTmpHead == s_spuDataQueueTailIdx)
    ;
  s_spuDataQueue[nTmpHead]=msg;
  s_spuDataQueueHeadIdx=nTmpHead;
}

/*
 * This call will return anull message is Q is empty
 */
struct SWARM_MSG popSpuDataQ(void)
{
  unsigned long nTmpTail;
  if(s_spuDataQueueHeadIdx == s_spuDataQueueTailIdx)
    {
      //queue is empty return null msg
      struct SWARM_MSG nullMsg;
      nullMsg.swarm_msg_type=eLinkNullMsg;
      return nullMsg;
    }
  nTmpTail= (s_spuDataQueueTailIdx + 1) & SWARM_Q_MASK;
  s_spuDataQueueTailIdx=nTmpTail;
  return s_spuDataQueue[nTmpTail];
}

void lightLedPortB6(void)
{
  PORTB = PORTB ^ (1<<PB6);
}

void lightLedPortB7(void)
{
  PORTB = PORTB ^ (1<<PB7);
}


int main(void)
{
  DDRB = 0xff;
  PORTB = 0xff;
  
  //initXbeeModule(lightLedPortB6, sendDebugMsg);
  //initGpsModule(lightLedPortB7, sendDebugMsg);
  //initSpuModule(lightLedPortB7, sendDebugMsg);
  //sendDebugMsg("init ");
  //
  uart_init(handleXbeeSerial,
    	    handleSpuSerial,
    	    handleGpsSerial);
  lightLedPortB6();
  sei();
  
  while(1){

    struct SWARM_MSG msg = popQ();
    if(msg.swarm_msg_type == eLinkMotorControl){
      //send to spu
      pushSpuDataQ(msg);
    }
    else if(msg.swarm_msg_type == eLinkNMEA){
  
      s_lastGPGGAMsg=msg;
    }
    else if(msg.swarm_msg_type == eLinkSpuMsg){
      //poor man's strcmp
      if(msg.swarm_msg_payload[0] == '$' &&
	msg.swarm_msg_payload[1] == 'A' &&
	 msg.swarm_msg_payload[2] == 'g' &&
	 msg.swarm_msg_payload[3] == '*')
	{
	  sendSpuMsg(s_lastGPGGAMsg.swarm_msg_payload);
	}
      else if(msg.swarm_msg_payload[0] == '$' &&
	msg.swarm_msg_payload[1] == 'A' &&
	 msg.swarm_msg_payload[2] == 'x' &&
	 msg.swarm_msg_payload[3] == '*')
	{
	  //drain q
	  while(1)
	    {
	      struct SWARM_MSG spuMsg = popSpuDataQ();
	      if(spuMsg.swarm_msg_type == eLinkNullMsg)
		break;
	      else
		sendSpuMsg(spuMsg.swarm_msg_payload);
	    }
	}
    }
  } 
  //
  return 0;
}
 
