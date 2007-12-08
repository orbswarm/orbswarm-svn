#include "include/timer0.h"

volatile uint16_t m_unitsOf1ms=0;
volatile uint16_t timecount=0;

ISR(SIG_OVERFLOW0)
{
  TCNT0=26;
  if(++timecount == m_unitsOf1ms)
    {
      //disable timer we are done
      TIMSK0 = TIMSK0 & (~(1<<TOIE0));
    }
}


int loopTimer0(uint16_t units1ms)
{
  cli();
  timecount=0;
  m_unitsOf1ms=units1ms;
  TCNT0=26;
  //TCCR0A = TCCR0A | ((1<< CS01)|(1<<CS00))  ;
  TCCR0B = TCCR0B | ((1<<CS01) | (1<<CS00));
  TIMSK0= TIMSK0 | (1<<TOIE0);
  sei();
  while(timecount<m_unitsOf1ms)
    ;
  return 0;
}

