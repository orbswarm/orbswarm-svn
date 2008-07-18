# Makefile for Orb_Xmit v14 - Orb Maker Faire Transmitter Unit
# Setup to run on ATMeag8 chip
# Compiled using avr-gcc 4.02-1

# set chip target here
#TARGET=attiny2313
TARGET=atmega8

#set programmer here
PGMR = stk500v2 

# set avrdude COM port here

COM = COM5
GCCFLAGS = -c -Os

BIN = C:\\WinAVR\\bin\\

# default target when "make" is run w/o arguments
all: remote.hex

# compile eprom.c into eprom.o
eprom.o: eprom.c eprom.h
	${BIN}avr-gcc ${GCCFLAGS} -Wall -mmcu=atmega8 -I. eprom.c -o eprom.o

# compile UART.c into UART.o
UART.o: UART.c UART.h
	${BIN}avr-gcc ${GCCFLAGS} -Wall -mmcu=atmega8 -I. UART.c -o UART.o

# compile putstr.c into putstr.o
putstr.o: putstr.c UART.h putstr.h
	${BIN}avr-gcc ${GCCFLAGS} -Wall -mmcu=atmega8 -I. putstr.c -o putstr.o

# compile timer.c into timer.o
timer.o: timer.c timer.h
	${BIN}avr-gcc ${GCCFLAGS} -Wall -mmcu=atmega8 -I. timer.c -o timer.o

# compile a2d.c into a2d.o
a2d.o: a2d.c a2d.h
	${BIN}avr-gcc ${GCCFLAGS} -Wall -mmcu=atmega8 -I. a2d.c -o a2d.o

# compile main.c into main.o
main.o: main.c eprom.h a2d.h putstr.h timer.h
	${BIN}avr-gcc ${GCCFLAGS} -Wall -mmcu=atmega8 -I. main.c -o main.o

debug.o: main.c eprom.h a2d.h putstr.h timer.h
	${BIN}avr-gcc -DDEBUG ${GCCFLAGS} -Wall -mmcu=atmega8 -I. main.c -o debug.o

# compile main.c into main.o
standalone.o: standalone.c eprom.h a2d.h putstr.h timer.h
	${BIN}avr-gcc ${GCCFLAGS} -Wall -mmcu=atmega8 -I. standalone.c -o standalone.o

# link up obj files into remote.elf
remote.hex: UART.o putstr.o timer.o eprom.o a2d.o main.o
	${BIN}avr-gcc UART.o putstr.o timer.o eprom.o a2d.o main.o -Wl,-Map=remote.map,--cref -mmcu=atmega8 -o remote.elf
	${BIN}avr-size remote.elf
	${BIN}avr-objcopy -O ihex remote.elf remote.hex 

debug.hex: UART.o putstr.o timer.o eprom.o a2d.o debug.o
	${BIN}avr-gcc UART.o putstr.o timer.o eprom.o a2d.o debug.o -Wl,-Map=remote.map,--cref -mmcu=atmega8 -o debug.elf
	${BIN}avr-size debug.elf
	${BIN}avr-objcopy -O ihex debug.elf debug.hex 

controller.hex: UART.o putstr.o timer.o eprom.o a2d.o standalone.o
	${BIN}avr-gcc UART.o putstr.o timer.o eprom.o a2d.o standalone.o -Wl,-Map=remote.map,--cref -mmcu=atmega8 -o controller.elf
	${BIN}avr-size controller.elf
	${BIN}avr-objcopy -O ihex controller.elf controller.hex

# command to program chip 
install:
	${BIN}avrdude -v -P ${COM} -c ${PGMR}  -C  ${BIN}avrdude.conf  -p ${TARGET} -U flash:w:remote.hex

debug:
	${BIN}avrdude -v -P ${COM} -c ${PGMR}  -C  ${BIN}avrdude.conf  -p ${TARGET} -U flash:w:debug.hex

#code for standalone controller. 
control: 
	${BIN}avrdude -v -P ${COM} -c ${PGMR}  -C  ${BIN}avrdude.conf  -p ${TARGET} -U flash:w:controller.hex

# set the fuses  for 7.32Mhz atmega8
lfuse:
	${BIN}avrdude.exe -v -P ${COM}  -c stk500v2  -p m8  -C  ${BIN}avrdude.conf  -U lfuse:w:0xEF:m

hfuse:
	${BIN}avrdude.exe -v -P ${COM}  -c stk500v2  -p m8  -C  ${BIN}avrdude.conf  -U hfuse:w:0xD9:m

interactive:  # to fix sck: sck 5
	${BIN}avrdude.exe -v -P ${COM}  -c stk500v2  -p m8  -C  ${BIN}avrdude.conf -t


# command to clean up junk (not source files) (invoked by "make clean")
clean:
	rm -f *.o *.rom *.elf *.map *~
