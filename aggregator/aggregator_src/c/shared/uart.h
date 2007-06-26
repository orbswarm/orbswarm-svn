//#if !defined(UART_HDR)
//#define UART_HDR

int uart_init(void (*handleXBeeRecv)(unsigned char c, int isError),
	 void (*handleSpuRecv)(unsigned char c, int isErrror));

void sendXBeeMsg(unsigned char *s);

void sendSpuMsg(unsigned char *s);
//#endif
