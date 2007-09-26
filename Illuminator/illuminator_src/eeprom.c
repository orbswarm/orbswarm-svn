// -----------------------------------------------------------------------
// 
//	File: eeprom.c
//	eeprom code for SWARM Orb LED Illumination Unit http://www.orbswarm.com
//      saves and restores board address to eeprom
//      build code using WinAVR toolchain: see makefile
//
//	Written by Jonathan Foote (Head Rotor at rotorbrain.com)
//      based on code by Petey the Programmer
// -----------------------------------------------------------------------
#include <avr/interrupt.h>
#include "illuminator.h"

/* address byte to store address in eeprom */
#define ADDRESS_EEPROM 1

/* save current address to eeprom */
void writeAddressEEPROM(unsigned char addr){
  while(EECR & (1 << EEWE));
  EEAR = ADDRESS_EEPROM;
  EEDR = addr;
  cli();			// turn OFF interrupts
  EECR |= (1 << EEMWE);	// setup to write to eeprom
  EECR |= (1 << EEWE);	// must follow within 4 cycles--therefore cli()
  sei();			// turn interrupts back ON
}

/* read address from eeprom */
unsigned char readAddressEEPROM(void){
  while(EECR & (1 << EEWE));
  EEAR = ADDRESS_EEPROM;
  EECR |= (1<<EERE);
  return( (unsigned char) EEDR);
}

