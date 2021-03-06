# Makefile for dboard_MCU v16+ - dboard Motor Control Unit
# Setup to run on ATMega8 chip
# Compiled using avr-gcc 4.02-1


# set avrdude COM port here
COM = COM5

#set avr executable path here
AVRPATH = C:\\WinAVR\\bin\\

# default target when "make" is run w/o arguments
all: dboard_MCU.rom

# compile UART.c into UART.o
UART.o: UART.c UART.h
	${AVRPATH}avr-gcc -c -g -O3 -Wall -mmcu=atmega8 -I. UART.c -o UART.o

# compile eprom.c into eprom.o
eprom.o: eprom.c eprom.h
	${AVRPATH}avr-gcc -c -g -O3 -Wall -mmcu=atmega8 -I. eprom.c -o eprom.o

# compile putstr.c into putstr.o
putstr.o: putstr.c UART.h putstr.h
	${AVRPATH}avr-gcc -c -g -O3 -Wall -mmcu=atmega8 -I. putstr.c -o putstr.o

# compile timer.c into timer.o
timer.o: timer.c timer.h
	${AVRPATH}avr-gcc -c -g -O3 -Wall -mmcu=atmega8 -I. timer.c -o timer.o

# compile encoder.c into encoder.o
encoder.o: encoder.c encoder.h
	${AVRPATH}avr-gcc -c -g -O3 -Wall -mmcu=atmega8 -I. encoder.c -o encoder.o

# compile a2d.c into a2d.o
a2d.o: a2d.c a2d.h
	${AVRPATH}avr-gcc -c -g -O3 -Wall -mmcu=atmega8 -I. a2d.c -o a2d.o

# compile motor.c into motor.o
motor.o: motor.c encoder.h UART.h putstr.h motor.h eprom.h
	${AVRPATH}avr-gcc -c -g -O3 -Wall -mmcu=atmega8 -I. motor.c -o motor.o


# compile steering.c into steering.o
steering.o: steering.c a2d.h motor.h steering.h eprom.h
	${AVRPATH}avr-gcc -c -g -O3 -Wall -mmcu=atmega8 -I. steering.c -o steering.o

# compile IMU.c into IMU.o
IMU.o: IMU.c IMU.h a2d.h UART.h putstr.h encoder.h steering.h motor.h
	${AVRPATH}avr-gcc -c -g -O3 -Wall -mmcu=atmega8 -I. IMU.c -o IMU.o

# compile main.c into main.o
main.o: main.c a2d.h putstr.h motor.h steering.h encoder.h timer.h eprom.h IMU.h
	${AVRPATH}avr-gcc -c -g -O3 -Wall -mmcu=atmega8 -I. main.c -o main.o

# link up obj files into dboard_MCU.elf
dboard_MCU.elf: eprom.o UART.o putstr.o timer.o encoder.o motor.o steering.o a2d.o IMU.o main.o
	${AVRPATH}avr-gcc eprom.o UART.o putstr.o timer.o encoder.o motor.o steering.o a2d.o IMU.o main.o -Wl,-Map=dboard_MCU.map,--cref -mmcu=atmega8 -o dboard_MCU.elf

# copy ROM (FLASH) object out of dboard_MCU.elf into dboard_MCU.rom
dboard_MCU.rom: dboard_MCU.elf
	${AVRPATH}avr-objcopy -O ihex dboard_MCU.elf dboard_MCU.rom




# command to program chip 
install:
	${AVRPATH}avrdude.exe -v -P ${COM} -c stk500v2  -p m8  -C  ${AVRPATH}avrdude.conf  -U flash:w:dboard_MCU.rom

# set the fuses (need only do this once....
lfuse:
	${AVRPATH}avrdude.exe -v -P ${COM}  -c stk500v2  -p m8  -C  ${AVRPATH}avrdude.conf  -U lfuse:w:0xEF:m

hfuse:
	${AVRPATH}avrdude.exe -v -P ${COM}  -c stk500v2  -p m8  -C  ${AVRPATH}avrdude.conf  -U hfuse:w:0xD9:m



# command to clean up junk (not source files) (invoked by "make clean")
clean:
	rm -f *.o *.rom *.elf *.map *~
