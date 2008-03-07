
# Compiler flags
CFLAGS = -Wall -W -O2 
LFLAGS =  -O2 

all:  spuflex

test: test.l
	flex test.l
	gcc lex.yy.c  -o test 

spuflex: spuflex.l 
	flex spuflex.l 
	gcc lex.yy.c  -o spuflex 

clean:	
	rm  -f  spuflex *.o 


