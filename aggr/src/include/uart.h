#include <avr/io.h>		// include I/O definitions (port names, pin names, etc)
#include <avr/interrupt.h>	// include interrupt support
#include "../uart.c"

int uart_init(void (*handleXBeeRecv)(char c, int isError),
	      void (*handleSpuRecv)(char c, int isErrror),
	      void (*handleGpsARecv)(char c, int isErrror),
	       char (*getXBeeOutChar)(void),
	       char (*getSpuOutChar)(void));

//void sendXBeeMsg(const unsigned char *s);

void sendSpuMsg(const char *s);

void sendDebugMsg(const char *s);

void sendGPSAMsg(const char *s);

void startAsyncXBeeTransmit(void);

void stopAsyncXbeeTransmit(void);

void startAsyncSpuTransmit(void);

void stopAsyncSpuTransmit(void);

int isSpuSendInProgress(void);

int isXBeeSendInProgress(void);
