// -------------------------------------------------------------------------
// UART.c
// Routines for interrupt controlled UART
// Last modified: 30-July-2007
// Modified by: MrPete from AVR sample code.
// -------------------------------------------------------------------------

/* Includes */
#include <avr/io.h>
#include <avr/interrupt.h>

#include "UART.h"


/* UART Buffer Defines */
#define UART_RX_BUFFER_SIZE 16     /* 2,4,8,16,32,64,128 or 256 bytes */
#define UART_TX_BUFFER_SIZE 16


#define UART_RX_BUFFER_MASK ( UART_RX_BUFFER_SIZE - 1 )
#if ( UART_RX_BUFFER_SIZE & UART_RX_BUFFER_MASK )
	#error RX buffer size is not a power of 2
#endif

#define UART_TX_BUFFER_MASK ( UART_TX_BUFFER_SIZE - 1 )
#if ( UART_TX_BUFFER_SIZE & UART_TX_BUFFER_MASK )
	#error TX buffer size is not a power of 2
#endif


/* Static Variables -- Tx & Rx Ring Buffers */
static volatile unsigned char UART_RxHead;
static volatile unsigned char UART_RxTail;
static volatile unsigned char UART_TxHead;
static volatile unsigned char UART_TxTail;

static unsigned char UART_RxBuf[UART_RX_BUFFER_SIZE];
static unsigned char UART_TxBuf[UART_TX_BUFFER_SIZE];

// ----------------------------------------------------------------
// Init UART - Enable Rx Interrupts

void UART_Init( unsigned int baud ) 
{ 
	unsigned char x;
	
	UBRRH = (unsigned char)(baud>>8);			/* Set baud rate */ 
	UBRRL = (unsigned char)baud;				/* Value depends on MPU clock speed */
	
	UCSRB = (1<<RXEN)|(1<<TXEN);				/* Enable receiver and transmitter */ 
	UCSRC = (3<<UCSZ0);							/* Set frame format: 8data, 1-stop bit */ 

	x = 0;										/* Init ring buf indexes */
	UART_RxTail = x;
	UART_RxHead = x;
	UART_TxTail = x;
	UART_TxHead = x;
	
	UCSRB |= (1<<RXCIE);						/* Enable Rx Complete interrupt */
}

// ----------------------------------------------------------------
// Interrupt handlers - UART Rx vector

SIGNAL(SIG_USART0_RX)
{
	unsigned char data;
	unsigned char tmphead;

	data = UDR;                 /* Read the received data */
	/* Calculate buffer index */
	tmphead = ( UART_RxHead + 1 ) & UART_RX_BUFFER_MASK;
	UART_RxHead = tmphead;      /* Store new index */

	if ( tmphead == UART_RxTail )
	{
		/* ERROR! Receive buffer overflow */
	}
	
	UART_RxBuf[tmphead] = data; /* Store received data in buffer */
}

// ----------------------------------------------------------------
// Interrupt handler - UART Tx vector for Data Register Empty - UDRE

SIGNAL(SIG_USART0_UDRE)
{
	unsigned char tmptail;

	/* Check if all data is transmitted */
	if ( UART_TxHead != UART_TxTail )
	{
		/* Calculate buffer index */
		tmptail = ( UART_TxTail + 1 ) & UART_TX_BUFFER_MASK;
		UART_TxTail = tmptail;      /* Store new index */
	
		UDR = UART_TxBuf[tmptail];  /* Start transmition */
	}
	else
	{
		UCSRB &= ~(1<<UDRIE);   /* Disable UDRE interrupt or we'll re-trigger on exit */
	}
}

// ----------------------------------------------------------------
// Check if there are any bytes waiting in the input ring buffer.

unsigned char UART_data_in_ring_buf( void )
{
	return ( UART_RxHead != UART_RxTail ); /* Return 0 (FALSE) if the receive buffer is empty */
}

// ----------------------------------------------------------------
// Pull 1 byte from Ring Buffer of bytes received from USART

unsigned char UART_ring_buf_byte( void )
{
	unsigned char tmptail;
	
	while ( UART_RxHead == UART_RxTail )  /* Wait for incoming data */
		;
	tmptail = ( UART_RxTail + 1 ) & UART_RX_BUFFER_MASK;/* Calculate buffer index */
	
	UART_RxTail = tmptail;                /* Store new index */
	
	return UART_RxBuf[tmptail];           /* Return data */
}

// ---------------------------------------------------------------------
// Put byte into ring buffer - use interupts to send strings

void UART_send_byte( unsigned char data )
{
	unsigned char tmphead;
	/* Calculate buffer index */
	tmphead = ( UART_TxHead + 1 ) & UART_TX_BUFFER_MASK; /* Wait for free space in buffer */
	while ( tmphead == UART_TxTail );

	UART_TxBuf[tmphead] = data;			/* Store data in buffer */
	UART_TxHead = tmphead;				/* Store new index */

	UCSRB |= (1<<UDRIE);				/* Enable UDRE interrupt */
}

// wait for empty buffer, then send byte now.

void UART_Transmit( unsigned char data ) 
{ 
	/* Wait for empty transmit buffer */ 
	while ( !( UCSRA & (1<<UDRE)) ) 
		; 
	/* Put data into buffer, sends the data */ 
	UDR = data; 


// ---------------------------------------------------------------------
// End of File
