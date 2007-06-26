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

//Circular Q to hold all the messages
volatile static struct SWARM_MSG s_queue[MAX_SWARM_MSG_QUEUE_SIZE];
volatile static long s_nHeadIdx=0;
volatile static long s_nTailIdx=0;

/*
 * return 'true' for successful
 * and 'false' for unsuccessful/queue full
 */ 
int pushQ(struct SWARM_MSG msg)
{
  int nNewTailIdx=-1;
  if(s_nTailIdx==0)
    nNewTailIdx=MAX_SWARM_MSG_QUEUE_SIZE;
  else 
    nNewTailIdx=s_nTailIdx  -1;
  if(nNewTailIdx==s_nHeadIdx)
    return 0;//Q full
  else
    {
      s_queue[s_nTailIdx]=msg;
      s_nTailIdx=nNewTailIdx;
      return 1;//success
    }
}

/**
 * Will return null object if Q is empty. Inspect 'swarm_msg_type' 
 * field for '\0' 
 * to identify null objects
 */
struct SWARM_MSG popQ(void)
{
  struct SWARM_MSG msg;
  msg.swarm_msg_type='\0';

  int nNewHeadIdx;
  if(s_nHeadIdx==0)
    nNewHeadIdx=MAX_SWARM_MSG_QUEUE_SIZE;
  else
    nNewHeadIdx=s_nHeadIdx-1;
  if(nNewHeadIdx==s_nTailIdx)
    return msg;
  else
    {
      msg=s_queue[s_nHeadIdx];
      s_nHeadIdx=nNewHeadIdx;
      return msg;
    }
}

