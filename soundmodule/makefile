# Makefile for sample - AVR Sample code

# default target when "make" is run w/o arguments
all: sample.rom

# compile UART.c into UART.o
UART.o: UART.c UART.h
	avr-gcc -c -g -O3 -Wall -mmcu=attiny2313 -I. UART.c -o UART.o

# compile USI_UART.c into USI_UART.o
USI_UART.o: USI_UART.c USI_UART_config.h
	avr-gcc -c -g -O3 -Wall -mmcu=attiny2313 -I. USI_UART.c -o USI_UART.o

# compile putstr.c into putstr.o
putstr.o: putstr.c putstr.h
	avr-gcc -c -g -O3 -Wall -mmcu=attiny2313 -I. putstr.c -o putstr.o

# compile main.c into sample.o
main.o: main.c USI_UART_config.h UART.h
	avr-gcc -c -g -O3 -Wall -mmcu=attiny2313 -I. main.c -o main.o

# link up objects into sample.elf
sample.elf: main.o UART.o USI_UART.o putstr.o
	avr-gcc main.o UART.o USI_UART.o putstr.o -Wl,-Map=sample.map,--cref -mmcu=attiny2313 -o sample.elf

# copy ROM (FLASH) object out of sample.elf into sample.rom
sample.rom: sample.elf
	avr-objcopy -O ihex sample.elf sample.rom

# command to program chip (optional) (invoked by running "make install")
install:
	avrdude -P COM1 -c stk500v2  -C  c:\WinAVR\bin\avrdude.conf  -p t2313 -U flash:w:sample.rom 

# command to set fuse for 8.0 Mhz internal Osc on AT-Tiny2313
internalosc:
	avrdude -p t2313 -U lfuse:w:0xE4:m

# command to set fuse for 7,32Mhz external xtal Osc on AT-Tiny2313
732xtal:
	avrdude -p t2313 -U lfuse:w:0xDD:m

# command to clean up junk (no source files) (invoked by "make clean")
clean:
	rm -f *.o *.rom *.elf *.map *~
