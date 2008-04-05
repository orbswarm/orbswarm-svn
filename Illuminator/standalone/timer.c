/* timer.c */

#include <avr/interrupt.h>  
#include <avr/io.h>
#include "timer.h"   

/* global variables */

volatile unsigned short Timer0_ticks;
volatile unsigned char Timer0_10hz_Flag;

// ------------------------------------------------------------------------------------
// Interrupt routine to handle Timer0 overflow

SIGNAL(SIG_OVERFLOW0)
{
	Timer0_ticks++;						// increment slow counter
	if ((Timer0_ticks % 51) == 0)		// flag on 510, not on zero
//	if ((Timer0_ticks % 102) == 0)		// flag on 510, not on zero
		Timer0_10hz_Flag = 1;			// set flag for 10 Hz sample rates
}

// ----------------------------------------------------------------------------------------
// Init Timer0 so we get 512 tics per second from timer0_ticks
// 51 tics = 1/10 second.

void Timer0_Init(void)
{
	Timer0_ticks = 0;
	Timer0_10hz_Flag = 0;

	TCCR0 |= (_BV(CS01) | _BV(CS00));		/* Pre-scaler = 64 */
//	TCCR0 |= (_BV(CS01) );					/* Pre-scaler = 8  */
	
	TCNT0 = 0;								/* reset TCNT0 */
	TIMSK |= _BV(TOIE0);					/* enable Timer0 interrupts */
}

// -----------------------------------------------------------------------------------------

void Timer0_reset(void)
{
	TIMSK &= ~(_BV(TOIE0));					/* disable Timer0 interrupts */
	Timer0_ticks = 0;
	TIMSK |= _BV(TOIE0);					/* enable Timer0 interrupts */
}

// --------------------------------------------------------------------------------------------

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
