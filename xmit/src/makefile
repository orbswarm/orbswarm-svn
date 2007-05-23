# Makefile for Orb_Xmit v14 - Orb Maker Faire Transmitter Unit
# Setup to run on ATMeag8 chip
# Compiled using avr-gcc 4.02-1

# default target when "make" is run w/o arguments
all: Orb_xmit.rom

# compile eprom.c into eprom.o
eprom.o: eprom.c eprom.h
	avr-gcc -c -g -O3 -Wall -mmcu=atmega8 -I. eprom.c -o eprom.o

# compile UART.c into UART.o
UART.o: UART.c UART.h
	avr-gcc -c -g -O3 -Wall -mmcu=atmega8 -I. UART.c -o UART.o

# compile putstr.c into putstr.o
putstr.o: putstr.c UART.h putstr.h
	avr-gcc -c -g -O3 -Wall -mmcu=atmega8 -I. putstr.c -o putstr.o

# compile timer.c into timer.o
timer.o: timer.c timer.h
	avr-gcc -c -g -O3 -Wall -mmcu=atmega8 -I. timer.c -o timer.o

# compile a2d.c into a2d.o
a2d.o: a2d.c a2d.h
	avr-gcc -c -g -O3 -Wall -mmcu=atmega8 -I. a2d.c -o a2d.o

# compile main.c into main.o
main.o: main.c eprom.h a2d.h putstr.h timer.h
	avr-gcc -c -g -O3 -Wall -mmcu=atmega8 -I. main.c -o main.o

# link up obj files into Orb_xmit.elf
Orb_xmit.elf: UART.o putstr.o timer.o eprom.o a2d.o main.o
	avr-gcc UART.o putstr.o timer.o eprom.o a2d.o main.o -Wl,-Map=Orb_xmit.map,--cref -mmcu=atmega8 -o Orb_xmit.elf

# copy ROM (FLASH) object out of Orb_xmit.elf into Orb_xmit.rom
Orb_xmit.rom: Orb_xmit.elf
	avr-objcopy -O ihex Orb_xmit.elf Orb_xmit.rom

# command to program chip 
install:
	avrdude -p m8 -U flash:w:Orb_xmit.rom

# command to set fuses for 8.0 Mhz internal osc
fuse:
	avrdude -p m8 -U lfuse:w:0xE4:m

# command to clean up junk (not source files) (invoked by "make clean")
clean:
	rm -f *.o *.rom *.elf *.map *~
