#include "swarm_common_defines.h"

#define MAX_SPU_MSG_QUEUE_SIZE 1024
#define SPU_Q_MASK (MAX_SPU_MSG_QUEUE_SIZE -1)
#if (MAX_SPU_MSG_QUEUE_SIZE & SPU_Q_MASK)
     #error SPU queue size not a power of 2
#endif

#define MAX_XBEE_MSG_QUEUE_SIZE 256
#define XBEE_Q_MASK (MAX_XBEE_MSG_QUEUE_SIZE -1)
#if (MAX_XBEE_MSG_QUEUE_SIZE & XBEE_Q_MASK)
     #error XBEE queue size not a power of 2
#endif

#define MAX_SWARM_MSG_BUS_SIZE 16
#define SWARM_MSG_BUS_MASK (MAX_SWARM_MSG_BUS_SIZE -1)
#if (MAX_SWARM_MSG_BUS_SIZE & SWARM_MSG_BUS_MASK)
     #error SWARM queue size not a power of 2
#endif


void pushSpuDataQ(const char* msg);
char popSpuDataQ(void);

char popXbeeDataQ(void);
void pushXbeeDataQ(const char* msg);

void pushSwarmMsgBus(struct SWARM_MSG msg);
struct SWARM_MSG popSwarmMsgBus(void);
	
