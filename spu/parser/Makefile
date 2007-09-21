# Version
VERSION = 0.0.1

# Compiler flags
CFLAGS = -Wall -W -O2 
LFLAGS =  -O2 -s -pipe

#scan.c is automatically generated from scan.y
#scanfrontend.c has support code and lexical analyser fror the lemon scanner

all: lemon scanner
scanner: scan.c scan.y scanfrontend.c
	cat scan.c scanfrontend.c  > scanner.c
	gcc -o scanner $(CFLAGS) scanner.c

scan.c: scan.y 
	  ./lemon scan.y ; rm -f scanner.c

lemon: lemon.c
	  gcc -o $@ $(LFLAGS)  $<

clean:	
	rm  -f  test *.o scan.c scan.h scan.out


