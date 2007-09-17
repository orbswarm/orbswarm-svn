# Version
VERSION = 0.0.1

# Compiler flags
CFLAGS = -Wall -W -O2 -s -pipe
LFLAGS =  -O2 -s -pipe

all: lemon example1 exp1 example2 exp2 example3 exp3 example4 exp4 lexer example5 exp5 dcalc

test: test.c 
	cat testharness.c >> test.c
	gcc -o test $(LFLAGS) $<

test.c: test.y 
	  ./lemon test.y

lemon: lemon.c
	  gcc -o $@ $(LFLAGS)  $<

clean:	
	rm  -f  test *.o lemon


