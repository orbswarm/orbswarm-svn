#include "queues.h"
#include <string.h>

int push(char* msg, Queue* qPtr)
{
  unsigned char tmpHead;
  tmpHead = (qPtr->head +1) & QUEUE_MASK;
  if(tmpHead == qPtr->tail)
    return 0;//queue is full
  else{
    strncpy(qPtr->buffer[tmpHead], msg, MSG_LENGTH);
    qPtr->head=tmpHead;
    return 1;
  }
}

int pop(char* msg, Queue* qPtr)
{
  unsigned char tmpTail;
  if(qPtr->head == qPtr->tail)
    return 0;
  else{
    tmpTail= (qPtr->tail +1) & QUEUE_MASK;
    strncpy(msg, qPtr->buffer[tmpTail], MSG_LENGTH);
    qPtr->tail=tmpTail;
    return 1;
  }
}


