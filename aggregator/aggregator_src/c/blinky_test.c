//#include <timer0.h> 
//#include <xbee.h>
#include <util/delay.h>
#include <avr/io.h>

int main(void)
{

  DDRJ=0xff;
  //PORTJ=0xff;
  //Direction bit = PC1
  //PORTB = PORTB | (1<<PB5);
  PORTJ  = PORTJ | (1<<PJ3);
  
  while(1){
  }

}
