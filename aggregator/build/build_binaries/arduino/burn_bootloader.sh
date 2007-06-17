#!/bin/sh
CMD="avrdude" 
OPTS="-p m8 -b 115200 -P usb -c avrispmkII" 

# Erase chip write lock and fuses 
$CMD $OPTS -e -U lock:w:0x3f:m -U lfuse:w:0xdf:m -U hfuse:w:0xca:m  

# Upload bootloader code 
$CMD $OPTS -D -U flash:w:ATmegaBOOT.hex:i 

# Lock boot section 
$CMD $OPTS -U lock:w:0x0f:m 