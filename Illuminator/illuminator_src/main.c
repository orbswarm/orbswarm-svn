// -----------------------------------------------------------------------
// 
//	File: main.c
//	main file for SWARM Orb LED Illumination Unit http://www.orbswarm.com
//      which is a custom circuit board by rick L using an Atmel AVR atmega-8
//      build code using WinAVR toolchain: see makefile
//
//	Written by Jonathan Foote (Head Rotor at rotorbrain.com)
//      based on code by Petey the Programmer
// -----------------------------------------------------------------------

#include <avr/io.h>
#include <avr/interrupt.h>
#include <math.h>

#include "UART.h"
#include "putstr.h"        /* you can eliminate this entirely to save mem */
#include "illuminator.h"   /* contains illuminatorStruct definition */
#include "parser.h"	   /* contains parser for input commands */
#include "gamma.h"  	   /* contains gamma lookup table */

// =======================================================

// Set this for testing when a high output pin turns off the LEDs
//#define INVERT foo

/* This version uses putstr.c for debugging. To save memory,
   you can remove all instances of putstr and other debug code by
   removing everything commented DBG_REMOVE. In linux, 
   grep -v DBG_REMOVE main.c > newversion.c 
   (also take it out of the link line in the makefile)*/


/* this is the main data struct defined in illuminator.h */
volatile illuminatorStruct illum;

/* preserve this during interrupts if necessary */
volatile int pwm=0;


void doFade(illuminatorStruct *illum){
  int diff;

  putstr("doFade rcount");
  illum->rInc = 0;
  illum->gInc = 0;
  illum->bInc = 0;
  if(illum->Time == 0) {
    illum->R = illum->tR;
    illum->G = illum->tG;
    illum->B = illum->tB;
  }

  /* to fade, find difference (amount we need to change) */
  /* delay count will be inversely proportional to diff  */
  /* i.e. more increments per time for bigger difference */
  /* delay count will be proportional to time param      */
  /* i.e. wait longer for longer time                   */
  else {
    diff = illum->tR - illum->R;
    if (diff > 0)
      illum->rInc = 1;
    else if (diff < 0) {
      illum->rInc = -1;
      diff = -diff;
    }

    illum->rCount = diff==0? 0 : (int) (256/diff);
    illum->rCount *= illum->Time;
    putS16(illum->rCount );


    diff = illum->tG - illum->G;
    if (diff > 0)
      illum->gInc = 1;
    else if (diff < 0) {
      illum->gInc = -1;
      diff = -diff;
    }
    illum->gCount = diff==0? 0 : (int) (256/diff);
    illum->gCount *= illum->Time;


    diff = illum->tB - illum->B;
    if (diff > 0)
      illum->bInc = 1;
    else if (diff < 0) {
      illum->bInc = -1;
      diff = -diff;
    }
    illum->bCount = diff==0? 0 : (int) (256/diff);
    illum->bCount *= illum->Time;

  }

}


int main( void ){
  unsigned char cData; 		/* byte to read from UART */
  unsigned int rdelay=0;
  unsigned int gdelay=0;
  unsigned int bdelay=0;

  /* set up DDRD for serial IO */
  DDRD = 0xF2;		/* 0b 1111 0010	   1 indicates Output pin */
  //	led_port = 0xF0;	// turn OFF leds
  
   UART_Init(11); // 12 = 38.4k when system clock is 8Mhz (AT Tiny2313) 
  //11 = 38.4K for 7.37MHz xtal 
  // 51 = 9600 baud when system clock is 8Mhz
   sei();

  DDRB |= _BV(PB0); /*  PB0 is spare debug  */
  DDRB |= _BV(PB1); /*  PB1 is RED output */
  DDRB |= _BV(PB2); /*  PB2 is GRN output */
  DDRB |= _BV(PB3); /*  PB3 is BLU output */


  putstr("\r\n...Illuminator at address ");
  illum.Addr = readAddressEEPROM();
  putS16((short)illum.Addr );
  putstr(" says hello...\r\n");

 
  /* init data structure */
  illum.Addr = 0; 		/* set this from program */
  illum.H=0;
  illum.S=0;
  illum.V=0;
  illum.R=0; 			/* raw RGB output vales */
  illum.G=0;
  illum.B=0;
  illum.tR=0; 			/* target RGB values */
  illum.tG=0;
  illum.tB=0;
  illum.rCount=0; 			/* fade delay counts */
  illum.gCount=0; 			
  illum.bCount=0; 			
  illum.rInc =0; 			/* fade increment counts */
  illum.gInc =0; 			/* fade increment counts */
  illum.bInc =0; 			/* fade increment counts */
  illum.Time=0;
  illum.Now=0;
  illum.fading=0;
  // =======================================================
  

  while(1)    {
    
    // Main parser loop starts here. To save space, not modularized 
    if (UART_data_in_ring_buf()) { // check for waiting UART data from SPU
      cData = UART_ring_buf_byte(); // get next char from ring buffer... 
      if(accumulateCommandString(cData)){ // ... and add to command string
	// if we are here we have a complete command; parse it
	parseCommand(); // parse and execute commands
      }
    }


#ifdef  INVERT  
  /* main PWM loop is free-running here INVERTED VERSION */
    PORTB=0x00; 		/* turn on all LEDs */
    for(pwm=0;pwm<255;pwm++){
      if(pwm >= gtab[illum.R]) /* if we've reached red value turn off R bit */
	PORTB |= _BV(PB1);	
      if(pwm >= gtab[illum.G])       
	PORTB |= _BV(PB2);     /* if we've reached grn value turn off B bit */
      if(pwm >= gtab[illum.B])
	PORTB |= _BV(PB3);     /* if we've reached blu value turn off G bit */
#else
  /* main PWM loop is free-running here */
    PORTB=0x0F; 		/* turn on all LEDs */
    for(pwm=0;pwm<255;pwm++){
      if(pwm >= gtab[illum.R])  /* if we've reached red value turn off R bit */
	PORTB &=~_BV(PB1);	
      if(pwm >= gtab[illum.G])       
	PORTB &=~_BV(PB2);     /* if we've reached grn value turn off B bit */
      if(pwm >= gtab[illum.B])
	PORTB &=~_BV(PB3);     /* if we've reached blu value turn off G bit */
#endif
      /* IF we are fading, AND we're not there, AND we've waited enough... */
      if (illum.rInc && (illum.R != illum.tR) && (rdelay++ >= illum.rCount)) {
	rdelay = 0;
	illum.R += illum.rInc;	/* increment towards target */
	if (illum.R == illum.tR){ 
	  illum.rInc = 0;	/* we've reached our target, stop increment */
	}

      }

      if (illum.gInc && (illum.G != illum.tG) && (gdelay++ >= illum.gCount)) {
	gdelay = 0;
	illum.G += illum.gInc;	/* increment towards target */
	if (illum.G == illum.tG) { 
	  illum.gInc = 0;	/* we've reached our target, stop increment */
	}
      }

      if (illum.bInc && (illum.B != illum.tB) && (bdelay++ >= illum.bCount)) {
	bdelay = 0;
	illum.B += illum.bInc;	/* increment towards target */
	if (illum.B == illum.tB) 
	  illum.bInc = 0;	/* we've reached our target, stop increment */
      }
    }
  }
}
