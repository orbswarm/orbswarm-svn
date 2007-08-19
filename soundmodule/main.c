/*****************************************************************************
*
* File: main.c
*
* Main file for AT-Tiny Serial SPU / MP3 Test  
*
* Written for AT-Tiny2313
*
* Uses UART for 38.4K Baud Full Duplex Async Serial Communications with SPU and Lighting Module.
* Echos all the chrs received from the SPU to the Lighting Module.
*
* Uses USI module as second serial line for one-way sending of serial data to MP3 module at 9600 baud.
* If Music Module Cmds are received from the SPU, they are routed to the MP3 player.
*
* Written by Petey the Programmer
* 30-July-2007
*
* Code Size = 1044 Bytes (Tiny2313 has a total of 2000 bytes available.)
*
****************************************************************************/

#include <avr/io.h>
#include <avr/signal.h>
#include <avr/interrupt.h>

#include "USI_UART_config.h"
#include "UART.h"
#include "putstr.h"

#define led_port	PORTD

#define BUF_SIZE 20
// =====================================================================================================================================

int main( void )
{
    unsigned char cData, CmdStr[BUF_SIZE];
	unsigned char CmdLen = 0;
	unsigned char n=0;
		
	DDRD = 0xF2;		/* 0b 1111 0010	   1 indicates Output pin */
//	led_port = 0xF0;	// turn OFF leds
	
	UART_Init(11);		// 12 = 38.4k when system clock is 8Mhz (AT Tiny2313) 11 = 38.4K for 7.37MHz xtal 
						// 51 = 9600 baud when system clock is 8Mhz

    USI_UART_Flush_Buffers();
    USI_UART_Initialise_Receiver();								// Initialisation for USI Rx/Tx
    
	sei();														// Enable global interrupts
    
//    MCUCR = (1<<SE)|(0<<SM1)|(0<<SM0);						// Enable Sleepmode: Idle
    
	putstr("\r\n...Tiny Test...\r\n");
	
// =====================================================================================================================================
// 	USI_UART_Transmit_Byte('V');	// Send Play All to MP3 player
//	USI_UART_Transmit_Byte('3');
//	USI_UART_Transmit_Byte('A');
//	USI_UART_Transmit_Byte(13);		

    for( ; ; )					// Run forever
    {
	
	if (UART_data_in_ring_buf()) {			// check for waiting UART data from SPU
		cData = UART_ring_buf_byte();		// pull 1 chr from ring buffer. 
											// Echo byte from SPU to lighting unit.
		UART_Transmit(cData);				// wait for empty buffer. send byte now.
	//	UART_send_byte(cData);				// put byte in buffer - send using interupts
//		USI_UART_Transmit_Byte('x');
//	USI_UART_Transmit_Byte(13);		
					
		if (cData == '>'){	// process commands after Carrage Return is rcvd
//			USI_UART_Transmit_Byte('y');
//	USI_UART_Transmit_Byte(13);		
		
			if ((CmdStr[0] == '<') && (CmdStr[1] == 'M')){ 	// send music cmds to MP3 player
//			if (CmdStr[0] == '<'){ 	// send music cmds to MP3 player

//			USI_UART_Transmit_Byte('z');
			for(n=4;n<CmdLen;n++) {
				USI_UART_Transmit_Byte(CmdStr[n]);		
		       }
			USI_UART_Transmit_Byte(13);

				}
			CmdStr[0] = 0;	// reset CmdStr to blank
			CmdLen = 0;
			}
		else
			{
			CmdStr[CmdLen] = cData;		// store CmdStr until Carrage Return received
			if (CmdLen < BUF_SIZE) CmdLen++;
			}
		}
    }    
}
