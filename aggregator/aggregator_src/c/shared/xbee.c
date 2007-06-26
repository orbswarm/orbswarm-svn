#include "xbee.h"


static int rx_state=eRxStateStart;
static int rx_state_byte_num=0;
static long rx_exp_payload_len=0;
static struct XBEE_RX_PACKET rx_packet;

void initRxBuffers()
{
  rx_state=eRxStateStart;
  rx_state_byte_num=0;
  rx_exp_payload_len=0;
}

void handleXBeeRx(char c, int isError)
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
	rx_packet.payload[rx_state_byte_num] = c;
	break;
      }
    else
      {
	rx_state_byte_num=0;
	rx_state=eRxStateChksum;
      }
  case eRxStateChksum:
    rx_packet.checksum=c;
  default:
    //initialise stuff here
    initRxBuffers();
  }
}
