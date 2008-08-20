

# Crosstools are slightly different versions for cygwin and linux; 
#LINUXCC = /opt/crosstool/gcc-4.0.1-glibc-2.3.5/arm-unknown-linux-gnu/arm-unknown-linux-gnu/bin/gcc
LINUXCC = /opt/crosstool/arm-none-linux-gnueabi/bin/arm-none-linux-gnueabi-gcc
CYGWINCC = /opt/crosstool/gcc-3.3.4-glibc-2.3.2/arm-unknown-linux-gnu/arm-unknown-linux-gnu/bin/gcc.exe


#NATIVECC = /usr/bin/gcc
NATIVECC = gcc

# select them here
# (I'm sure there's a more elegant way to do this...)

#CC = $(NATIVECC)
#CC = $(CYGWINCC)
CC = $(LINUXCC)


# Version
VERSION = 0.0.1

# Compiler flags
CFLAGS = -Wall -Wno-unused-parameter  -O2 
# Linker flags
LFLAGS =  -static -O2 -s -pipe -lm

#scan.c is automatically generated from scan.y by lemon
#scanfrontend.c has support code and lexical analyser for the lemon scanner

all: lemon dispatcher joypatcher

scanner.o scan.h: scan.c scan.y scanfrontend.c
	cat scan.c scanfrontend.c  > scanner.c
	$(CC) -c -o scanner.o $(CFLAGS) scanner.c

scan.c: scan.y 
	./lemon scan.y ; rm -f scanner.c

lemon: lemon.c
	$(NATIVECC) -o $@ $(CFLAGS)  $<

gpsutils.o: gpsutils.c 
	$(CC) -c -o $@ $(CFLAGS)  $<

queues.o: queues.c queues.h
	$(CC) -c -o $@ $(CFLAGS)  $<

gronkulator.o: gronkulator.c
	$(CC) -c -o $@ $(CFLAGS)  $<

# this disables gronk motor control for use with remotes
joygronk.o: gronkulator.c
	$(CC)  -DJOYSTICK -c -o $@ $(CFLAGS)  $<

kalmanswarm.o: kalmanswarm.c
	$(CC) -c -o $@ $(CFLAGS)  $<

kalman.o: kalman.c
	$(CC) -c -o $@ $(CFLAGS)  $<

matmath.o: matmath.c
	$(CC) -c -o $@ $(CFLAGS)  $<

pathfollow.o: pathfollow.c
	$(CC) -c -o $@ $(CFLAGS)  $<

swarmipc.p: swarmipc.c swarmdefines.h scanner.h queues.h
	$(CC) -c -o $@ $(CFLAGS) $<

feedback.o: feedback.c
	$(CC) -c -o $@ $(CFLAGS)  $<

%.o : %.c scan.h
	$(CC) -c -o $@ $(CFLAGS)  $<

testharness: scanner.o testharness.o scan.h gpsutils.o queues.o 	
	$(CC) $(LFLAGS) -o testharness scanner.o testharness.o gpsutils.o /usr/lib/libm.a

dispatcher: dispatcher.o serial.o spuutils.o scanner.o scan.h serial.h  gpsutils.o queues.o imuutils.h imuutils.o kalmanswarm.o kalman.o matmath.o pathfollow.o swarmipc.o feedback.o gronkulator.o
	$(CC) $(LFLAGS) -o dispatcher dispatcher.o serial.o spuutils.o scanner.o  gpsutils.o queues.o imuutils.o kalmanswarm.o kalman.o matmath.o pathfollow.o swarmipc.o feedback.o gronkulator.o /opt/crosstool/arm-none-linux-gnueabi/arm-none-linux-gnueabi/sys-root/usr/lib/libm.a

# version of dispatcher for remote control only...
joypatcher: dispatcher.o serial.o spuutils.o scanner.o scan.h serial.h  gpsutils.o queues.o imuutils.h imuutils.o kalmanswarm.o kalman.o matmath.o pathfollow.o swarmipc.o feedback.o joygronk.o
	$(CC) $(LFLAGS) -o joypatcher dispatcher.o serial.o spuutils.o scanner.o  gpsutils.o queues.o imuutils.o kalmanswarm.o kalman.o matmath.o pathfollow.o swarmipc.o feedback.o joygronk.o /opt/crosstool/arm-none-linux-gnueabi/arm-none-linux-gnueabi/sys-root/usr/lib/libm.a



install: dispatcher
	chmod +x $<
	../transfer.sh $<
clean:	
	rm  -f  test *.o scan.c scan.h scan.out dispatcher gpsutils


