# Makefile for Illuminator Board -- atmega 8

# set chip target here
#TARGET=attiny2313
TARGET=atmega8

#set programmer here
PGMR = stk500v2 

# set avrdude COM port here
#COM = COM1
COM = COM5

BIN = C:\\WinAVR\\bin\\

# default target when "make" is run w/o arguments
all: illum.hex

# compile UART.c into UART.o
UART.o: UART.c UART.h
	${BIN}avr-gcc -c -g -O3 -Wall -mmcu=${TARGET} -I. UART.c -o UART.o

parser.o: parser.c parser.h illuminator.h
	${BIN}avr-gcc -c -g -O3 -Wall -mmcu=${TARGET} -I. parser.c -o parser.o

eeprom.o: eeprom.c illuminator.h
	${BIN}avr-gcc -c -g -O3 -Wall -mmcu=${TARGET} -I. eeprom.c -o eeprom.o

putstr.o: putstr.c putstr.h
	${BIN}avr-gcc -c -g -O3 -Wall -mmcu=${TARGET} -I. putstr.c -o putstr.o


main.o: main.c UART.h illuminator.h 
	${BIN}avr-gcc -c -g -O3 -Wall -mmcu=${TARGET} -I. main.c -o main.o

# link up objects into illum.elf
illum.elf: main.o UART.o  putstr.o parser.o eeprom.o
	${BIN}avr-gcc main.o UART.o  putstr.o parser.o eeprom.o -Wl,-Map=illum.map,--cref -mmcu=${TARGET} -o illum.elf

# copy ROM (FLASH) object out of illum.elf into illum.rom
illum.hex: illum.elf
	${BIN}avr-objcopy -O ihex illum.elf illum.hex

# command to program chip (optional) (invoked by running "make install")
install:
	${BIN}avrdude -P ${COM} -c ${PGMR}  -C  ${BIN}avrdude.conf  -p $(TARGET) -U flash:w:illum.hex


# set the fuses  for 7.32Mhz atmega8
lfuse:
	${BIN}avrdude.exe -v -P ${COM}  -c stk500v2  -p m8  -C  c:\WinAVR\bin\avrdude.conf  -U lfuse:w:0xEF:m

hfuse:
	${BIN}avrdude.exe -v -P ${COM}  -c stk500v2  -p m8  -C  c:\WinAVR\bin\avrdude.conf  -U hfuse:w:0xD9:m


# command to clean up junk (no source files) (invoked by "make clean")
clean:
	rm -f *.o *.hex *.elf *.map *~
