#include "swarm_common_defines.h"

/*
  For now the assumption is that there will bbe one swarm message
  per xbee packet. May change in the future
*/

struct SWARM_MSG{
   char swarm_msg_type;
   char swarm_msg_length[2];
  char swarm_msg_payload[MAX_SWARM_MSG_LENGTH + 1];//leave one byte for /0 termination
};

enum ESwarmRespCodes{
  eSwarmRespOk,
  eSwarmRespBadMsg,
  eSwarmRespQueueFull
};

struct SWARM_ACK{
  char swarm_msg_type;
  char swarm_resp_code;
};

void pushQ(struct SWARM_MSG msg);
struct SWARM_MSG popQ(void);



