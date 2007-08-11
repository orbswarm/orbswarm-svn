#  Makefile for kalman filtering code
#
#  Modified:	JT Szeto, 05/20/2007		Modified to compile for Linux.
#  Original:	J. Watlington, 11/7/1995
#

OBJS   = matmath.o kalman.o kalmanswarm.o mrqmin.o kftime.o
TEST_OBJS   = test.o  $(OBJS)
HARNESS_OBJS   = harness.o  $(OBJS)
LIBS   = -lm
CCOPTS =

test:    $(TEST_OBJS) $(OBJS)
	cc ${CCOPTS} $(TEST_OBJS) -o test $(LIBS)

harness:    $(HARNESS_OBJS) $(OBJS)
	cc ${CCOPTS} $(HARNESS_OBJS) -o harness $(LIBS)

all: test

clean:
	/bin/rm -f *.o *~ test core

${TEST_OBJS}: Makefile test.h kalman.h matmath.h kalmanswarm.h kftime.h

.c.o:
	cc -c $(CCOPTS) $(INCLUDES) -o $@ $<
