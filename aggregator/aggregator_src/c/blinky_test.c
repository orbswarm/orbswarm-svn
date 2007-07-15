//#include <timer0.h> 
//#include <xbee.h>
#include <util/delay.h>
#include <avr/io.h>

int main(void)
{

  DDRB=0xff;
  PORTB=0xff;
  //Direction bit = PC1
  //PORTB = PORTB | (1<<PB5);
  
  while(1){
    loopTimer0(1000u);
    /*
    for(int i=0; i < 5; i++)
      _delay_ms(200);
    */
    PORTB = PORTB ^ (1<<PB7);
  }

}
