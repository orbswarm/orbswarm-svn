/*
  XBee message definition and common code to be used 
  by both SPU and aggreagtor.
*/

/*
  XBee message formats for 64 bit addresses
*/

#define MAX_SWARM_MSG_LENGTH 97

/*
  For now the assumption is that there will bbe one swarm message
  per xbee packet. May change in the future
*/
struct SWARM_MSG{
  unsigned char swarm_msg_type[1];
  unsigned char swarm_msg_length[2];
  unsigned char swarm_msg_payload[MAX_SWARM_MSG_LENGTH];
};

struct XBEE_RX_PACKET {
  unsigned char start_byte; //value=0x7e
  unsigned char packet_length[2];
  unsigned char api_identifier; //value=0x80
  unsigned char source_address[8];
  unsigned char rssi_indicator;
  unsigned char broadcast_option;
  unsigned char payload[100];
} ;


struct XBEE_TX_PACKET {
  unsigned char start_byte; //value=0x7e
  unsigned char packet_length[2];
  unsigned char api_identifier; //value=0x00
  unsigned char frame_id;
  unsigned char dest_address[8];
  unsigned char ack_broadcast_option;
  unsigned char payload[100];
}  ;

struct XBEE_RX_PACKET parse_packet(unsigned char* raw_packet);
void build_packet(struct XBEE_TX_PACKET tx_packet, unsigned char* raw_packet); 
