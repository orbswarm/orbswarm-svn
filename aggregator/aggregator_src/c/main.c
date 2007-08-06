#include <avr/io.h>
#include <uart.h>
#include <spu.h>
#include <timer0.h>
#include <swarm_messaging.h>
#include <xbee.h>
#include <gps.h>
#include <packet_type.h>
#include <string.h>

#define MAX_SPU_MSG_QUEUE_SIZE 512
#define SPU_Q_MASK (MAX_SPU_MSG_QUEUE_SIZE -1)
#if (MAX_SPU_MSG_QUEUE_SIZE & SPU_Q_MASK)
     #error SPU queue size not a power of 2
#endif

volatile static unsigned char s_spuDataQueue[MAX_SPU_MSG_QUEUE_SIZE];
volatile static unsigned long s_spuDataQueueHeadIdx=0;
volatile static unsigned long s_spuDataQueueTailIdx=0;

/*
 * This call will block if Q is full
 */
void pushSpuDataQ(const unsigned char* msg)
{
  while(*msg)
    {
      unsigned long nTmpHead;
      nTmpHead = ( s_spuDataQueueHeadIdx + 1) & SWARM_Q_MASK;
      while(nTmpHead == s_spuDataQueueTailIdx)
	;
      s_spuDataQueue[nTmpHead]=*msg++;
      s_spuDataQueueHeadIdx=nTmpHead;
    }
}

/*
 * This call will return null is Q is empty
 */
unsigned char popSpuDataQ(void)
{
  unsigned long nTmpTail;
  if(s_spuDataQueueHeadIdx == s_spuDataQueueTailIdx)
    {
       return 0;
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


void setGpsMode(void)
{
  volatile unsigned char ack[MAX_GPS_PACKET_LENGTH];
  sendDebugMsg("\r\nReading init params if any");
  loopTimer0(2000);
  getPmtkMsg(ack);
  sendDebugMsg(";ack=");
  sendDebugMsg(ack);

  sendDebugMsg("\r\nsending 313");
  sendGPSAMsg((unsigned char*)"$PMTK313,1*2E\r\n");
  loopTimer0(2000);
  getPmtkMsg(ack);
  sendDebugMsg(";ack=");
  sendDebugMsg(ack);

  sendDebugMsg("\r\nsending 301");
  sendGPSAMsg((unsigned char*)"$PMTK301,2*2D\r\n");
  loopTimer0(2000);
  getPmtkMsg(ack);
  sendDebugMsg(";ack=");
  sendDebugMsg(ack);

  sendDebugMsg("\r\nsending query");
  sendGPSAMsg((unsigned char*) "$PMTK401*37\r\n");
  loopTimer0(2000);
  getPmtkMsg(ack);
  sendDebugMsg(";ack=");
  sendDebugMsg(ack);

}


int main(void)
{
  DDRB = 0xff;
  PORTB = 0xff;
  unsigned char gps_msg_buffer[MAX_GPS_PACKET_LENGTH];
  
  //initXbeeModule(lightLedPortB6, sendDebugMsg);
  initGpsModule(lightLedPortB7, sendDebugMsg);
  //initSpuModule(lightLedPortB7, sendDebugMsg);
  //sendDebugMsg("init ");
  //
  uart_init(handleXbeeSerial,
    	    handleSpuSerial,
    	    handleGpsSerial);
  sendDebugMsg((unsigned char*)"\ninit ");
  setGpsMode();
  lightLedPortB6();
  sei();
  
  while(1){
    struct SWARM_MSG msg = popQ();
    if(msg.swarm_msg_type == eLinkMotorControl){
      //send to spu
      pushSpuDataQ(msg.swarm_msg_payload);
    }
    else if(msg.swarm_msg_type == eLinkSpuMsg){
      //poor man's strcmp
      if(msg.swarm_msg_payload[0] == '$' &&
	msg.swarm_msg_payload[1] == 'A' &&
	 msg.swarm_msg_payload[2] == 'g' &&
	 msg.swarm_msg_payload[3] == '*')
	{
	  lightLedPortB7();
	  getGpsGpggaMsg(gps_msg_buffer);
	  sendSpuMsg(gps_msg_buffer);
	  getGpsGpvtgMsg(gps_msg_buffer);
	  sendSpuMsg(gps_msg_buffer);
	  sendSpuMsg((unsigned char*)"!");
	}
      else if(msg.swarm_msg_payload[0] == '$' &&
	msg.swarm_msg_payload[1] == 'A' &&
	 msg.swarm_msg_payload[2] == 'x' &&
	 msg.swarm_msg_payload[3] == '*')
	{
	  //drain q
	  unsigned char buffer[64];
	  long nLen=0;
	  while(1){
	    unsigned char c = popSpuDataQ();
	    if(0 ==c){
	      //DONE
	      buffer[++nLen]='\0';
	      sendSpuMsg(buffer);
	      break;
	    }
	    else{
	      buffer[nLen++]=c;
	      if(63 == nLen){
		//write out full buffer
		buffer[nLen]='\0';
		sendSpuMsg(buffer);
		nLen=0;
	      }
	    }//end else
	  }//end inner while(1)
	  sendSpuMsg((unsigned char*)"!");
	}
    }
  } 
  //
  return 0;
}
 
