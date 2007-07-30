This directory contains code for the Orb's Motor Control Unit (MCU)
Of interest for those interfacing with it is the Serial API. 
This is documented in MCU_API.txt. 
If you change anything, be sure to update the API documentation!

This code runs on an ATMEL Atmega8 chip, running at 7.32 Mhz.

The current version (16.0 of 18 June 2007) is setup for the SPU daughterboard
(see trunk/pcb/daughterboard in the svn repository for schematic)
+5 volt VCC operation, and talks to the OSSC H-Bridge Hardware.

NOTE: The schematic shows the Analogue to Digital converter unit wired for AVREF operation at 3.3 volts.

There is a define in a2d.c that sets up the ADC for either +5v AVCC ref or +3.3v AVREF operation.

See the MCU-API.txt for info on the serial commands used to control the MCU.


Jon Foote

(modifed by Pete's code from Petey the Programmer. 7-May-2007)

--------------------

V15.0: Updated by Jon Foote to support

Changes:

New include file global.h defines processor clock frequency
New #defines in uart.h to support multiple clock frequencies

Added file motor-vsp.c to replace motor.c to control vsp-based H-bridges
Changed makefile to support new target orb-vsp


-----------------------------------------

V16.0: Updated by Jon Foote to support

Changes:


New #defines in uart.h to support 7.32 mHz frequencies
Changed pin definitions to support daughterboard
Removed motor-vsp.c support
