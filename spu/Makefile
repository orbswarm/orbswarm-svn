SRC = src
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

CC = $(CYGWINCC)
#CC = $(NATIVECC)
CPP = $(CYGWINCPP)
CFLAGS = -Wall -g -O3 -D_REENTRANT 

# -----------------------------------------------------------------------
# Makefile script for generating spu binary 

testspuutils: testspuutils.o swarmspuutils.o
	$(CC) $(LIBS) $(CFLAGS) testspuutils.o swarmspuutils.o -o testspuutils

testspuutils.o: $(SRC)/testspuutils.cc $(SRC)/swarmspuutils.cc $(INC)/swarmspuutils.h $(INC)/swarmdefines.h
	$(CC) $(CFLAGS)-c $(SRC)/testspuutils.cc

setled: setled.o swarmspuutils.o
	$(CC) $(LIBS) $(CFLAGS) setled.o swarmspuutils.o -o setled

setled.o: $(SRC)/setled.cc $(SRC)/swarmspuutils.cc $(INC)/swarmspuutils.h $(INC)/swarmdefines.h
	$(CC) $(CFLAGS)-c $(SRC)/setled.cc

logtest: logtest.o swarmspuutils.o $(SRC)/swarmspuutils.cc $(INC)/swarmspuutils.h
	$(CC) $(LIBS) $(CFLAGS) logtest.o swarmspuutils.o -o logtest 

logtest.o: $(SRC)/logtest.cc $(INC)/swarmdefines.h $(INC)/swarmspuutils.h
	$(CC) $(CFLAGS)-c $(SRC)/logtest.cc

gpsconvtest: gpsconvtest.o swarmspuutils.o $(SRC)/swarmspuutils.cc $(INC)/swarmspuutils.h
	$(CC) $(LIBS) $(CFLAGS) gpsconvtest.o swarmspuutils.o -o gpsconvtest 

gpsconvtest.o: $(SRC)/gpsconvtest.cc $(INC)/swarmdefines.h $(INC)/swarmspuutils.h
	$(CC) $(CFLAGS)-c $(SRC)/gpsconvtest.cc

swarmspuutils.o: $(SRC)/swarmspuutils.cc $(INC)/swarmspuutils.h $(INC)/swarmdefines.h
	$(CC) $(CFLAGS)-c $(SRC)/swarmspuutils.cc

readcom: readcom.o swarmserial.o 
	$(CC) $(LIBS) $(CFLAGS) readcom.o swarmserial.o -o readcom 

readcom.o: $(SRC)/readcom.cc $(SRC)/swarmserial.cc $(INC)/swarmserial.h $(INC)/swarmdefines.h 
	$(CC) $(CFLAGS)-c $(SRC)/readcom.cc


writecom: writecom.o swarmserial.o 
	$(CC) $(LIBS) $(CFLAGS) writecom.o swarmserial.o -o writecom 

writecom.o: $(SRC)/writecom.cc $(SRC)/swarmserial.cc $(INC)/swarmserial.h $(INC)/swarmdefines.h 
	$(CC) $(CFLAGS)-c $(SRC)/writecom.cc


spu: spu.o swarmserial.o 
	$(CC) $(LIBS) $(CFLAGS) spu.o swarmserial.o -o spu 



spu.o: $(SRC)/spu.cc $(SRC)/swarmserial.cc $(INC)/swarmserial.h $(INC)/swarmdefines.h
	$(CC) $(CFLAGS)-c $(SRC)/spu.cc


mainloop: mainloop.o swarmserial.o swarmspuutils.o
	$(CC) $(LIBS) $(CFLAGS) mainloop.o swarmserial.o swarmspuutils.o -o mainloop 

mainloop.o: $(SRC)/mainloop.cc $(SRC)/swarmserial.cc $(SRC)/swarmspuutils.cc $(INC)/swarmserial.h $(INC)/swarmdefines.h 
	$(CC) $(CFLAGS)-c $(SRC)/mainloop.cc


swarmserial.o: $(SRC)/swarmserial.cc $(INC)/swarmserial.h $(INC)/swarmdefines.h
	$(CC) $(CFLAGS)-c $(SRC)/swarmserial.cc

clean:
	rm *.o 
