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

#CC = $(CYGWINCC)
CC = $(LINUXCC)
#CC = $(NATIVECC)
#CPP = $(LINUXCPP)
CFLAGS = -Wall -g -fno-exceptions -O3 -D_REENTRANT 

# -----------------------------------------------------------------------
# Makefile script for generating spu binary 

#A few useful targets
#all - should be obvious
#test - Make just the test utilities(and everything they fold in)
#app - Make the SPU-resident binaries:spu, mainloop, spumond, etc. 
#util - Make the utilities readcom writecom and setled

#transfer: send all app files to all orb SPUs in reach

all: app test util
app: spu spumond mainloop
test: testspuutils logtest gpsconvtest testpacketizer adconverter_input_test
util: readcom testcom setled


#HEY IF SOMEONE KNOWS WHAT THEY ARE DOING WE NEED SOME RULES INSTEAD OF ALL THIS ALACARTE CRAP...


testspuutils: testspuutils.o swarmspuutils.o swarmserial.o 
	$(CC) $(LIBS) $(CFLAGS) testspuutils.o swarmspuutils.o swarmserial.o -o testspuutils

testspuutils.o: $(TEST)/testspuutils.cc $(INC)/swarmspuutils.h $(INC)/swarmGPSutils.h $(INC)/swarmdefines.h $(INC)/swarmserial.h
	$(CC) $(CFLAGS)-c $(TEST)/testspuutils.cc

testpacketizer: testpacketizer.o  swarmspuutils.o swarmserial.o 
	$(CC) $(LIBS) $(CFLAGS) testpacketizer.o  swarmspuutils.o swarmserial.o -o testpacketizer

testpacketizer.o: $(TEST)/testpacketizer.cc 
	$(CC) $(CFLAGS)-c $(TEST)/testpacketizer.cc

testaggstreamparser: testaggstreamparser.o  swarmspuutils.o swarmserial.o 
	$(CC) $(LIBS) $(CFLAGS) testaggstreamparser.o  swarmspuutils.o swarmserial.o -o testaggstreamparser

testaggstreamparser.o: $(TEST)/testaggstreamparser.cc 
	$(CC) $(CFLAGS)-c $(TEST)/testaggstreamparser.cc

setled: setled.o swarmspuutils.o
	$(CC) $(LIBS) $(CFLAGS) setled.o swarmspuutils.o -o setled

setled.o: $(TEST)/setled.cc $(SRC)/swarmspuutils.cc $(INC)/swarmspuutils.h $(INC)/swarmdefines.h
	$(CC) $(CFLAGS)-c $(TEST)/setled.cc

logtest: logtest.o swarmspuutils.o swarmserial.o 
	$(CC) $(LIBS) $(CFLAGS) logtest.o swarmspuutils.o swarmserial.o -o logtest 

logtest.o: $(TEST)/logtest.cc $(INC)/swarmdefines.h $(INC)/swarmspuutils.h  $(INC)/swarmserial.h  
	$(CC) $(CFLAGS)-c $(TEST)/logtest.cc

gpsconvtest: gpsconvtest.o swarmGPSutils.o  
	$(CC) $(LIBS) $(CFLAGS) gpsconvtest.o swarmGPSutils.o -o gpsconvtest 

gpsconvtest.o: $(TEST)/gpsconvtest.cc $(INC)/swarmdefines.h $(INC)/swarmGPSutils.h
	$(CC) $(CFLAGS)-c $(TEST)/gpsconvtest.cc


swarmGPSutils.o: $(SRC)/swarmGPSutils.cc $(INC)/swarmGPSutils.h $(INC)/swarmdefines.h $(INC)/swarmserial.h
	$(CC) $(CFLAGS)-c $(SRC)/swarmGPSutils.cc


swarmIMUutils.o: $(SRC)/swarmIMUutils.cc $(INC)/swarmIMUutils.h $(INC)/swarmdefines.h $(INC)/swarmserial.h
	$(CC) $(CFLAGS)-c $(SRC)/swarmIMUutils.cc


swarmspuutils.o: $(SRC)/swarmspuutils.cc $(INC)/swarmspuutils.h $(INC)/swarmdefines.h $(INC)/swarmserial.h
	$(CC) $(CFLAGS)-c $(SRC)/swarmspuutils.cc


readcom: readcom.o swarmserial.o 
	$(CC) $(LIBS) $(CFLAGS) readcom.o swarmserial.o -o readcom 

readcom.o: $(TEST)/readcom.cc $(SRC)/swarmserial.cc $(INC)/swarmserial.h $(INC)/swarmdefines.h 
	$(CC) $(CFLAGS)-c $(TEST)/readcom.cc

testcom: testcom.o swarmserial.o 
	$(CC) $(LIBS) $(CFLAGS) testcom.o swarmserial.o -o testcom 

testcom.o: $(TEST)/testcom.cc $(SRC)/swarmserial.cc $(INC)/swarmserial.h $(INC)/swarmdefines.h 
	$(CC) $(CFLAGS)-c $(TEST)/testcom.cc

testSpuCom2RW: testSpuCom2RW.o swarmserial.o 
	$(CC) $(LIBS) $(CFLAGS) testSpuCom2RW.o swarmserial.o -o testSpuCom2RW 

