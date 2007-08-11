/*****************************************************************************
*
* File: main.c
*
* Main file for AT-Tiny Serial SPU / MP3 Test  
*
* Originally Written for AT-Tiny2313
*
* Uses UART for 38.4 kb serial communications with SPU and lighting module.
* Echos all the chrs received from the SPU downstream to any other modules.
*
* Written by Petey the Programmer
* 30-July-2007
*
*  Modified for atmega-8 and serial lighting protocol by Jon Foote (Head Rotor)
*
****************************************************************************/

#include <avr/io.h>
#include <avr/interrupt.h>

#include "UART.h"
#include "putstr.h"		/* you can eliminate this entirely to save mem */
#include "illuminator.h"	/* contains illuminatorStruct definition */
#include "parser.h"	/* contains illuminatorStruct definition */

#define led_port	PORTD

// =======================================================



/* This version uses putstr.c for debugging. To save memory,
   you can remove all instances of putstr and other debug code by
   removing everything commented DBG_REMOVE. In linux, 
   grep -v DBG_REMOVE main.c > newversion.c 
   (also take it out of the link line in the makefile)*/


/* this is the main data struct defined in illuminator.h */
illuminatorStruct illum;

/* preserve this during interrups if necessary */
volatile int pwm=0;

int main( void ){
  unsigned char cData; 		/* byte to read from UART */

  
  DDRD = 0xF2;		/* 0b 1111 0010	   1 indicates Output pin */
  //	led_port = 0xF0;	// turn OFF leds
  
  UART_Init(11); // 12 = 38.4k when system clock is 8Mhz (AT Tiny2313) 
  //11 = 38.4K for 7.37MHz xtal 
  // 51 = 9600 baud when system clock is 8Mhz
  sei();

  DDRB = 0x07; 			/* use PB0 for R, PB1 for G, PB2 for B */

  putstr("\r\n...Illuminator says hello...\r\n"); // DBG_REMOVE
 
  /* init data structure */
  illum.Addr = 0; 		/* may want to set this from DIP? */
  illum.H=0;
  illum.S=0;
  illum.V=0;
  illum.R=0; 			/* raw RGB output vales */
  illum.G=0;
  illum.B=0;
  illum.tHue=0;
  illum.tSat=0;
  illum.tVal=0;
  illum.Time=0;
  illum.Now=0;
  // =======================================================
  
  
  while(1)    {
    
    // Main parser loop starts here. To save space, not modularized 
    if (UART_data_in_ring_buf()) { // check for waiting UART data from SPU
      cData = UART_ring_buf_byte(); // get next char from ring buffer... 
      if(accumulateCommandString(cData)){ // ... and add to command string
	// if we are here we have a complete command; parse it
	parseCommand(); // parse and execute commands
      }
      // Echo byte to next illuminator downstream
      //UART_Transmit(cData);   // wait for empty buffer. send byte now.
       UART_send_byte(cData);   // put byte in buffer - send using interupts
    }

    /* Crude PWM loop runs as fast as we can...*/
    PORTB = 0x07;		/* turn R, G, B on */
    for(pwm=0;pwm<255;pwm++){
      if(pwm >= illum.R)     /* if we've reached red value turn off R bit */
	PORTB &=~_BV(0);	
      if(pwm >= illum.G)       
	PORTB &=~_BV(1);     /* if we've reached grn value turn off B bit */
      if(pwm >= illum.B)
	PORTB &=~_BV(2);     /* if we've reached blu value turn off G bit */
    }
    
  }
}
