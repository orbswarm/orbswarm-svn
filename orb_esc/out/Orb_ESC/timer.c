/* timer.c */

#include <avr/signal.h>  
#include <avr/io.h>
#include "timer.h"   

/* global variables */

volatile unsigned short Timer0_ticks;
volatile unsigned char Timer0_10hz_Flag;

// ------------------------------------------------------------------------------------------------------------------------------------------------------------
// Interupt routine to handle Timer0 overflow

SIGNAL(SIG_OVERFLOW0)
{
	Timer0_ticks++;						// increment slow counter
	if ((Timer0_ticks % 51) == 0)		// flag on 510, not on zero
		Timer0_10hz_Flag = 1;			// set flag for 10 Hz sample rates
}

// ------------------------------------------------------------------------------------------------------------------------------------------------------------
// Init Timer0 so we get 512 tics per second from timer0_ticks
// 51 tics = 1/10 second.

void Timer0_Init(void)
{
	Timer0_ticks = 0;
	Timer0_10hz_Flag = 0;

	TCCR0 |= ((1<<CS01) | (1<<CS00));		/* Pre-scaler = 64 */
	TCNT0 = 0;								/* reset TCNT0 */
	TIMSK |= (1<<TOIE0);					/* enable Timer0 interrupts */
}

// ------------------------------------------------------------------------------------------------------------------------------------------------------------

void Timer0_reset(void)
{
	TIMSK &= ~(1<<TOIE0);					/* disable Timer0 interrupts */
	Timer0_ticks = 0;
	//  TCNT0 = 0;							/* reset TCNT0 */
	TIMSK |= (1<<TOIE0);					/* enable Timer0 interrupts */
}

// ------------------------------------------------------------------------------------------------------------------------------------------------------------

void Timer0_ON(void)
{
  TIMSK |= (1<<TOIE0);					/* enable Timer0 interrupts */
}

// ------------------------------------------------------------------------------------------------------------------------------------------------------------

void Timer0_OFF(void)
{
  TIMSK &= ~(1<<TOIE0);					/* disable Timer0 interrupts */
}

// ------------------------------------------------------------------------------------------------------------------------------------------------------------
// End of File