testSpuCom2RW.o: $(TEST)/testSpuCom2RW.cc $(SRC)/swarmserial.cc $(INC)/swarmserial.h $(INC)/swarmdefines.h 
	$(CC) $(CFLAGS)-c $(TEST)/testSpuCom2RW.cc

spu: spu.o swarmserial.o 
	$(CC) $(LIBS) $(CFLAGS) spu.o swarmserial.o -o spu 

spu.o: $(SRC)/spu.cc $(SRC)/swarmserial.cc $(INC)/swarmserial.h $(INC)/swarmdefines.h
	$(CC) $(CFLAGS)-c $(SRC)/spu.cc

spu2: spu2.o swarmserial.o swarmspuutils.o
	$(CC) $(LIBS) $(CFLAGS) spu2.o swarmserial.o swarmspuutils.o -o spu2 

spu2.o: $(SRC)/spu2.cc $(SRC)/swarmserial.cc $(INC)/swarmserial.h $(INC)/swarmdefines.h
	$(CC) $(CFLAGS)-c $(SRC)/spu2.cc

spumond: spumond.o swarmspuutils.o swarmserial.o
	$(CC) $(LIBS) $(CFLAGS) spumond.o swarmspuutils.o swarmserial.o -o spumond

spumond.o: $(SRC)/spumond.cc
	$(CC) $(CFLAGS)-c $(SRC)/spumond.cc

mainloop: mainloop.o swarmserial.o swarmspuutils.o  swarmGPSutils.o  swarmIMUutils.o 
	$(CC) $(LIBS) $(CFLAGS) mainloop.o swarmserial.o swarmspuutils.o swarmGPSutils.o swarmIMUutils.o  -o mainloop 

mainloop.o: $(SRC)/mainloop.cc $(SRC)/swarmserial.cc $(SRC)/swarmspuutils.cc $(INC)/swarmserial.h $(INC)/swarmdefines.h 
	$(CC) $(CFLAGS)-c $(SRC)/mainloop.cc

heading.o: $(SRC)/heading.cc $(SRC)/swarmserial.cc $(SRC)/swarmspuutils.cc $(INC)/swarmserial.h $(INC)/swarmdefines.h $(INC)/heading.h 
	$(CC) $(CFLAGS)-c $(SRC)/heading.cc

swarmserial.o: $(SRC)/swarmserial.cc $(INC)/swarmserial.h $(INC)/swarmdefines.h
	$(CC) $(CFLAGS)-c $(SRC)/swarmserial.cc

kalman.o: $(SRC)/kalman.c $(INC)/kalman.h $(INC)/swarmdefines.h $(INC)/swarmkalman.h $(INC)/matmath.h
	$(CC) $(CFLAGS)-c $(SRC)/kalman.c

swarmkalman.o: $(SRC)/swarmkalman.c $(INC)/kalman.h $(INC)/swarmdefines.h $(INC)/swarmkalman.h $(INC)/matmath.h
	$(CC) $(CFLAGS)-c $(SRC)/swarmkalman.c

matmath.o: $(SRC)/matmath.c $(INC)/kalman.h $(INC)/swarmdefines.h $(INC)/swarmkalman.h $(INC)/matmath.h
	$(CC) $(CFLAGS)-c $(SRC)/matmath.c

kalmantest.o: $(test)/kalmantest.c $(INC)/kalman.h $(INC)/swarmdefines.h $(INC)/swarmkalman.h $(INC)/matmath.h
	$(CC) $(CFLAGS)-c $(SRC)/kalmantest.c

kalmantest: kalmantest.o matmath.o swarmkalman.o kalman.o
	$(CC) $(LIBS) $(CFLAGS) kalmantest.o matmath.o swarmkalman.o kalman.o -o kalmantest

transfer: 
	/bin/sh ./transfer.sh spu spumond mainloop

adconverter_input_test: adconverter_input_test.o  adconverter.o swarmserial.o
	$(CC) $(LIBS) $(CFLAGS) adconverter_input_test.o  adconverter.o swarmserial.o -o adconverter_input_test 

adconverter_input_test.o: $(TEST)/adconverter_input_test.cc $(SRC)/swarmserial.cc $(INC)/swarmserial.h  $(INC)/adconverter.h  $(INC)/swarmdefines.h 
	$(CC) $(CFLAGS)-c $(TEST)/adconverter_input_test.cc
	
	
sonar_test: sonar_test.o  adconverter.o swarmserial.o
	$(CC) $(LIBS) $(CFLAGS) sonar_test.o  adconverter.o swarmserial.o -o adconverter_input_test 

sonar_test.o: $(TEST)/sonar_test.cc $(SRC)/swarmserial.cc $(INC)/swarmserial.h  $(INC)/adconverter.h  $(INC)/swarmdefines.h 
	$(CC) $(CFLAGS)-c $(TEST)/sonar_test.cc


adconverter.o: $(INC)/adconverter.h $(SRC)/adconverter.cc 
	$(CC) $(CFLAGS)-c  $(SRC)/adconverter.cc 

clean:
	rm *.o 
