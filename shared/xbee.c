//#include <stdlib.h>
#include <swarm_messaging.h>
#include "include/xbee.h"
#include <packet_type.h>

volatile static int xbee_rx_state=eXbeeStraightSerialRxInit;
volatile static int xbee_rx_is_error=0;
volatile static unsigned long xbee_rx_state_byte_num=0;
volatile static struct SWARM_MSG xbee_rx_packet;
static void (*_debugCallback)(void)=0;
static void (*_debug)(const char* debugMsg)=0;
//static long xbee_rx_exp_payload_len=0;
//volatile static struct XBEE_RX_PACKET xbee_rx_packet;

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
  xbee_rx_state=eXbeeStraightSerialRxStartMsg;
  xbee_rx_state_byte_num=0;
  xbee_rx_is_error=0;
  xbee_rx_packet.swarm_msg_type=eLinkMotorControl;
  xbee_rx_packet.swarm_msg_payload[xbee_rx_state_byte_num]=c;
}

void handleXbeeSerial(unsigned char c, int isError)
{
  debugCallback();
  //debug("in handle");
  //if it's an error flag and discard till the start of the next message
  if(isError){
    //debugCallback();
    xbee_rx_is_error=isError;
    return;
  }
  switch(xbee_rx_state){
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
      xbee_rx_state=eXbeeStraightSerialRxPayload;
      xbee_rx_state_byte_num++;
      xbee_rx_packet.swarm_msg_payload[xbee_rx_state_byte_num]=c;
    }
    break;
  case eXbeeStraightSerialRxPayload:
     if('$'==c)
       {
	 //if not error first dispatch old message
	 if(!xbee_rx_is_error){
	   xbee_rx_packet.swarm_msg_length[0] = 
	     (unsigned char)(xbee_rx_state_byte_num>>8);
	   xbee_rx_packet.swarm_msg_length[1] = (unsigned char)xbee_rx_state_byte_num;
	   xbee_rx_packet.swarm_msg_payload[xbee_rx_state_byte_num+1]='\n';
	   xbee_rx_packet.swarm_msg_payload[xbee_rx_state_byte_num+2]='\0';
	   pushQ(xbee_rx_packet);
	 }
	 initXbeeMsgStart(c);
       }
     else
       {
	 //check for max size
	 xbee_rx_state_byte_num++;
	 if(xbee_rx_state_byte_num 
	    <= (MAX_SWARM_MSG_LENGTH-1)/*leave 1 byte for the new line */ )
	   xbee_rx_packet.swarm_msg_payload[xbee_rx_state_byte_num]=c;
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
  xbee_rx_state=eRxStateStart;
  xbee_rx_state_byte_num=0;
  xbee_rx_exp_payload_len=0;
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
      xbee_rx_state = eRxStateLength;
    //else there is nothing for me to do. just wait for frame start
    break;
  case eRxStateLength :
    if(xbee_rx_state_byte_num < 2)
      {
	xbee_rx_packet.packet_length[xbee_rx_state_byte_num++]=c;
	break;
      }
    else if(2 == xbee_rx_state_byte_num)
      {
	//init and fall through
	xbee_rx_state_byte_num=0;
	xbee_rx_state=eRxStateAPIId;
      }
  case eRxStateAPIId :
    if(0 == xbee_rx_state_byte_num)
      {
	xbee_rx_packet.api_identifier=c;
	xbee_rx_state_byte_num++;
	break;
      }
    else
      {
	//init and fall through
	xbee_rx_state_byte_num=0;
	xbee_rx_state=eRxStateSrcAddr;
      }
  case eRxStateSrcAddr:
    if(xbee_rx_state_byte_num < 2)
      {
	xbee_rx_packet.source_address[xbee_rx_state_byte_num++] = c;
	break;
      }
    else
      {
	//change state, init and fall thru
	xbee_rx_state=eRxStateRssi;
	xbee_rx_state_byte_num=0;
      }
  case eRxStateRssi:
    if(0 ==  xbee_rx_state_byte_num)
      {
	xbee_rx_packet.rssi_indicator=c;
	xbee_rx_state_byte_num++;
	break;
      }
    else
      {
	//change state, init and fall thru
	xbee_rx_state=eRxStateBroadcastOpt;
	xbee_rx_state_byte_num=0;
      }
  case eRxStateBroadcastOpt:
    if(0 == xbee_rx_state_byte_num)
      {
	xbee_rx_state_byte_num++;
	break;
      }
    else
      {
	xbee_rx_exp_payload_len = atol((char*)xbee_rx_packet.packet_length);
	//account for the bytes we have already read
	xbee_rx_exp_payload_len = xbee_rx_exp_payload_len -5;
	//change state, init and fall thru
	xbee_rx_state=eRxStatePayload;
	xbee_rx_state_byte_num=0;
      }
  case eRxStatePayload:
    if(xbee_rx_state_byte_num< xbee_rx_exp_payload_len)
      {
	xbee_rx_packet.payload[xbee_rx_state_byte_num++] = c;
	break;
      }
    else
      {
	xbee_rx_state_byte_num=0;
	xbee_rx_state=eRxStateChksum;
      }
  case eRxStateChksum:
    xbee_rx_packet.checksum=c;
    //fall through to init
    handleXBeePacket(xbee_rx_packet);
  default:
    //initialise stuff here
    initRxBuffers();
  }
}
*/
