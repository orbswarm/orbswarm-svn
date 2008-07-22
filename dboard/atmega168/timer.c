// ---------------------------------------------------------------------
// 
//	File: timer.c
//      SWARM Orb  http://www.orbswarm.com
//	Motor control code: Set timer0 to interrupt at 100hz rate
//
//	Refactored by Jonathan (Head Rotor at rotorbrain.com)
//      Original Version by Petey the Programmer  7-May-2007
// rewritten for atmega88
// -----------------------------------------------------------------------


// How this works: Timer0 interrupts at 3600hz rate, and
// increments tick variables at each overflow
// to get any period in 1/3600 second resolution, count 
// ticks and reset variable at end of period

#include <avr/io.h>
#include <avr/interrupt.h>
#include "global.h"
#include "timer.h"   
#include "encoder.h"

/* global variables */

volatile unsigned short Timer0_ticks;
volatile unsigned short Timer2_ticks;
volatile unsigned char Timer0_100hz_Flag;

extern volatile unsigned short encoder1_count;
extern volatile short encoder1_speed;
extern volatile unsigned short encoder1_dir;


// --------------------------------------------------------------------------
// Interrupt routine to handle Timer0 overflow

SIGNAL(TIMER0_OVF_vect)
{
  Timer0_ticks++;		// increment slow counter
  
  // use PB0 (LED) as debug output: toggle at timer rate
  // should see 1800 hz square wave on PB0
  //if(Timer0_ticks & 0x01) // extract bit 1
  //  PORTB |= 0x01; // set bit 1;
  //else
  //  PORTB &= 0xFE; // clear bit 1

  if (Timer0_ticks >= 36){  // count 36 overflows of 7.37mHz/(8*256) = 3600hz
    Timer0_100hz_Flag = 1; // set flag for 100 Hz sample rates
  }

  Timer2_ticks++;		// increment slow counter
  if (Timer2_ticks > 360){ // 10 hz interrupt
    
    encoder1_speed = encoder1_count;
    if(encoder1_dir)
      encoder1_speed = -encoder1_speed;
    encoder1_count = 0;
    Timer2_ticks = 0;
  }
}


// --------------------------------------------------------------------------
// Init Timer0 with CLK/8 prescale. Thus 8 bit counter overflows
// at a rate of CLK/(8*256) = 7.37 Mhz/1024 = 3600 Hz. 
// So to get 100 hz interrupt rate, count 36 overflows

void Timer0_Init(void)
{
  Timer0_ticks = 0;
  Timer2_ticks = 0;
  Timer0_100hz_Flag = 0;
  
  //TCCR0 |= (_BV(CS01) | _BV(CS00)); /* Pre-scaler = 64 */
  TCCR0A = 0;          		/* Normal op; no output pins */
  TCCR0B |= (_BV(CS01));	/* Pre-scaler = 8 */
  TCNT0 = 0;			/* reset TCNT0 */
  TIMSK0 |= _BV(TOIE0);		/* enable Timer0 interrupts */
  /* debug */
  PORTB =0; // set bit 1;
}

// --------------------------------------------------------------------------

void Timer0_reset(void)
{
  TIMSK0 &= ~(_BV(TOIE0));	/* disable Timer0 interrupts */
  Timer0_ticks = 0;
  TIMSK0 |= _BV(TOIE0);		/* enable Timer0 interrupts */
}

// -----------------------------------------------------------------------

void Timer0_ON(void)
{
  TIMSK0 |= _BV(TOIE0);		/* enable Timer0 interrupts */
}

// -----------------------------------------------------------------------

void Timer0_OFF(void)
{
  TIMSK0 &= ~_BV(TOIE0);		/* disable Timer0 */
}




