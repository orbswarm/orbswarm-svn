

# Crosstools are slightly different versions for cygwin and linux; 
LINUXCC = /opt/crosstool/gcc-4.0.1-glibc-2.3.5/arm-unknown-linux-gnu/arm-unknown-linux-gnu/bin/gcc
CYGWINCC = /opt/crosstool/gcc-3.3.4-glibc-2.3.2/arm-unknown-linux-gnu/arm-unknown-linux-gnu/bin/gcc

NATIVECC = /usr/bin/gcc

# select them here
# (I'm sure there's a more elegant way to do this...)

CC = $(NATIVECC)
CC = $(CYGWINCC)


# Version
VERSION = 0.0.1

# Compiler flags
CFLAGS = -Wall -Wno-unused-parameter -W -O2 
# Linker flags
LFLAGS =  -O2 -s -pipe -lm

#scan.c is automatically generated from scan.y
#scanfrontend.c has support code and lexical analyser fror the lemon scanner

all: lemon scanner testharness
scanner.o scan.h: scan.c scan.y scanfrontend.c
	cat scan.c scanfrontend.c  > scanner.c
	$(CC) -c -o scanner.o $(CFLAGS) scanner.c

scan.c: scan.y 
	./lemon scan.y ; rm -f scanner.c

lemon: lemon.c
	$(NATIVECC) -o $@ $(CFLAGS)  $<

%.o : %.c scan.h
	$(CC) -c -o $@ $(CFLAGS)  $<

testharness: scanner.o testharness.o scan.h
	$(CC) $(LFLAGS) -o testharness scanner.o testharness.o

install: testharness
	chmod +x testharness
	../transfer.sh testharness
clean:	
	rm  -f  test *.o scan.c scan.h scan.out


