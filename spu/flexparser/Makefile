
# Compiler flags
CFLAGS = -Wall -W -O2 
LFLAGS =  -O2 

all:  test
test: test.l
	flex test.l
	gcc lex.yy.c  -o test 


clean:	
	rm  -f  test *.o 


