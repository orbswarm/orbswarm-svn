/*
 * 1. The synch transmit methods i.e. [start|stop]Async*Transmit()
 * are turned off at reset
 * 2. Stopping a stopped xmit or strating a started xmit result in a
 * no op
 * 3. It maybe desirable to stop an async xmit before using one of the
 * send*Msg() methods and then starting the async xmit immediately after.
 * 4. Async xmits are available only for the spu and the xbee.
 * 5. Async receives i.e. handle*Recv() are available for all 3 UART
 * peripherals.
 * */
int uart_init(void (*handleXBeeRecv)(char c, int isError),
	      void (*handleSpuRecv)(char c, int isErrror),
	      void (*handleGpsARecv)(char c, int isErrror),
	       char (*getXBeeOutChar)(int isInterruptCtx),
	       char (*getSpuOutChar)(int isInterruptCtx));

void sendSpuMsg(const char *s);

void sendGPSAMsg(const char *s);

void sendGPSABinaryMsg(unsigned char *s, int len);

void startAsyncXBeeTransmit(void);

void stopAsyncXbeeTransmit(void);

void startAsyncSpuTransmit(void);

void stopAsyncSpuTransmit(void);

int isSpuSendInProgress(void);

int isXBeeSendInProgress(void);

void debugUART(const char *s);
