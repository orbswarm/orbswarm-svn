#include "timer0.h"

volatile uint16_t m_unitsOf1ms=0;
volatile uint16_t timecount=0;

ISR(SIG_OVERFLOW0)
{
  TCNT0=6;
  if(++timecount == m_unitsOf1ms)
    {
      //disable timer we are done
      TIMSK = TIMSK & (~(1<<TOIE0));
    }
}


int loopTimer0(uint16_t units1ms)
{
  cli();
  timecount=0;
  m_unitsOf1ms=units1ms;
  TCNT0=6;
  TCCR0 = TCCR0 | ((1<< CS01)|(1<<CS00))  ;
  TIMSK=TIMSK | (1<<TOIE0);
  sei();
  while(timecount<m_unitsOf1ms)
    ;
  return 0;
}

