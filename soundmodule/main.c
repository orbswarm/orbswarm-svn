/*****************************************************************************
*
* File: main.c
*
* Main file for AT-Tiny Serial SPU / MP3 Test  
*
* Written for AT-Tiny2313
*
* Uses UART for 38.4K Baud Full Duplex Async Serial Communications with SPU and Lighting Module.
*
* Uses USI module as second serial line for one-way sending of serial data to MP3 module at 9600 baud.
* If Music Module Cmds are received from the SPU, they are routed to the MP3 player.
*
* Written by Petey the Programmer
* 30-July-2007
*
* modified by Jonathan Foote (headrotor at rotorbrain.com)
*
*  (Tiny2313 has a total of 2000 bytes available.)
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
// ============================================================================

int main( void )
{
  unsigned char cData, CmdStr[BUF_SIZE];
  unsigned char CmdLen = 0;
  unsigned char n=0;
  unsigned char inCmd = 0; 	/* very simple state machine; equals 1 if in a command (after '<' but before '>') */

  DDRD = 0xF2;		/* 0b 1111 0010	   1 indicates Output pin */
  
  UART_Init(11);		// 12 = 38.4k when system clock is 8Mhz (AT Tiny2313) 11 = 38.4K for 7.37MHz xtal 
				// 51 = 9600 baud when system clock is 8Mhz
  
  USI_UART_Flush_Buffers();	  
  USI_UART_Initialise_Receiver(); // Initialisation for USI Rx/Tx
  
  sei();			// Enable global interrupts
    
	
  for(;;) {			// loop forever
	
    if (UART_data_in_ring_buf()) { // check for waiting UART data from SPU
      cData = UART_ring_buf_byte(); // pull 1 chr from ring buffer. 
      //UART_Transmit(cData); 

      if(cData == '<') { 	/* start of a command. Reset everything */
	CmdStr[0] = 0;		
	CmdLen = 0;
	inCmd = 1;		/* we are in the command state */
      }
      else if (cData == '>'){	/* we got a complete command; process it */
	n = 0;			/* index of current char in command str */
	if (CmdStr[n++] == 'M'){ /* if it starts with an M, it's a music command */
	  while(CmdStr[n] == ' ') ++n; /* skip spaces */

	  for(;n<CmdLen;n++) { 	/* send the rest of the command */
	    USI_UART_Transmit_Byte(CmdStr[n]);		
	  }
	  USI_UART_Transmit_Byte(13); /* send CR to terminate command */
	}
	/* reset everything even if it was not a M command */
	CmdStr[0] = 0;		
	CmdLen = 0;
	inCmd = 0;
      }
      else {
	if (inCmd) { 					/* accumulate chars into command buffer */
	  CmdStr[CmdLen] = cData;		  
	  if (CmdLen < BUF_SIZE) CmdLen++;
	}
      }
    }
  }    
}
