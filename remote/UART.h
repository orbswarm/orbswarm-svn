// --------------------------------------------------------------------------------------------------------------------------------------------------------------
// UART.h
// Routines for interrupt controlled USART for RS-232 communication.
// Last modified: 9-Apr-07
// Modified by: MrPete
// UART baud rate defs added by Jon
// --------------------------------------------------------------------------------------------------------------------------------------------------------------


// uart_Init() speed defs from Atmel ATmega8 manual, pp. 159--160
// http://www.atmel.com/dyn/resources/prod_documents/doc2486.pdf
// assumes u2X = 1
#if  F_CPU == 8000000               		// 8MHz processor
#warning "UART: using 8 mHz clock"
#define UART_9600    51
#define UART_14400   34
#define UART_19200   25
#define UART_28800   16
#define UART_384000  12

#endif


#if  F_CPU == 7372800               		// 7.37MHz processor
#warning "UART: using 7.37 mHz clock"
#define UART_9600    47
#define UART_14400   31
#define UART_19200   23
#define UART_28800   15
#define UART_384000  11
#endif


#if F_CPU == 3686400               		// 3.69MHz processor
#warning "UART: using 3.69 mHz clock"
#define UART_9600    23
#define UART_14400   15
#define UART_19200   11
#define UART_28800   7
#define UART_384000  5

#endif

void UART_Init( unsigned int baud );

void UART_send_byte( unsigned char data );

unsigned char UART_ring_buf_byte( void );
unsigned char UART_data_in_ring_buf( void );
