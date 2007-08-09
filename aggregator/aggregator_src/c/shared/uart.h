int uart_init(void (*handleXBeeRecv)(char c, int isError),
	      void (*handleSpuRecv)(char c, int isErrror),
	      void (*handleGpsARecv)(char c, int isErrror),
	      char (*getXBeeOutChar)(void),
	      char (*getSpuOutChar)(void));

//void sendXBeeMsg(const unsigned char *s);

void sendSpuMsg(const char *s);

void sendDebugMsg(const char *s);

void sendGPSAMsg(const char *s);

void sendGPSBMsg(const char *s);

void startXBeeTransmit(void);

void startSpuTransmit(void);

int isSpuSendInProgress(void);

int isXBeeSendInProgress(void);
