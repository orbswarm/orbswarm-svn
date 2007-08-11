SRC = src
TEST = test
INC = include
LIBS = -lm 

# Crosstools are slightly different versions for cygwin and linux; 
LINUXCC = /opt/crosstool/gcc-4.0.1-glibc-2.3.5/arm-unknown-linux-gnu/arm-unknown-linux-gnu/bin/gcc
LINUXCPP = /opt/crosstool/gcc-4.0.1-glibc-2.3.5/arm-unknown-linux-gnu/arm-unknown-linux-gnu/bin/g++
CYGWINCC = /opt/crosstool/gcc-3.3.4-glibc-2.3.2/arm-unknown-linux-gnu/arm-unknown-linux-gnu/bin/gcc
CYGWINCPP = /opt/crosstool/gcc-3.3.4-glibc-2.3.2/arm-unknown-linux-gnu/arm-unknown-linux-gnu/bin/g++

NATIVECC = gcc

# select them here
# (I'm sure there's a more elegant way to do this...)

CC = $(LINUXCC)
#CC = $(NATIVECC)
#CPP = $(LINUXCPP)
CFLAGS = -Wall -g -O3 -D_REENTRANT -mcpu=ep9312  

# -----------------------------------------------------------------------
# Makefile script for generating spu binary 

#A few useful targets
#all - should be obvious
#test - Make just the test utilities(and everything they fold in)
#app - Make the SPU-resident binaries:spu, mainloop, spumond, etc. 
#util - Make the utilities readcom writecom and setled

#transfer: send all app files to all orb SPUs in reach

all: test 

test: test.o matmath.o random.o kalman.o kalmanswarm.o mrqmin.o kftime.o
	$(CC) $(LIBS) $(CFLAGS) test.o matmath.o random.o kalman.o kalmanswarm.o mrqmin.o kftime.o -o test

test.o: test.c kalman.h matmath.h kalmanswarm.h kftime.h
	$(CC) $(CFLAGS)-c test.c

matmath.o: matmath.c test.h kalman.h matmath.h kalmanswarm.h kftime.h
	$(CC) $(CFLAGS)-c matmath.c

random.o: random.c test.h kalman.h matmath.h kalmanswarm.h kftime.h
	$(CC) $(CFLAGS)-c random.c

kalman.o: kalman.c test.h kalman.h matmath.h kalmanswarm.h kftime.h
	$(CC) $(CFLAGS)-c kalman.c

kalmanswarm.o: kalmanswarm.c test.h kalman.h matmath.h kalmanswarm.h kftime.h
	$(CC) $(CFLAGS)-c kalmanswarm.c

mrqmin.o: mrqmin.c test.h kalman.h matmath.h kalmanswarm.h kftime.h
	$(CC) $(CFLAGS)-c mrqmin.c

kftime.o: kftime.c test.h kalman.h matmath.h kalmanswarm.h kftime.h
	$(CC) $(CFLAGS)-c kftime.c

clean:
	rm *.o 
