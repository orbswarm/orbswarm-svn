#include "utils.h"

void dummy_spu_handler(char c, int n)
{
   if('B' == c)
    lightLedPortB6();
}


volatile char dummyPop(void){return 'A';}
//
int main(void)
{
  DDRB = 0xff;
  PORTB = 0xff;
  uart_init(dummy_spu_handler, 
	    dummy_spu_handler,
	    dummy_spu_handler,
	    //	    dummyPop());
	    popXbeeDataQ);
  //
  sei();
  //  lightLedPortB6();
    while(1)
      {
	//show that we are alive and looping
	char str[] = "hello";
	pushXbeeDataQ((char*)str);
	loopTimer0(2000);
	lightLedPortB6();
      }

}
//

