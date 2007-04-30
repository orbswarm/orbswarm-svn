This directory contains code for Pete's ESC (Electronic Speed Control). 
Of interest for those interfacing with it is the serial api. 
This is documented in ESC_API.txt. 
If you change anything, be sure to update the API documentation!



This code runs on an ATMEL Atmega8 chip, running at 8.0 Mhz.

It was compiled on a Mac running OS-X using avr-gcc 4.02, and was downloaded to the Chip 
using AvrDude and the ATMEL STK500 dev board / programmer.

(It will also compile using AVR Studio 4 running on Windows)

The current version (13.4 of 29-April-2007) is setup for +5volt operation, and talks to the OSSC H-Bridge Hardware.

See the ESC-API.txt for info on the serial commands used to control the ESC.

Petey the Programmer.
30-Apr-07
