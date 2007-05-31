#include "timer0.h"

int main(void)
{
  DDRB=0xff;
  //PORTB= 1<<PB5;
  DDRC=0xff;
  PORTC=0;
  //Direction bit = PC1
  //PORTC = PORTC | (1<<PC1);
  
  //Chip select = PC2
  //PORTC = PORTC | (1<<PC2);
  int i;
  for(i=0; i < 100; i++)
    {
      PORTB=PORTB ^ (1<<PB5);
      PORTC=PORTC | 1<<PC0;
      loopTimer0(2000);
      PORTB=PORTB ^ (1<<PB5);
      PORTC=PORTC & (~(1<<PC0));
      loopTimer0(2000);
    }
  //PORTB=0;
  while(1)
    ;
  
}
