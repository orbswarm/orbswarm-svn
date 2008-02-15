# Makefile for sample - AVR Sample code

AVRPATH = C:\\WinAVR\\bin\\
COM = COM5

# default target when "make" is run w/o arguments
all: soundmod.hex

# compile UART.c into UART.o
UART.o: UART.c UART.h
	${AVRPATH}avr-gcc -c -g -O3 -Wall -mmcu=attiny2313 -I. UART.c -o UART.o

# compile USI_UART.c into USI_UART.o
USI_UART.o: USI_UART.c USI_UART_config.h
	${AVRPATH}avr-gcc -c -g -O3 -Wall -mmcu=attiny2313 -I. USI_UART.c -o USI_UART.o

# compile putstr.c into putstr.o
putstr.o: putstr.c putstr.h
	${AVRPATH}avr-gcc -c -g -O3 -Wall -mmcu=attiny2313 -I. putstr.c -o putstr.o

# compile main.c into soundmod.o
main.o: main.c USI_UART_config.h UART.h
	${AVRPATH}avr-gcc -c -g -O3 -Wall -mmcu=attiny2313 -I. main.c -o main.o

# link up objects into soundmod.elf
soundmod.elf: main.o UART.o USI_UART.o putstr.o
	${AVRPATH}avr-gcc main.o UART.o USI_UART.o putstr.o -Wl,-Map=soundmod.map,--cref -mmcu=attiny2313 -o soundmod.elf

# copy ROM (FLASH) object out of soundmod.elf into soundmod.rom
soundmod.hex: soundmod.elf
	${AVRPATH}avr-objcopy -O ihex soundmod.elf soundmod.hex

# command to program chip (optional) (invoked by running "make install")
install:
	${AVRPATH}avrdude -P ${COM} -c stk500v2  -C  ${AVRPATH}avrdude.conf  -p t2313 -U flash:w:soundmod.hex 

# command to set fuse for 8.0 Mhz internal Osc on AT-Tiny2313
internalosc:
	${AVRPATH}avrdude -p t2313 -U lfuse:w:0xE4:m

# command to set fuse for 7,32Mhz external xtal Osc on AT-Tiny2313
732xtal:
	${AVRPATH}avrdude -p t2313 -U lfuse:w:0xDD:m

# command to clean up junk (no source files) (invoked by "make clean")
clean:
	rm -f *.o *.hex *.elf *.map *~
