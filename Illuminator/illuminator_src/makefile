# Makefile for sample - AVR Sample code

# set chip target here
#TARGET=attiny2313
TARGET=atmega8

#set programmer here
PGMR = stk500v2 

# set avrdude COM port here
#COM = COM1
COM = COM4

# default target when "make" is run w/o arguments
all: sample.rom

# compile UART.c into UART.o
UART.o: UART.c UART.h
	avr-gcc -c -g -O3 -Wall -mmcu=${TARGET} -I. UART.c -o UART.o

parser.o: parser.c parser.h illuminator.h
	avr-gcc -c -g -O3 -Wall -mmcu=${TARGET} -I. parser.c -o parser.o

# compile USI_UART.c into USI_UART.o
#USI_UART.o: USI_UART.c USI_UART_config.h
#	avr-gcc -c -g -O3 -Wall -mmcu=${TARGET} -I. USI_UART.c -o USI_UART.o

# compile putstr.c into putstr.o
putstr.o: putstr.c putstr.h
	avr-gcc -c -g -O3 -Wall -mmcu=${TARGET} -I. putstr.c -o putstr.o

# compile main.c into sample.o
main.o: main.c UART.h illuminator.h 
	avr-gcc -c -g -O3 -Wall -mmcu=${TARGET} -I. main.c -o main.o

# link up objects into sample.elf
sample.elf: main.o UART.o  putstr.o parser.o
	avr-gcc main.o UART.o  putstr.o parser.o -Wl,-Map=sample.map,--cref -mmcu=${TARGET} -o sample.elf

# copy ROM (FLASH) object out of sample.elf into sample.rom
sample.rom: sample.elf
	avr-objcopy -O ihex sample.elf sample.rom

# command to program chip (optional) (invoked by running "make install")
install:
	avrdude -P ${COM} -c ${PGMR}  -C  c:\WinAVR\bin\avrdude.conf  -p $(TARGET) -U flash:w:sample.rom 

# command to set fuse for 8.0 Mhz internal Osc on AT-Tiny2313
tinyInt:
	avrdude ${COM} -p t2313 -U lfuse:w:0xE4:m

# command to set fuse for 7.32Mhz external xtal Osc on AT-Tiny2313
tinyXtal:
	avrdude -p t2313 -U lfuse:w:0xDD:m

# set the fuses  for 7.32Mhz atmega8
atmegaLfuse:
	avrdude.exe -v -P ${COM}  -c stk500v2  -p m8  -C  c:\WinAVR\bin\avrdude.conf  -U lfuse:w:0xEF:m

atmegahfuse:
	avrdude.exe -v -P ${COM}  -c stk500v2  -p m8  -C  c:\WinAVR\bin\avrdude.conf  -U hfuse:w:0xD9:m


# command to clean up junk (no source files) (invoked by "make clean")
clean:
	rm -f *.o *.rom *.elf *.map *~
