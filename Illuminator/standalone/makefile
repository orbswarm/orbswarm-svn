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

GCCFLAGS = -c -Os

# default target when "make" is run w/o arguments

all: illum0.hex illum1.hex illum2.hex illum3.hex

# compile UART.c into UART.o
UART.o: UART.c UART.h
	${BIN}avr-gcc ${GCCFLAGS} -Wall -mmcu=${TARGET} -I. UART.c -o UART.o

parser.o: parser.c parser.h illuminator.h
	${BIN}avr-gcc ${GCCFLAGS} -Wall -mmcu=${TARGET} -I. parser.c -o parser.o

eeprom.o: eeprom.c illuminator.h
	${BIN}avr-gcc ${GCCFLAGS} -Wall -mmcu=${TARGET} -I. eeprom.c -o eeprom.o

putstr.o: putstr.c putstr.h
	${BIN}avr-gcc ${GCCFLAGS} -Wall -mmcu=${TARGET} -I. putstr.c -o putstr.o

main.o: main.c UART.h illuminator.h 
	${BIN}avr-gcc ${GCCFLAGS} -Wall -mmcu=${TARGET} -I. main.c -o main.o

timer.o: timer.c timer.h
	${BIN}avr-gcc ${GCCFLAGS} -Wall -mmcu=${TARGET} -I. timer.c -o timer.o

# probably a ninja way to iterate this but dammned if I can be bothered to figure out the #$%^&* make docs
address0.o:
	${BIN}avr-gcc -DADDRESS=0 ${GCCFLAGS} -Wall -mmcu=${TARGET} -I. address.c -o address0.o 
address1.o:
	${BIN}avr-gcc -DADDRESS=1 ${GCCFLAGS} -Wall -mmcu=${TARGET} -I. address.c -o address1.o 
address2.o:
	${BIN}avr-gcc -DADDRESS=2 ${GCCFLAGS} -Wall -mmcu=${TARGET} -I. address.c -o address2.o 
address3.o:
	${BIN}avr-gcc -DADDRESS=3 ${GCCFLAGS} -Wall -mmcu=${TARGET} -I. address.c -o address3.o 


# link up objects and convert into hex files
illum0.hex: main.o UART.o  putstr.o parser.o eeprom.o timer.o address0.o
	${BIN}avr-gcc main.o UART.o  putstr.o parser.o eeprom.o timer.o address0.o -Wl,-Map=illum.map,--cref -mmcu=${TARGET} -o illum.elf
	${BIN}avr-size illum.elf
	${BIN}avr-objcopy -O ihex illum.elf illum0.hex

illum1.hex: main.o UART.o  putstr.o parser.o eeprom.o timer.o address1.o
	${BIN}avr-gcc main.o UART.o  putstr.o parser.o eeprom.o timer.o address1.o -Wl,-Map=illum.map,--cref -mmcu=${TARGET} -o illum.elf
	${BIN}avr-size illum.elf
	${BIN}avr-objcopy -O ihex illum.elf illum1.hex

illum2.hex: main.o UART.o  putstr.o parser.o eeprom.o timer.o address2.o
	${BIN}avr-gcc main.o UART.o  putstr.o parser.o eeprom.o timer.o address2.o -Wl,-Map=illum.map,--cref -mmcu=${TARGET} -o illum.elf
	${BIN}avr-size illum.elf
	${BIN}avr-objcopy -O ihex illum.elf illum2.hex

illum3.hex: main.o UART.o  putstr.o parser.o eeprom.o timer.o address3.o
	${BIN}avr-gcc main.o UART.o  putstr.o parser.o eeprom.o timer.o address3.o -Wl,-Map=illum.map,--cref -mmcu=${TARGET} -o illum.elf
	${BIN}avr-size illum.elf
	${BIN}avr-objcopy -O ihex illum.elf illum3.hex

# command to program chip (optional) (invoked by running "make install")
il0:
	${BIN}avrdude -P ${COM} -c ${PGMR}  -C  ${BIN}avrdude.conf  -p ${TARGET} -U flash:w:illum0.hex
il1:
	${BIN}avrdude -P ${COM} -c ${PGMR}  -C  ${BIN}avrdude.conf  -p ${TARGET} -U flash:w:illum1.hex
il2:
	${BIN}avrdude -P ${COM} -c ${PGMR}  -C  ${BIN}avrdude.conf  -p ${TARGET} -U flash:w:illum2.hex
il3:
	${BIN}avrdude -P ${COM} -c ${PGMR}  -C  ${BIN}avrdude.conf  -p ${TARGET} -U flash:w:illum3.hex


# set the fuses  for 7.32Mhz atmega8
lfuse:
	${BIN}avrdude.exe -v -P ${COM}  -c stk500v2  -p m8  -C  c:\WinAVR\bin\avrdude.conf  -U lfuse:w:0xEF:m

hfuse:
	${BIN}avrdude.exe -v -P ${COM}  -c stk500v2  -p m8  -C  c:\WinAVR\bin\avrdude.conf  -U hfuse:w:0xD9:m


# eeprom was not sticky! Ouch!! 
a0:
	${BIN}avrdude.exe -v -P ${COM}  -c stk500v2 -C ${BIN}avrdude.conf  -p ${TARGET}  -U eeprom:w:0x00,0x00,0x00:m
a1:
	${BIN}avrdude.exe -v -P ${COM}  -c stk500v2 -C  ${BIN}avrdude.conf  -p ${TARGET}  -U eeprom:w:0x00,0x01,0x00:m
a2:
	${BIN}avrdude.exe -v -P ${COM}  -c stk500v2 -C  ${BIN}avrdude.conf  -p ${TARGET}  -U eeprom:w:0xAA,0xF2,0x55:m
a3:
	${BIN}avrdude.exe -v -P ${COM}  -c stk500v2 -C  ${BIN}avrdude.conf  -p ${TARGET}  -U eeprom:w:0x00,0x03,0x00:m

interactive:  # to fix sck: sck 5
	${BIN}avrdude.exe -v -P ${COM}  -c stk500v2  -p ${TARGET}  -C  ${BIN}avrdude.conf -t


# command to clean up junk (no source files) (invoked by "make clean")
clean:
	rm -f *.o *.hex *.elf *.map *~
