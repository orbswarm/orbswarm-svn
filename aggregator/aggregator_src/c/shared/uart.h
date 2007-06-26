#include <avr/io.h>		// include I/O definitions (port names, pin names, etc)
#include <avr/interrupt.h>	// include interrupt support

int init(void (*handleXBeeRecv)(char c, int isError),
	 void (*handleSpuRecv)(char c, int isErrror));

void sendXBeeMsg(char *s);

void sendSpuMsg(char *s);
