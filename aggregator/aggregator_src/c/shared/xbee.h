#include <stdlib.h>
#include "uart.h"
#include <swarm_messaging.h>

/*
  XBee message definition and common code to be used 
  by both SPU and aggreagtor.
*/

/*
  XBee message formats for 16 bit addresses
*/

#define XBEE_FRAME_ID 0x7e

struct XBEE_RX_PACKET {
  //unsigned char start_byte; //value=0x7e
  unsigned char packet_length[2];
  unsigned char api_identifier; //value=0x80
  unsigned char source_address[2];
  unsigned char rssi_indicator;
  //unsigned char broadcast_option;
  unsigned char payload[100];
  unsigned char checksum;
} ;

struct XBEE_TX_PACKET {
  //unsigned char start_byte; //value=0x7e
  unsigned char packet_length[2];
  unsigned char api_identifier; //value=0x00
  //unsigned char frame_id;
  unsigned char dest_address[2];
  //unsigned char ack_broadcast_option;
  unsigned char payload[100];
  unsigned char checksum;
}  ;



enum ETxStates {
  eTxStateStart=0x00,
  eTxStateLength,
  eTxStateAPIId,
  eTxStateDestAddr,
  eTxStatePayload,
  eTxStateChksum
};

//static int tx_state=-1;
//static int tx_state_byte_num=-1;

enum ERxStates {
  eRxStateStart=0x00,
  eRxStateLength,
  eRxStateAPIId,
  eRxStateSrcAddr,
  eRxStateRssi,
  eRxStateBroadcastOpt,
  eRxStatePayload,
  eRxStateChksum
};


/*
struct XBEE_RX_PACKET parse_packet(unsigned char* raw_packet)
{
  struct XBEE_RX_PACKET p;
  p.start_byte = raw_packet[0];
  p.packet_length[0] = raw_packet[1];
  p.packet_length[1] = raw_packet[2];
  char len[2];
  len[0]= raw_packet[1];
  len[1]= raw_packet[2];
  long nLen = atol(len);
  long nLenIdx = 0;
  p.api_identifier = raw_packet[3];
  nLenIdx++;
  p.source_address[0] = raw_packet[4];
  p.source_address[1] = raw_packet[5];
  nLenIdx = nLenIdx + 2;
  p.rssi_indicator = raw_packet[6];
  p.broadcast_option = raw_packet[7];
  nLenIdx = nLenIdx + 2;
  for( ; nLenIdx<= nLen; nLenIdx++)
    {
      p.payload[nLenIdx - 5] = raw_packet[nLenIdx];
    }
  p.checksum = raw_packet[nLenIdx];
  return p;
}

void build_packet(struct XBEE_TX_PACKET tx_packet, unsigned char* raw_packet); 
*/
