
This directory contains code for the SPU (swarm processing unit), a Technologic Systems TS-7260 Linux ARM computer (google it). 

The main files here are for testing and development

testspuutils.cc:  test-calls some of the routines in ../srv/swarmspuutils.c
logtest.cc: Test the logging code in ../src/swarmspuutils.cc
readcom.cc, writecom.cc: Very simple programs to exercise the serial ports. 
setled.cc: Uses ../src/swarmspuutils.cc to turn on and off status LEDs from the command line
gpsconvtest.cc: Excercise the gps parsing stuff in ../src/swarmspuutils.cc
