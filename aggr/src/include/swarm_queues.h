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

void initSwarmMsgBus(void (*debug)(const char *c));
/*
 * The push() and pop() methods are guarded by a mutex. The strategy for 
 * lock acquire and release is -
 * while(1){
 * 		if(lock ==0)
 * 		{
 * 			lock++;
 * 	    	if(lock ==1)
 * 			{
 * 				//lock acquired
 * 				//do your stuff
 * 				lock --;
 * 				return;
 * 			}
 * 			else
 * 				lock --;
 * 		}
 * 		else if(ctx == interrupt)
 * 			return;
 * }
 * The difference is in the context in which they are invoked
 * Main Thread: In this context the method will loop infinitely trying to
 * acquire a lock.
 * Interrupt handler: In this context the method will return immediately 
 * with a no-op(will return "null" msg for pop()). There is also always the
 * possibilty of losing a message during a push() from the interrupt handler.
 * If possible design you are app so that there are no push()'s from the
 * interrupt handler.
 * 
 * The rationale behind is that there no time slicing in the processor.
 * The interrupt handler always has higher priority and will loop infinitely
 * if wiaitng for lock already acquired by the main thread
 */
void pushSwarmMsgBus(struct SWARM_MSG msg, int isInterruptCtx);
struct SWARM_MSG popSwarmMsgBus(int isInterruptCtx);
	
