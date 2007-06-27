#include <avr/io.h>
#include <uart.h>
#include <spu.h>
#include <util/delay.h>

void lightLedPortB0(void)
{
  PORTB = PORTB  | (1 << PB0);
}

void lightLedPortB1(void)
{
  PORTB = PORTB | (1 << PB1);
}

int main(void)
{
  DDRB = 0xff;
  setupSpuCallbacks(lightLedPortB0, lightLedPortB1);
  uart_init(0, 
	    handleSpuSerialRx);
  while(1)
    {
      //show that we are alive and looping
      _delay_ms(1000);
      PORTB = PORTB ^ (1<<PB2);
    }
  return 0;
}

