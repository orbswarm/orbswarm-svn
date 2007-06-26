

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

//void initRxBuffers(void); 

void handleXBeeRx(char c, int isError);

