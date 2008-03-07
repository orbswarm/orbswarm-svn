#include <avr/io.h>      // this contains all the IO port definitions
#include <avr/interrupt.h>
#include <stdint.h> 
#include "include/timer0.h"

volatile uint16_t m_unitsOf1ms=0;
volatile uint16_t m_nTimecount=0;
volatile uint16_t m_nQuarterTick=0;
volatile uint16_t m_nTimerOverFlag=0;

ISR(SIG_OVERFLOW0)
{
  //TCNT0=26;
  TCNT0=6;
  if(++m_nQuarterTick == 4)
    {
    	if(++m_nTimecount == m_unitsOf1ms)
    	{
    		m_nTimerOverFlag=1;
    		TIMSK0 = TIMSK0 & (~(1<<TOIE0));
    	}
    	else
    		m_nQuarterTick=0;
    }
}

int isTimedOut(void){return m_nTimerOverFlag;}

void setTimerInAsync(uint16_t units1ms)
{
  //disable timer first
  TIMSK0 = TIMSK0 & (~(1<<TOIE0));
  m_nTimecount=0;
  m_nQuarterTick=0;
  m_unitsOf1ms=units1ms;
  //TCNT0=26;
  TCNT0=6;
  TCCR0B = TCCR0B | (1<<CS01);
  //TCCR0B = TCCR0B | ((1<<CS01) | (1<<CS00));
  m_nTimerOverFlag=0;
  TIMSK0= TIMSK0 | (1<<TOIE0);
}

int loopTimer0(uint16_t units1ms)
{
  //cli();
  //disable timer first
  TIMSK0 = TIMSK0 & (~(1<<TOIE0));
  m_nTimecount=0;
  m_nQuarterTick=0;
  m_unitsOf1ms=units1ms;
  //TCNT0=26;
  TCNT0=6;
  TCCR0B = TCCR0B | (1<<CS01);
  //TCCR0B = TCCR0B | ((1<<CS01) | (1<<CS00));
  TIMSK0= TIMSK0 | (1<<TOIE0);
  //sei();
  while(m_nTimecount < m_unitsOf1ms)
    ;
  return 0;
}

