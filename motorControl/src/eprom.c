/* eprom.c */
// 
// Routines for accessing eeprom memory on ATMega8 chip
//
// Named eprom 'cause there are a bunch of eeprom.h files out there.
//
// Petey the Programmer


#include <avr/io.h>
#include <avr/interrupt.h>
#include "eprom.h"

// --------------------------------------------------------------------------------------------------------------------------------------------------------------------------
// There are 512 bytes of EEPROM memory available on ATMega8
// Enter with Addr in [0..511]

void eeprom_Write(unsigned short addr, unsigned char val)
{
    while(EECR & (1 << EEWE));
    EEAR = addr;
    EEDR = val;
    cli();					// turn OFF interrupts
    EECR |= (1 << EEMWE);	// setup to write to eeprom
    EECR |= (1 << EEWE);	// must follow within 4 cycles -- therefore cli()
    sei();					// turn interrupts back ON
}

unsigned char eeprom_Read(unsigned short addr)
{
    while(EECR & (1 << EEWE));
    EEAR = addr;
    EECR |= (1<<EERE);
    return EEDR;
}

// ------------------------------------------------------------------------------------------------------------------------------------------------------------
// End of File
