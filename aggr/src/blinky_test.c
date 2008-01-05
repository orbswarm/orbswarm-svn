#include <avr/io.h>      // this contains all the IO port definitions
#include <avr/interrupt.h>
#include "include/timer0.h"

int main(void)
{

  DDRB=0xff;
  //PORTJ=0xff;
  //Direction bit = PC1
  //PORTB = PORTB | (1<<PB5);
  //PORTJ  = PORTJ | (1<<PJ3);
   PORTB = PORTB ^ (1<<PB6);
   sei();
   while(1){
    loopTimer0(1000);
    PORTB = PORTB ^ (1<<PB6);
  }

}
