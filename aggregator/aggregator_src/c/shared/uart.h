/**
   UART assignment:
   0 - SPU
   1 - GPS
   2 - debug
   3 - XBee
 */

int uart_init(void (*handleXBeeRecv)(unsigned char c, int isError),
	 void (*handleSpuRecv)(unsigned char c, int isErrror));


void sendXBeeMsg(const unsigned char *s);

void sendSpuMsg(const unsigned char *s);

void sendDebugMsg(const char *s);
