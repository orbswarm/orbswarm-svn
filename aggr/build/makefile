MCU = atmega640
F_CPU = 14700000
SVN_TRUNK = c:/trunk

MYSRC = $(SVN_TRUNK)/aggr/src
MYBIN = $(SVN_TRUNK)/aggr/binaries
#TARGET=blinky_test
#TARGET=uart_test
#TARGET=swarm_queues_test
#TARGET=simple_uart_test
#TARGET=spu_test
#TARGET=xbee_test
#TARGET=gps_test
TARGET=main

all:  $(MYBIN)/$(TARGET).hex

#####AVRDUDE saection for the arduino
ARDUINO_UPLOAD_RATE = 19200
ARDUINO_AVRDUDE=avrdude
ARDUINO_AVRDUDE_PROGRAMMER = stk500
ARDUINO_AVRDUDE_PORT = /dev/tty.usbserial-A1000fOr
ARDUINO_AVRDUDE_WRITE_FLASH = -U flash:w:$(MYBIN)/$(TARGET).hex
ARDUINO_AVRDUDE_FLAGS = -F -p $(MCU) -P $(ARDUINO_AVRDUDE_PORT) -c $(ARDUINO_AVRDUDE_PROGRAMMER) \
  -b $(ARDUINO_UPLOAD_RATE)
ARDUINO_FORMAT = ihex 		# create a .hex file

arduino_upload: $(MYBIN)/$(TARGET).hex 
	$(ARDUINO_AVRDUDE) $(ARDUINO_AVRDUDE_FLAGS) $(ARDUINO_AVRDUDE_WRITE_FLASH)


########AVRDUDE section for the stk500
###programming mode is ISP
AVRDUDE=avrdude
AVRDUDE_PROGRAMMER = stk500v2
#AVRDUDE_PORT = /dev/tty.USA19H1b1P1.1
#AVRDUDE_PORT = /dev/tty.usbserial-ftDDKSD7
AVRDUDE_PORT = COM7
UPLOAD_RATE = 115200
AVRDUDE_WRITE_FLASH = -U flash:w:$(MYBIN)/$(TARGET).hex:i
AVRDUDE_FLAGS = -v -p $(MCU) -b $(UPLOAD_RATE) -P $(AVRDUDE_PORT) \
	-c $(AVRDUDE_PROGRAMMER)
FORMAT = ihex 		# create a .hex file



# Program the device.  
upload: $(MYBIN)/$(TARGET).hex 
	$(AVRDUDE) $(AVRDUDE_FLAGS) $(AVRDUDE_WRITE_FLASH)



$(MYBIN)/timer0.o: $(MYSRC)/timer0.c $(MYSRC)/include/timer0.h
	avr-gcc -funsigned-char -c -g -O3 -Wall -mmcu=$(MCU) \
	$< -o $@

$(MYBIN)/uart.o: $(MYSRC)/uart.c $(MYSRC)/include/uart.h
	avr-gcc -funsigned-char -c -g -O3 -Wall -mmcu=$(MCU) \
	$< -o $@

$(MYBIN)/swarm_queues.o: $(MYSRC)/swarm_queues.c $(MYSRC)/include/swarm_queues.h
	avr-gcc -funsigned-char -c -g -O3 -Wall -mmcu=$(MCU) \
	$< -o $@

$(MYBIN)/spu.o: $(MYSRC)/spu.c $(MYSRC)/include/spu.h
	avr-gcc -funsigned-char -c -g -O3 -Wall -mmcu=$(MCU) \
	$< -o $@

$(MYBIN)/xbee.o: $(MYSRC)/xbee.c $(MYSRC)/include/xbee.h
	avr-gcc -funsigned-char -c -g -O3 -Wall -mmcu=$(MCU) \
	$< -o $@

$(MYBIN)/gps.o: $(MYSRC)/gps.c $(MYSRC)/include/gps.h
	avr-gcc -funsigned-char -c -g -O3 -Wall -mmcu=$(MCU) \
	$< -o $@

