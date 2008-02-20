SRC = src
TEST = test
INC = include
LIBS = -static -lm 
# -Wl,-rpath /lib/swarm

# Crosstools are slightly different versions for cygwin and linux; 
LINUXCC = /opt/crosstool/arm-none-linux-gnueabi/bin/arm-none-linux-gnueabi-gcc
LINUXCPP = /opt/crosstool/arm-none-linux-gnueabi/bin/arm-none-linux-gnueabi-g++
#LINUXCC = /opt/crosstool/gcc-4.0.1-glibc-2.3.5/arm-unknown-linux-gnu/arm-unknown-linux-gnu/bin/gcc
#LINUXCPP = /opt/crosstool/gcc-4.0.1-glibc-2.3.5/arm-unknown-linux-gnu/arm-unknown-linux-gnu/bin/g++
#LINUXCC = /opt/crosstool/gcc-4.0.1-glibc-2.3.5/arm-unknown-linux-gnu/bin/arm-unknown-linux-gnu-gcc
#LINUXCPP = /opt/crosstool/gcc-4.0.1-glibc-2.3.5/arm-unknown-linux-gnu/bin/arm-unknown-linux-gnu-g++
CYGWINCC = /opt/crosstool/gcc-3.3.4-glibc-2.3.2/arm-unknown-linux-gnu/arm-unknown-linux-gnu/bin/gcc
CYGWINCPP = /opt/crosstool/gcc-3.3.4-glibc-2.3.2/arm-unknown-linux-gnu/arm-unknown-linux-gnu/bin/g++

NATIVECC = gcc

# select them here
# (I'm sure there's a more elegant way to do this...)

CC = $(LINUXCC)
#CC = $(NATIVECC)
#CPP = $(LINUXCPP)
CFLAGS = -Wall -g -O3 -D_REENTRANT -marm 
#-msoft-float 
#-mcpu=ep9312  

# -----------------------------------------------------------------------
# Makefile script for generating spu binary 

#A few useful targets
#all - should be obvious
#test - Make just the test utilities(and everything they fold in)
#app - Make the SPU-resident binaries:spu, mainloop, spumond, etc. 
#util - Make the utilities readcom writecom and setled

#transfer: send all app files to all orb SPUs in reach

all: test 

test: test.o matmath.o kalman.o kalmanswarm.o kftime.o
	$(CC) $(LIBS) $(CFLAGS) test.o matmath.o kalman.o kalmanswarm.o kftime.o /opt/crosstool/arm-none-linux-gnueabi/arm-none-linux-gnueabi/sys-root/usr/lib/libm.a -o test

test.o: test.c kalman.h matmath.h kalmanswarm.h kftime.h ../include/swarmdefines.h
	$(CC) $(CFLAGS)-c test.c

matmath.o: matmath.c kalman.h matmath.h kalmanswarm.h kftime.h ../include/swarmdefines.h
	$(CC) $(CFLAGS)-c matmath.c

kalman.o: kalman.c kalman.h matmath.h kalmanswarm.h kftime.h ../include/swarmdefines.h
	$(CC) $(CFLAGS)-c kalman.c

kalmanswarm.o: kalmanswarm.c kalman.h matmath.h kalmanswarm.h kftime.h ../include/swarmdefines.h
	$(CC) $(CFLAGS)-c kalmanswarm.c

mrqmin.o: mrqmin.c kalman.h matmath.h kalmanswarm.h kftime.h ../include/swarmdefines.h
	$(CC) $(CFLAGS)-c mrqmin.c

kftime.o: kftime.c kalman.h matmath.h kalmanswarm.h kftime.h ../include/swarmdefines.h
	$(CC) $(CFLAGS)-c kftime.c

clean:
	rm *.o 
