SRC = src
INC = include
LIBS =  
##########GCC 3
#CC = /opt/crosstool/gcc-3.3.4-glibc-2.3.2/arm-unknown-linux-gnu/arm-unknown-linux-gnu/bin/gcc

#########GCC 4
#CC = /opt/crosstool/gcc-4.0.1-glibc-2.3.5/arm-unknown-linux-gnu/arm-unknown-linux-gnu/bin/gcc

CC = gcc
CFLAGS = -Wall -g -O3 -D_REENTRANT 

# -----------------------------------------------------------------------
# Makefile script for generating spu binary 
spu: spu.o swarmserial.o
	$(CC) $(LIBS) $(CFLAGS) spu.o swarmserial.o -o spu 

testspuutils: testspuutils.o swarmspuutils.o
	$(CC) $(LIBS) $(CFLAGS) testspuutils.o swarmspuutils.o -o testspuutils

spu.o: $(SRC)/spu.cc $(SRC)/swarmserial.cc $(INC)/swarmserial.h $(INC)/swarmdefines.h
	$(CC) $(CFLAGS)-c $(SRC)/spu.cc

testspuutils.o: $(SRC)/testspuutils.cc $(SRC)/swarmspuutils.cc $(INC)/swarmspuutils.h $(INC)/swarmdefines.h
	$(CC) $(CFLAGS)-c $(SRC)/testspuutils.cc

swarmserial.o: $(SRC)/swarmserial.cc $(INC)/swarmserial.h $(INC)/swarmdefines.h
	$(CC) $(CFLAGS)-c $(SRC)/swarmserial.cc

swarmspuutils.o: $(SRC)/swarmspuutils.cc $(INC)/swarmspuutils.h $(INC)/swarmdefines.h
	$(CC) $(CFLAGS)-c $(SRC)/swarmspuutils.cc

