#include <avr/io.h>      // this contains all the IO port definitions
#include <avr/interrupt.h>
#include <stdint.h> 
#include "../timer0.c"

int loopTimer0(uint16_t unitsOf1ms);
