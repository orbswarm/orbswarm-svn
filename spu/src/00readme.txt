This directory contains production code that runs on the SPU (swarm processing unit), an Technologic Systems TS-7260 (google it). 

The main code files are:

mainloop.cc: this is (as the name suggests) the main loop. It runs at 10hz. It generates motor control commands in response to commands from the mother node, and GPS data, and motor and IMU data. 

spu.cc: This is a basic pipe used in early development. It takes input on COM2 (from the zigbee or elsewhere_) and pipes it to COM5 (the motor controller) and vice-versa. 

swarmserial.cc  Routines for opening and maintaining the serial ports (COM1-COM5)

swarmspuutils,cc: Other routines for lighting status LEDs and parsing GPS. (We may move the latter to its own file)
