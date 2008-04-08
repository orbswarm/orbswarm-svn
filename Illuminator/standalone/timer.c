/* timer.c */

#include <avr/interrupt.h>  
#include <avr/io.h>
#include "timer.h"   

/* global variables */

volatile unsigned short Timer0_ticks;
volatile unsigned char Timer0_10hz_Flag;

// ---------------------------------------------------------------------
// Interrupt routine to handle Timer0 overflow

SIGNAL(SIG_OVERFLOW0){
  Timer0_ticks++;			// increment slow counter
  if ((Timer0_ticks % 36) == 0)		// roughly 10 hz
    Timer0_10hz_Flag = 1;	// set flag for 10 Hz sample rates
}

// ---------------------------------------------------------------------
// Init Timer0 so we get 512 tics per second from timer0_ticks
// 51 tics = 1/10 second.

void Timer0_Init(void)
{
  Timer0_ticks = 0;
  Timer0_10hz_Flag = 0;
  
  TCCR0 |= (_BV(CS01) | _BV(CS00)); /* Pre-scaler = 64 */
  //	TCCR0 |= (_BV(CS01) );      /* Pre-scaler = 8  */
  
  TCNT0 = 0;			/* reset TCNT0 */
  TIMSK |= _BV(TOIE0);		/* enable Timer0 interrupts */

  /* ok, that was timer 0. Now set up timer 2 for PWM */


  // Enable non inverting 8Bit PWM /
  // Timer Clock = system clock / 1
  // fast PWM, non-inverting
  // 64x prescale
  TCCR2 = _BV(COM21) | _BV(WGM21) | _BV(WGM20) | _BV(CS22);
  OCR2= 0xFF; // Set compare value/duty cycle ratio


  // Enable non inverting 8Bit PWM /
  // fast PWM, non-inverting
  // 64x prescale
  TCCR2 = _BV(COM21) | _BV(WGM21) | _BV(WGM20) | _BV(CS22);
  OCR2= 0xF0; // Set compare value/duty cycle ratio

  /* set up Timer 1A and 1B for non-inverting fast PWM */
  TCCR1A = _BV(COM1A1) | _BV(COM1B1) | _BV(WGM10) | _BV(CS22);

  /* fast PWM,  64x prescale */
  TCCR1B =   _BV(WGM12) | _BV(CS11) | _BV(CS10);

  OCR1AH=0; /* only using low 8 bits */

  OCR1BH=0; /* only using low 8 bits */
  OCR1AL=0x80; /* only using low 8 bits */
  OCR1AH=0; /* only using low 8 bits */
  OCR1AL=0x80; /* only using low 8 bits */



}

// --------------------------------------------------------

void Timer0_reset(void)
{
	TIMSK &= ~(_BV(TOIE0));					/* disable Timer0 interrupts */
	Timer0_ticks = 0;
	TIMSK |= _BV(TOIE0);					/* enable Timer0 interrupts */
}

// --------------------------------------------------------

void Timer0_ON(void)
{
  TIMSK |= _BV(TOIE0);					/* enable Timer0 interrupts */
}

// --------------------------------------------------------------------------------------------

void Timer0_OFF(void)
{
  TIMSK &= ~_BV(TOIE0);					/* disable Timer0 interrupts */
}

// ---------------------------------------------------------------------------------------------
// End of File
