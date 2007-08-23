

This dir contains ARM executables to run on the SPU. 

Contents:

testcom:  terminal-like program. Usage: "testcom N" where N is com port. 
          echoes stdin (typing) to selected com port and prints out anything back. 

spu:  open pipe: takes com2 aqnd pipes it to com 5 and vice-versa

spu2: Matt's rewrite of mainloop and the IMU/GPS parsing code. 

spumond: Dillo's monitor daemon
