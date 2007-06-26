//#include <xbee.h>
#include <uart.h>
#include <spu.h>

int main(void)
{
  uart_init(handleSpuSerialRx, 
	    handleSpuSerialRx);
  while(1)
    ;
  return 0;
}
