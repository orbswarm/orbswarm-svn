This directory contains code for the Daughterboard. The daughterboard
generates motor control commands

Note this is for the new rev2 daughterboards used with the 7800 SPUs. For the 
rev1 daugfhterboards used before summer 2008, look in ../motorControl.


The serial API describes the serial interface. This is documented in
MCU_API.txt.  If you change anything, be sure to update the API
documentation!

This code runs on an ATMEL Atmega8 chip, running at 7.32 Mhz.
We will be upgrading to at Atmega168 chip running at 14.6 mHz. 

See the MCU-API.txt for info on the serial commands used to control the MCU.


Jon Foote July 2008

