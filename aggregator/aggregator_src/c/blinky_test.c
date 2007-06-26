#include <timer0.h> 
#include <xbee.h>

int main(void)
{

  DDRB=0xff;
  PORTB=0xff;
  //Direction bit = PC1
  //PORTB = PORTB | (1<<PB5);
  
  while(1){
  	loopTimer0(1000u);
  	PORTB = PORTB ^ (1<<PB5);
  }

}
