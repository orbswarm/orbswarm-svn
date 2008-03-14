#define MSG_LENGTH 100 //TO DO: This is actually the same as BUFLENGTH in scanner.h
#define QUEUE_SIZE 32
#define QUEUE_MASK (QUEUE_SIZE -1)
#if (QUEUE_SIZE & QUEUE_MASK)
   #error Queue size is not a power of 2
#endif

typedef struct queueStruct{
  char buffer[QUEUE_SIZE][MSG_LENGTH];
  unsigned char head;
  unsigned char tail;
}  Queue;

int push(char* msg, Queue* q);

int pop(char* msg, Queue* q);
