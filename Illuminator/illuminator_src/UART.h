// ------------------------------------------------------------------------
// UART.h
// Routines for interrupt controlled USART for RS-232 communication.
// Last modified: 9-Apr-07
// Modified by: MrPete
// ------------------------------------------------------------------------


void UART_Init( unsigned int baud );

void UART_send_byte( unsigned char data );
void UART_Transmit( unsigned char data );

unsigned char UART_ring_buf_byte( void );
unsigned char UART_data_in_ring_buf( void );
