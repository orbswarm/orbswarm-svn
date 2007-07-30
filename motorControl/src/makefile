# Makefile for Orb_MCU v14 - Orb Motor Control Unit
# Setup to run on ATMeag8 chip
# Compiled using avr-gcc 4.02-1

# set avrdude COM port here
COM = COM6


# default target when "make" is run w/o arguments
all: Orb_MCU.rom

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

# compile IMU.c into IMU.o
IMU.o: IMU.c IMU.h a2d.h UART.h putstr.h encoder.h steering.h motor.h
	avr-gcc -c -g -O3 -Wall -mmcu=atmega8 -I. IMU.c -o IMU.o

# compile main.c into main.o
main.o: main.c a2d.h putstr.h motor.h steering.h encoder.h timer.h eprom.h IMU.h
	avr-gcc -c -g -O3 -Wall -mmcu=atmega8 -I. main.c -o main.o

# link up obj files into Orb_MCU.elf
Orb_MCU.elf: eprom.o UART.o putstr.o timer.o encoder.o motor.o steering.o a2d.o IMU.o main.o
	avr-gcc eprom.o UART.o putstr.o timer.o encoder.o motor.o steering.o a2d.o IMU.o main.o -Wl,-Map=Orb_MCU.map,--cref -mmcu=atmega8 -o Orb_MCU.elf

# copy ROM (FLASH) object out of Orb_MCU.elf into Orb_MCU.rom
Orb_MCU.rom: Orb_MCU.elf
	avr-objcopy -O ihex Orb_MCU.elf Orb_MCU.rom

# command to program chip 

install:
	avrdude.exe -v -P ${COM} -c stk500v2  -p m8  -C  c:\WinAVR\bin\avrdude.conf  -U flash:w:Orb_MCU.rom

# set the fuses (need only do this once....
lfuse:
	avrdude.exe -v -P ${COM}  -c stk500v2  -p m8  -C  c:\WinAVR\bin\avrdude.conf  -U lfuse:w:0xEF:m

hfuse:
	avrdude.exe -v -P ${COM}  -c stk500v2  -p m8  -C  c:\WinAVR\bin\avrdude.conf  -U hfuse:w:0xD9:m


# command to clean up junk (not source files) (invoked by "make clean")
clean:
	rm -f *.o *.rom *.elf *.map *~
