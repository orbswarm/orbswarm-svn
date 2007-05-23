This directory contains code for the Orb Stand-Alone Transmitter that was housed in the small Cox R/C transmitter case & used to control the orb at the Maker Faire.  It contained the ATMega chip, an XBee Pro RF module, and 4 alkaline batteries wired for 3 volts (2s2p).

This code runs on an ATMEL Atmega8L chip, running at 8.0 Mhz using it's internal oscillator.

It was compiled on a Mac running OS-X using avr-gcc 4.02, and was downloaded to the Chip using AvrDude and the ATMEL STK500 dev board / programmer.

The schematic for wiring the xmitter is shown in Xmit_Schematic.pdf

The Make file will compile the code, download to the chip, and set the fuses.

The Transmitter can be configured using an external laptop & XBee module, to send commands to the xmitter to set the steering and speed limits.  The config can be saved to eeprom. (See main.c)

Petey the Programmer.
23-May-2007

