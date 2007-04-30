# Makefile for orb_ESC v10 - Orb Electronic Speed Control code
# Setup to run on ATMeag8 chip
# Compiled using avr-gcc 4.02-1

# default target when "make" is run w/o arguments
all: orb_ESC.rom

# compile UART.c into UART.o
UART.o: UART.c UART.h
	avr-gcc -c -g -O3 -Wall -mmcu=atmega8 -I. UART.c -o UART.o

# compile eprom.c into eprom.o
eprom.o: eprom.c eprom.h
	avr-gcc -c -g -O3 -Wall -mmcu=atmega8 -I. eprom.c -o eprom.o

# compile putstr.c into putstr.o
putstr.o: putstr.c UART.h putstr.h
	avr-gcc -c -g -O3 -Wall -mmcu=atmega8 -I. putstr.c -o putstr.o

# compile timer.c into timer.o
timer.o: timer.c timer.h
	avr-gcc -c -g -O3 -Wall -mmcu=atmega8 -I. timer.c -o timer.o

# compile encoder.c into encoder.o
encoder.o: encoder.c encoder.h
	avr-gcc -c -g -O3 -Wall -mmcu=atmega8 -I. encoder.c -o encoder.o

# compile a2d.c into a2d.o
a2d.o: a2d.c a2d.h
	avr-gcc -c -g -O3 -Wall -mmcu=atmega8 -I. a2d.c -o a2d.o

# compile motor.c into motor.o
motor.o: motor.c encoder.h UART.h putstr.h motor.h eprom.h
	avr-gcc -c -g -O3 -Wall -mmcu=atmega8 -I. motor.c -o motor.o

# compile steering.c into steering.o
steering.o: steering.c a2d.h motor.h steering.h eprom.h
	avr-gcc -c -g -O3 -Wall -mmcu=atmega8 -I. steering.c -o steering.o

# compile main.c into main.o
main.o: main.c a2d.h putstr.h motor.h steering.h encoder.h timer.h eprom.h
	avr-gcc -c -g -O3 -Wall -mmcu=atmega8 -I. main.c -o main.o

# link up obj files into orb_ESC.elf
orb_ESC.elf: eprom.o UART.o putstr.o timer.o encoder.o motor.o steering.o a2d.o main.o
	avr-gcc eprom.o UART.o putstr.o timer.o encoder.o motor.o steering.o a2d.o main.o -Wl,-Map=orb_ESC.map,--cref -mmcu=atmega8 -o orb_ESC.elf

# copy ROM (FLASH) object out of orb_ESC.elf into orb_ESC.rom
orb_ESC.rom: orb_ESC.elf
	avr-objcopy -O ihex orb_ESC.elf orb_ESC.rom

# command to program chip 
install:
	avrdude -p m8 -U flash:w:orb_ESC.rom

# command to clean up junk (not source files) (invoked by "make clean")
clean:
	rm -f *.o *.rom *.elf *.map *~
