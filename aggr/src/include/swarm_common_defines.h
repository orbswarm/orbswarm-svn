#ifndef SWARM_COMMON_DEFINES_H_
#define SWARM_COMMON_DEFINES_H_

#include <avr/io.h>
#define MAX_SWARM_MSG_LENGTH 96
//#define MAX_SWARM_MSG_LENGTH 62
#define MAX_GPS_PACKET_LENGTH 100

enum ELinkPacketType {
  eLinkNullMsg = 0x00,
  eLinkXbeeMsg,
  eLinkSpuMsg, //poll from the spu for GPS or XBee data
  eLinkLastPacketType
};

struct SWARM_MSG{
	char swarm_msg_type;
//	char swarm_msg_length[2];
	char swarm_msg_payload[MAX_SWARM_MSG_LENGTH + 2];//leave one byte for /n and another for /0 termination
};

#endif /*SWARM_COMMON_DEFINES_H_*/
