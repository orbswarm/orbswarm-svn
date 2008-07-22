# Makefile for dboard_MCU v16+ - dboard Motor Control Unit
# Setup to run on ATMega8 chip
# Compiled using avr-gcc 4.02-1


# set avrdude COM port here
COM = COM5

#set avr executable path here
AVRPATH = C:\\WinAVR\\bin\\

# NOTE NEW TARGET! Not a atmega8 like old daughterboard
TARGET=atmega88
DUDETARGET=m88

# compile flags
GCCFLAGS = -c -g -O3

#set programmer here
PGMR = stk500v2 
#PGMR = usbtiny

# default target when "make" is run w/o arguments
all: dboard.hex

# compile UART.c into UART.o
UART.o: UART.c UART.h
	${AVRPATH}avr-gcc $(GCCFLAGS) -Wall -mmcu=$(TARGET) -I. UART.c -o UART.o

# compile putstr.c into putstr.o
putstr.o: putstr.c UART.h putstr.h
	${AVRPATH}avr-gcc $(GCCFLAGS) -Wall -mmcu=$(TARGET) -I. putstr.c -o putstr.o

# compile timer.c into timer.o
timer.o: timer.c timer.h
	${AVRPATH}avr-gcc $(GCCFLAGS) -Wall -mmcu=$(TARGET) -I. timer.c -o timer.o

# compile encoder.c into encoder.o
encoder.o: encoder.c encoder.h
	${AVRPATH}avr-gcc $(GCCFLAGS) -Wall -mmcu=$(TARGET) -I. encoder.c -o encoder.o

# compile a2d.c into a2d.o
a2d.o: a2d.c a2d.h
	${AVRPATH}avr-gcc $(GCCFLAGS) -Wall -mmcu=$(TARGET) -I. a2d.c -o a2d.o

# compile motor.c into motor.o
motor.o: motor.c encoder.h UART.h putstr.h motor.h 
	${AVRPATH}avr-gcc $(GCCFLAGS) -Wall -mmcu=$(TARGET) -I. motor.c -o motor.o


# compile steering.c into steering.o
steering.o: steering.c a2d.h motor.h steering.h 
	${AVRPATH}avr-gcc $(GCCFLAGS) -Wall -mmcu=$(TARGET) -I. steering.c -o steering.o

# compile IMU.c into IMU.o
IMU.o: IMU.c IMU.h a2d.h UART.h putstr.h encoder.h steering.h motor.h
	${AVRPATH}avr-gcc $(GCCFLAGS) -Wall -mmcu=$(TARGET) -I. IMU.c -o IMU.o

# compile main.c into main.o
main.o: main.c a2d.h putstr.h motor.h steering.h encoder.h timer.h IMU.h
	${AVRPATH}avr-gcc $(GCCFLAGS) -Wall -mmcu=$(TARGET) -I. main.c -o main.o

# link up obj files into dboard.elf
dboard.elf:  UART.o putstr.o timer.o encoder.o motor.o steering.o a2d.o IMU.o main.o
	${AVRPATH}avr-gcc  UART.o putstr.o timer.o encoder.o motor.o steering.o a2d.o IMU.o main.o -Wl,-Map=dboard.map,--cref -mmcu=$(TARGET) -o dboard.elf
	${AVRPATH}avr-size dboard.elf

# copy ROM (FLASH) object out of dboard.elf into dboard.rom
dboard.hex: dboard.elf
	${AVRPATH}avr-objcopy -O ihex dboard.elf dboard.hex




# command to program chip 
install:
	${AVRPATH}avrdude.exe -v -P ${COM} -c $(PGMR)  -p $(DUDETARGET)  -C  ${AVRPATH}avrdude.conf  -U flash:w:dboard.hex

# set the fuses (need only do this once....
lfuse:
	${AVRPATH}avrdude.exe -v -P ${COM}  -c $(PGMR)  -p $(DUDETARGET)  -C  ${AVRPATH}avrdude.conf  -U lfuse:w:0xEF:m

hfuse:
	${AVRPATH}avrdude.exe -v -P ${COM}  -c $(PGMR)  -p $(DUDETARGET)  -C  ${AVRPATH}avrdude.conf  -U hfuse:w:0xD9:m



# command to clean up junk (not source files) (invoked by "make clean")
clean:
	rm -f *.o *.rom *.elf *.map *~