$(MYBIN)/$(TARGET).o: $(MYSRC)/$(TARGET).c $(MYBIN)/uart.o \
$(MYBIN)/timer0.o $(MYBIN)/swarm_queues.o $(MYBIN)/spu.o \
$(MYBIN)/xbee.o  $(MYBIN)/gps.o
	avr-gcc -funsigned-char -c -g -O3 -Wall -mmcu=$(MCU) $< \
	-o $@

$(MYBIN)/blinky_test.elf: $(MYBIN)/blinky_test.o
	avr-gcc -funsigned-char $< \
	$(MYBIN)/uart.o $(MYBIN)/swarm_queues.o \
	$(MYBIN)/timer0.o -Wl,\
	-Map=$(MYBIN)/$(TARGET).map,--cref -mmcu=$(MCU) \
	-o $@

$(MYBIN)/simple_uart_test.elf: $(MYBIN)/simple_uart_test.o
	avr-gcc -funsigned-char $< \
	$(MYBIN)/uart.o $(MYBIN)/swarm_queues.o \
	$(MYBIN)/timer0.o -Wl,\
	-Map=$(MYBIN)/$(TARGET).map,--cref -mmcu=$(MCU) \
	-o $@

$(MYBIN)/uart_test.elf: $(MYBIN)/uart_test.o
	avr-gcc -funsigned-char $< \
	$(MYBIN)/uart.o $(MYBIN)/swarm_queues.o \
	$(MYBIN)/timer0.o -Wl,\
	-Map=$(MYBIN)/$(TARGET).map,--cref -mmcu=$(MCU) \
	-o $@

$(MYBIN)/swarm_queues_test.elf: $(MYBIN)/swarm_queues_test.o
	avr-gcc -funsigned-char $< \
	$(MYBIN)/uart.o $(MYBIN)/swarm_queues.o \
	-Wl,\
	-Map=$(MYBIN)/$(TARGET).map,--cref -mmcu=$(MCU) \
	-o $@

$(MYBIN)/spu_test.elf: $(MYBIN)/spu_test.o
	avr-gcc -funsigned-char $< \
	$(MYBIN)/uart.o $(MYBIN)/swarm_queues.o \
	$(MYBIN)/spu.o -Wl,\
	-Map=$(MYBIN)/$(TARGET).map,--cref -mmcu=$(MCU) \
	-o $@

$(MYBIN)/xbee_test.elf: $(MYBIN)/xbee_test.o
	avr-gcc -funsigned-char $< \
	$(MYBIN)/uart.o $(MYBIN)/swarm_queues.o \
	$(MYBIN)/xbee.o -Wl,\
	-Map=$(MYBIN)/$(TARGET).map,--cref -mmcu=$(MCU) \
	-o $@

$(MYBIN)/gps_test.elf: $(MYBIN)/gps_test.o
	avr-gcc -funsigned-char $< \
	$(MYBIN)/uart.o $(MYBIN)/swarm_queues.o \
	$(MYBIN)/gps.o -Wl,\
	-Map=$(MYBIN)/$(TARGET).map,--cref -mmcu=$(MCU) \
	-o $@

$(MYBIN)/main.elf: $(MYBIN)/main.o
	avr-gcc -funsigned-char $< \
	$(MYBIN)/uart.o $(MYBIN)/swarm_queues.o $(MYBIN)/timer0.o \
	$(MYBIN)/gps.o $(MYBIN)/xbee.o $(MYBIN)/spu.o -Wl,\
	-Map=$(MYBIN)/$(TARGET).map,--cref -mmcu=$(MCU) \
	-o $@

$(MYBIN)/$(TARGET).hex: $(MYBIN)/$(TARGET).elf
	avr-objcopy -O ihex -R .eeprom $< $@
	
REMOVE = rm -f
clean :
	$(REMOVE) $(MYBIN)/*.hex
	$(REMOVE) $(MYSRC)/*.lst
	$(REMOVE) $(MYBIN)/*.obj
	$(REMOVE) $(MYBIN)/*.elf
	$(REMOVE) $(MYBIN)/*.o
