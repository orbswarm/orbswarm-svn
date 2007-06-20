/*
  XBee message definition and common code to be used 
  by both SPU and aggreagtor
*/

/*
  XBee message format for 64 addresses
*/
struct XBEE_RX_PACKET {
  unsigned char start_byte; //value=0x7e
  unsigned char packet_length[2];
  unsigned char api_identifier; //value=0x80
  unsigned char source_address[8];
  unsigned char rssi_indicator;
  unsigned char broadcast_option;
  unsigned char payload[100];
} ;

struct XBEE_RX_PACKET parse_packet(unsigned char* raw_packet);

struct XBEE_TX_PACKET {
  unsigned char start_byte; //value=0x7e
  unsigned char packet_length[2];
  unsigned char api_identifier; //value=0x00
  unsigned char frame_id;
  unsigned char dest_address[8];
  unsigned char ack_broadcast_option;
  unsigned char payload[100];
}  ;

unsigned char* build_packet(struct XBEE_TX_PACKET tx_packet); 
