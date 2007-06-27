
#include <stdlib.h>
#include <swarm_messaging.h>
#include "uart.h"
#include <packet_type.h>
#include "spu.h"

static int spu_state= eSpuStateStart;
static int spu_state_byte_num=0;
static long spu_exp_payload_len=0;
static struct SWARM_MSG volatile spu_recv_msg;
static void (*  _handleSpuSerialRxStartCallback)(void)=0;
static void (*  _handleSpuSwarmMsgStartCallback)(void)=0;

void setupSpuCallbacks(void (*  handleSpuSerialRxStartCallback)(void),
		       void (*  handleSpuSwarmMsgStartCallback)(void))
{
  _handleSpuSerialRxStartCallback=handleSpuSerialRxStartCallback;
  _handleSpuSwarmMsgStartCallback=handleSpuSwarmMsgStartCallback;
}

static void initSpuVars(void)
{
 spu_state= eSpuStateStart;
 spu_state_byte_num=0;
 spu_exp_payload_len=0;
}

static void handleSpuSwarmMsg(struct SWARM_MSG msg)
{
  if(0 != _handleSpuSwarmMsgStartCallback)
    (*_handleSpuSwarmMsgStartCallback)();
  if(msg.swarm_msg_type == eLinkLoopback)
    {
      struct SWARM_ACK ack;
      ack.swarm_msg_type=eLinkAck;
      ack.swarm_resp_code=eSwarmRespOk;
      unsigned char ack_bytes[2];
      ack_bytes[0] = ack.swarm_msg_type;
      ack_bytes[1] = ack.swarm_resp_code;
      sendSpuMsg(ack_bytes);
    }
}

void handleSpuSerialRx(unsigned char c, int isError)
{
  switch(c){
  case eSpuStateStart :
    if(0 != _handleSpuSerialRxStartCallback)
      (*_handleSpuSerialRxStartCallback)();
    spu_recv_msg.swarm_msg_type=c;
    spu_state = eSpuStateMsgType;
  case eSpuStateMsgType :
    spu_state=eSpuStateMsgLength;//empty transition
    break;
  case eSpuStateMsgLength:
    if(spu_state_byte_num < 2)
      {
	spu_recv_msg.swarm_msg_length[spu_state_byte_num++]=c;
	break;
      }
    else if(2== spu_state_byte_num)
      {
	spu_exp_payload_len=atol((char*)spu_recv_msg.swarm_msg_length);
	//init and fall through
	spu_state_byte_num=0;
	spu_state=eSpuStateMsgPayload;
      }
  case eSpuStateMsgPayload:
    if(spu_state_byte_num < spu_exp_payload_len)
      {
	spu_recv_msg.swarm_msg_payload[spu_state_byte_num++]=c;
      }
    if((spu_state_byte_num+1)== spu_exp_payload_len)
      {
	//handle msh here
	handleSpuSwarmMsg(spu_recv_msg);
      }
      break;
  default :
    //init
    initSpuVars();
  } 
}


