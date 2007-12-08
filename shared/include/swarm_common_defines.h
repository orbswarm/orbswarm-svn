//gps.h
#define MAX_GPS_PACKET_LENGTH 100

//swarm_messaging.h
#define SWARM_MESSAGING_HDR
#define MAX_SWARM_MSG_QUEUE_SIZE 16
#define MAX_SWARM_MSG_LENGTH 96

#define SWARM_Q_MASK (MAX_SWARM_MSG_QUEUE_SIZE -1)
#if (MAX_SWARM_MSG_QUEUE_SIZE & SWARM_Q_MASK)
     #error SWARM queue size not a power of 2
#endif

//xbee.h
#define XBEE_FRAME_ID 0x7e

//aggregator_utils.h
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

#define MAX_SPUGPS_MSG_QUEUE_SIZE 2
#define SPUGPS_Q_MASK (MAX_SPUGPS_MSG_QUEUE_SIZE -1)
#if (MAX_SPUGPS_MSG_QUEUE_SIZE & SPUGPS_Q_MASK)
     #error SPUGPS queue size not a power of 2
#endif

