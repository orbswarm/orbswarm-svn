int uart_init(void (*handleXBeeRecv)(unsigned char c, int isError),
	      void (*handleSpuRecv)(unsigned char c, int isErrror),
	      void (*handleGpsARecv)(unsigned char c, int isErrror));

void sendXBeeMsg(const unsigned char *s);

void sendSpuMsg(const unsigned char *s);

void sendDebugMsg(const unsigned char *s);

void sendGPSAMsg(const unsigned char *s);

void sendGPSBMsg(const unsigned char *s);
