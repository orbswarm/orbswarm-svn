#ifndef SWARM_COMMON_DEFINES_H_
#define SWARM_COMMON_DEFINES_H_

#define MAX_SWARM_MSG_LENGTH 96
struct SWARM_MSG{
	char swarm_msg_type;
	char swarm_msg_length[2];
	char swarm_msg_payload[MAX_SWARM_MSG_LENGTH + 2];//leave one byte for /n and another for /0 termination
};

enum ELinkPacketType {

  eLinkRTCM         = 0x00,   // Binary data
  eLinkMotorControl,
  eLinkArt,
  eLinkNMEA,
  eLinkNavigation,            // Including trajectory and such
  eLinkCCS,                   // Command Control and Status
  eLinkConsole,
  eLinkCodeDownload,
  eLinkLoopback,
  eLinkSonar,
  eLinkAck,
  eLinkNullMsg,
  eLinkSpuMsg, //poll from the spu for GPS or XBee data
  eLinkLastPacketType
};


#endif /*SWARM_COMMON_DEFINES_H_*/
