#if !defined(SWARM_MESSAGING_HDR)
#define SWARM_MESSAGING_HDR
#define MAX_SWARM_MSG_QUEUE_SIZE 100
#define MAX_SWARM_MSG_LENGTH 97

/*
  For now the assumption is that there will bbe one swarm message
  per xbee packet. May change in the future
*/

struct SWARM_MSG{
  unsigned char swarm_msg_type;
  unsigned char swarm_msg_length[2];
  unsigned char swarm_msg_payload[MAX_SWARM_MSG_LENGTH];
};

enum ESwarmRespCodes{
  eSwarmRespOk,
  eSwarmRespBadMsg,
  eSwarmRespQueueFull
};

struct SWARM_ACK{
  unsigned char swarm_msg_type;
  unsigned char swarm_resp_code;
};

int pushQ(struct SWARM_MSG msg);
struct SWARM_MSG popQ(void);

#endif

