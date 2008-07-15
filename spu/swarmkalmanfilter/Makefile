#  Makefile for kalman filtering code
#
#  Modified:	JT Szeto, 05/20/2007		Modified to compile for Linux.95
#

OBJS   = matmath.o kalman.o kalmanswarm.o kftime.o
TEST_OBJS   = test.o  $(OBJS)
LIBS   = -lm
CCOPTS =

test:    $(TEST_OBJS) $(OBJS)
	cc ${CCOPTS} $(TEST_OBJS) -o test $(LIBS)

all: test

clean:
	/bin/rm -f *.o *~ test core

${TEST_OBJS}: Makefile kalman.h matmath.h kalmanswarm.h kftime.h ../dispatcher/swarmdefines.h

.c.o:
	cc -c $(CCOPTS) $(INCLUDES) -o $@ $<
