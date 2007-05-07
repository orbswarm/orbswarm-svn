This directory contains code for the Orb's Motor Control Unit (MCU)
Of interest for those interfacing with it is the Serial API. 
This is documented in MCU_API.txt. 
If you change anything, be sure to update the API documentation!

This code runs on an ATMEL Atmega8 chip, running at 8.0 Mhz.
It was compiled on a Mac running OS-X using avr-gcc 4.02, and was downloaded to the Chip 
using AvrDude and the ATMEL STK500 dev board / programmer.

(It will also compile using AVR Studio 4 running on Windows)

The current version (14.3 of 7-May-2007) is setup for +5 volt VCC operation, and talks to the OSSC H-Bridge Hardware.

The schematic for wiring the MCU is shown in MCU_Schematic.pdf
NOTE: The schematic shows the Analogue to Digital converter unit wired for AVREF operation at 3.3 volts.
The current code (v14.3) is still setup for AVCC ref of +5 volts. (AVREF not connected)
There is a define in a2d.c that sets up the ADC for either +5v AVCC ref or +3.3v AVREF operation.

See the MCU-API.txt for info on the serial commands used to control the MCU.

Petey the Programmer.
7-May-2007

