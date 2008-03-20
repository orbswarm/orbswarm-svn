// eprom.h

#define EEPROM_START	1

void eeprom_Write(unsigned short addr, unsigned char val);
unsigned char eeprom_Read(unsigned short addr);
