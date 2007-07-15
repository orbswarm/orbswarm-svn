

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


enum EXbeeApiTxStates {
  eXbeeApiTxStateStart=0x00,
  eXbeeApiTxStateLength,
  eXbeeApiTxStateAPIId,
  eXbeeApiTxStateDestAddr,
  eXbeeApiTxStatePayload,
  eXbeeApiTxStateChksum
};

enum EXbeeApiRxStates {
  eXbeeApiRxStateStart=0x00,
  eXbeeApiRxStateLength,
  eXbeeApiRxStateAPIId,
  eXbeeApiRxStateSrcAddr,
  eXbeeApiRxStateRssi,
  eXbeeApiRxStateBroadcastOpt,
  eXbeeApiRxStatePayload,
  eXbeeApiRxStateChksum
};

enum EXbeeStraightSerialRxStates {
  eXbeeStraightSerialRxInit,
  eXbeeStraightSerialRxStartMsg,
  eXbeeStraightSerialRxPayload
};

//void initRxBuffers(void); 

//void handleXBeeRx(char c, int isError);

void  handleXbeeSerial(unsigned char c, int isError);

void initXbeeModule(void (*debugCallback)(void),
		    void (*debug)(const char*) );

