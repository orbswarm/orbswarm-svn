// -----------------------------------------------------------------------
// 
//	File: address.c
//	eeprom code for SWARM Orb LED Illumination Unit http://www.orbswarm.com
//      hard-coded address
//      use gcc -DADDRESS=N to set address to N (see makefile)
// -----------------------------------------------------------------------

/* address byte to store address in eeprom */
//#define ADDRESS 0
#ifndef ADDRESS
#warning "ERROR: ADDRESS UNDEFINED  Use -D option in avr-gcc to set address"
#else

/* save current address to eeprom */
unsigned char getAddress(void){
  return((unsigned char) ADDRESS);
}

#endif

