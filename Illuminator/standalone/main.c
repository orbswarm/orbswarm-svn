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
#include "timer.h"


/* hardwired address hack because eeprom was flaky */
unsigned char getAddress(void);

// =======================================================

// Set this for testing when a high output pin turns off the LEDs
//#define INVERT inverted

/* This version uses putstr.c for debugging. To save memory,
   you can remove all instances of putstr and other debug code by
   removing everything commented DBG_REMOVE. In linux, 
   grep -v DBG_REMOVE main.c > newversion.c 
   (also take it out of the link line in the makefile)*/


/* this is the main data struct defined in illuminator.h */
volatile illuminatorStruct illum;

/* preserve this during interrupts if necessary */
volatile int pwm=0;


/* flags and counts for Timer0 */
extern volatile unsigned short Timer0_ticks;
extern volatile unsigned char Timer0_10hz_Flag;


char debug_out = 0;			/* set this to output debug info */


/* color index tables */
/* red, green, blue, magenta, yellow, cyan, white,black */
/* color indexes 0 and 1  alternate for blink */
unsigned char maxIndex = 15;
unsigned char cir[] = {0xFE,0x00,0x00,0xFE, 0xFE,0x00,0xFE,0x00, 0xFE, 0x00,0x00,0xFE, 0xFE,0x00,0xFE,0x00};
unsigned char cig[] = {0x00,0xFE,0x00,0x00, 0xFE,0xFE,0xFE,0x00, 0x00, 0xFE,0x00,0x00, 0xFE,0xFE,0xFE,0x00};
unsigned char cib[] = {0x00,0x00,0xFE,0xFE, 0x00,0xFE,0xFE,0x00, 0x00, 0x00,0xFE,0xFE, 0x00,0xFE,0xFE,0x00};


void doFade(illuminatorStruct *illum){
  int diff;

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
    if(debug_out) putS16(illum->rCount );


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
  
  Timer0_Init();		/* Init Tick Timer */
  
  
  sei();
  
  DDRB |= _BV(PB0); /*  PB0 is spare debug  */
  DDRB |= _BV(PB1); /*  PB1 is RED output */
  DDRB |= _BV(PB2); /*  PB2 is GRN output */
  DDRB |= _BV(PB3); /*  PB3 is BLU output */
  
  
  illum.Addr = getAddress() & 0x0F;
  if(debug_out){
    putstr("\r\n...Illuminator @ addr ");
    //illum.Addr = readAddressEEPROM();
    putS16((short)illum.Addr );
    putstr(" says hello...\r\n");
  }
  
  /* init data structure */
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
  illum.blink=0;	       /* set with number of times to blink */
  illum.blinkCounter=0;	 /* count of remaining ticks until we blink */
  illum.blinkToggle=0;		/* which phase of blink are we in?  */

  // =======================================================
  
  while(1)    {
    
    // Main parser loop starts here. To save space, not modularized 
    if (UART_data_in_ring_buf()) { // check for waiting UART data from SPU
      cData = UART_ring_buf_byte(); // get next char from ring buffer... 
      if(0) UART_send_byte(cData);   /* echo char to serial out for debug if req. */
      if(accumulateCommandString(cData)){ // ... and add to command string
	// if we are here we have a complete command; parse it
	parseCommand(); // parse and execute commands
      }
    }
    
    
    if (Timer0_10hz_Flag) {		// do these tasks only 10 times per second
      Timer0_10hz_Flag = 0;
      if(illum.blink) {
	if(illum.blinkCounter == illum.blink){
	  if(illum.blinkToggle){
	    if(debug_out) putstr("\r\n blink on");
	    illum.blinkToggle = 0;
	    illum.tR = cir[0]; 
	    illum.tG = cig[0]; 
	    illum.tB = cib[0]; 
	    doFade(&illum);
	  }
	  else {
	    if(debug_out) putstr("\r\n blink off");
	    illum.blinkToggle = 1;
	    illum.tR = cir[1]; 
	    illum.tG = cig[1]; 
	    illum.tB = cib[1]; 
	    doFade(&illum);
	  }
	  illum.blinkCounter = 0;
	}
	++illum.blinkCounter;
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

      /* put these computations in the loop to slow it down a bit */
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

void pauseMS(unsigned short mS){
  Timer0_reset();
  while (Timer0_ticks < mS) ;
  Timer0_reset();
}

void hue2rgb(short inthue, unsigned char charval, unsigned char *red, unsigned char *grn, unsigned char *blu) {
  float p,n,hue;
  float r,g,b;
  unsigned char k;

  while(inthue > 360)
    inthue -= 360;

  hue = (float) inthue;
  /* Get principal component of angle */
  //hue -= 360*(float)floor(hue/360);

  /* Get section */
  hue /= 60;
  //k = (int)floor(hue);
  k = (unsigned char)(hue);
  if (k == 6) {
    k = 5;
  } else if (k > 6) {
    k = 0;
  }
  p = hue - k;
  n = 1 - p;

  /* Get RGB values based on section */
  switch (k) {
  case 0:
    r = 1; g = p; b = 0;
    break;
  case 1:
    r = n; g = 1;  b = 0;
    break;
  case 2:  r = 0; g = 1; b = p;
    break;
  case 3:
    r = 0;
    g = n;
    b = 1;
    break;
  case 4:
    r = p;
    g = 0;
    b = 1;
    break;
  case 5:
    r = 1;
    g = 0;
    b = n;
    break;
  default:
    r = 1;
    g = 1;
    b = 1;
    break;
  }

  *red = (unsigned char)(r * charval);
  *grn = (unsigned char)(g * charval);
  *blu = (unsigned char)(b * charval);

  return;
}
