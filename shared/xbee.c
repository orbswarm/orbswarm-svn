#include <stdlib.h>
#include <swarm_messaging.h>
#include "include/xbee.h"
#include <packet_type.h>

volatile static int rx_state=eXbeeStraightSerialRxInit;
volatile static int rx_is_error=0;
volatile static unsigned long rx_state_byte_num=0;
volatile static struct SWARM_MSG rx_packet;
static void (*_debugCallback)(void)=0;
static void (*_debug)(const char* debugMsg)=0;
//static long rx_exp_payload_len=0;
//volatile static struct XBEE_RX_PACKET rx_packet;

void initXbeeModule(void (*debugCallback)(void),
		    void (*debug)(const char*) )
{
  _debugCallback=debugCallback;
  _debug=debug;
}

static void debugCallback(void)
{
  if(0 != _debugCallback)
    (*_debugCallback)();
}

static void debug(const char* debugMsg)
{
  if(0 != _debug)
    (*_debug)(debugMsg);
}

static void initXbeeMsgStart(unsigned char c)
{
  rx_state=eXbeeStraightSerialRxStartMsg;
  rx_state_byte_num=0;
  rx_is_error=0;
  rx_packet.swarm_msg_type=eLinkMotorControl;
  rx_packet.swarm_msg_payload[rx_state_byte_num]=c;
}

void handleXbeeSerial(unsigned char c, int isError)
{
  debugCallback();
  debug("in handle");
  //if it's an error flag and discard till the start of the next message
  if(isError){
    //debugCallback();
    rx_is_error=isError;
    return;
  }
  switch(rx_state){
  case eXbeeStraightSerialRxInit:
    if('$'==c){
      //debugCallback();
      initXbeeMsgStart(c);
    }
    break;
  case eXbeeStraightSerialRxStartMsg:
    if('$' ==c){
      //empty message
      initXbeeMsgStart(c);
    }
    else{
      rx_state=eXbeeStraightSerialRxPayload;
      rx_state_byte_num++;
      rx_packet.swarm_msg_payload[rx_state_byte_num]=c;
    }
    break;
  case eXbeeStraightSerialRxPayload:
     if('$'==c)
       {
	 //if not error first dispatch old message
	 if(!isError){
	   rx_packet.swarm_msg_length[0] = 
	     (unsigned char)(rx_state_byte_num>>8);
	   rx_packet.swarm_msg_length[1] = (unsigned char)rx_state_byte_num;
	   rx_packet.swarm_msg_payload[rx_state_byte_num+1]='\0';
	   pushQ(rx_packet);
	 }
	 initXbeeMsgStart(c);
       }
     else
       {
	 //check for max size
	 rx_state_byte_num++;
	 if(rx_state_byte_num <= MAX_SWARM_MSG_LENGTH )
	   rx_packet.swarm_msg_payload[rx_state_byte_num]=c;
	 //else ignore till the end
       }
     break;
     //default:
    //debugCallback();
  }
  //debugCallback();
}

void handleXbeeSerialBuf(const char* buf, long nNumBytes)
{
  for(int i=0; i < nNumBytes; i++)
    {
      handleXbeeSerial((unsigned char)buf[i], 0);
    }
}
/*
static void initRxBuffers(void)
{
  rx_state=eRxStateStart;
  rx_state_byte_num=0;
  rx_exp_payload_len=0;
}

static void handleXBeePacket(struct XBEE_RX_PACKET packet)
{
    //TO DO: handle the actual msg(queue it) and ack
}

void handleXBeeSerialRx(char c, int isError)
{
  switch(c){
  case eRxStateStart : 
    if(XBEE_FRAME_ID == c)
      rx_state = eRxStateLength;
    //else there is nothing for me to do. just wait for frame start
    break;
  case eRxStateLength :
    if(rx_state_byte_num < 2)
      {
	rx_packet.packet_length[rx_state_byte_num++]=c;
	break;
      }
    else if(2 == rx_state_byte_num)
      {
	//init and fall through
	rx_state_byte_num=0;
	rx_state=eRxStateAPIId;
      }
  case eRxStateAPIId :
    if(0 == rx_state_byte_num)
      {
	rx_packet.api_identifier=c;
	rx_state_byte_num++;
	break;
      }
    else
      {
	//init and fall through
	rx_state_byte_num=0;
	rx_state=eRxStateSrcAddr;
      }
  case eRxStateSrcAddr:
    if(rx_state_byte_num < 2)
      {
	rx_packet.source_address[rx_state_byte_num++] = c;
	break;
      }
    else
      {
	//change state, init and fall thru
	rx_state=eRxStateRssi;
	rx_state_byte_num=0;
      }
  case eRxStateRssi:
    if(0 ==  rx_state_byte_num)
      {
	rx_packet.rssi_indicator=c;
	rx_state_byte_num++;
	break;
      }
    else
      {
	//change state, init and fall thru
	rx_state=eRxStateBroadcastOpt;
	rx_state_byte_num=0;
      }
  case eRxStateBroadcastOpt:
    if(0 == rx_state_byte_num)
      {
	rx_state_byte_num++;
	break;
      }
    else
      {
	rx_exp_payload_len = atol((char*)rx_packet.packet_length);
	//account for the bytes we have already read
	rx_exp_payload_len = rx_exp_payload_len -5;
	//change state, init and fall thru
	rx_state=eRxStatePayload;
	rx_state_byte_num=0;
      }
  case eRxStatePayload:
    if(rx_state_byte_num< rx_exp_payload_len)
      {
	rx_packet.payload[rx_state_byte_num++] = c;
	break;
      }
    else
      {
	rx_state_byte_num=0;
	rx_state=eRxStateChksum;
      }
  case eRxStateChksum:
    rx_packet.checksum=c;
    //fall through to init
    handleXBeePacket(rx_packet);
  default:
    //initialise stuff here
    initRxBuffers();
  }
}
*/
