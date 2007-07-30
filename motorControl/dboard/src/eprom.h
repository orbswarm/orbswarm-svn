// eprom.h

#define MOTOR_EEPROM	1
#define STEER_EEPROM	10

void eeprom_Write(unsigned short addr, unsigned char val);
unsigned char eeprom_Read(unsigned short addr);
