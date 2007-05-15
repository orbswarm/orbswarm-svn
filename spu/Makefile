SRC = src
INC = include
LIBS =  
CC = /opt/crosstool/gcc-4.0.1-glibc-2.3.5/arm-unknown-linux-gnu/arm-unknown-linux-gnu/bin/gcc
CPP = /opt/crosstool/gcc-4.0.1-glibc-2.3.5/arm-unknown-linux-gnu/arm-unknown-linux-gnu/bin/g++
CFLAGS = -Wall -g -O3 -D_REENTRANT 

# -----------------------------------------------------------------------
# Makefile script for generating spu binary 
spu: spu.o swarmserial.o
	$(CC) $(LIBS) $(CFLAGS) spu.o swarmserial.o -o spu 

spu.o: $(SRC)/spu.cc $(SRC)/swarmserial.cc $(INC)/swarmserial.h $(INC)/swarmdefines.h
	$(CC) $(CFLAGS)-c $(SRC)/spu.cc

swarmserial.o: $(SRC)/swarmserial.cc $(INC)/swarmserial.h $(INC)/swarmdefines.h
	$(CC) $(CFLAGS)-c $(SRC)/swarmserial.cc

