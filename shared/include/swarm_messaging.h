#if !defined(SWARM_MESSAGING_HDR)
#define SWARM_MESSAGING_HDR
#define MAX_SWARM_MSG_QUEUE_SIZE 64
#define MAX_SWARM_MSG_LENGTH 96

#define SWARM_Q_MASK (MAX_SWARM_MSG_QUEUE_SIZE -1)
#if (MAX_SWARM_MSG_QUEUE_SIZE & SWARM_Q_MASK)
     #error SWARM queue size not a power of 2
#endif

/*
  For now the assumption is that there will bbe one swarm message
  per xbee packet. May change in the future
*/

struct SWARM_MSG{
   unsigned char swarm_msg_type;
   unsigned char swarm_msg_length[2];
  unsigned char swarm_msg_payload[MAX_SWARM_MSG_LENGTH + 1];//leave one byte for /0 termination
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

void pushQ(struct SWARM_MSG msg);
struct SWARM_MSG popQ(void);

#endif

