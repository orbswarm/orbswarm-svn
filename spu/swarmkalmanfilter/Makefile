#  Makefile for kalman filtering code
#
#  Modified:	JT Szeto, 05/20/2007		Modified to compile for Linux.95
#

OBJS   = matmath.o kalman.o kalmanswarm.o kftime.o
KALMANTEST_OBJS   = kalmantest.o  $(OBJS)
LIBS   = -lm
CCOPTS =



kalmantest:    $(KALMANTEST_OBJS) $(OBJS)
	cc ${CCOPTS} $(KALMANTEST_OBJS) -o kalmantest $(LIBS)

all: kalmantest

clean:
	/bin/rm -f *.o *~ kalmantest core

${KALMANTEST_OBJS}: Makefile kalman.h matmath.h kalmanswarm.h kftime.h ../dispatcher/swarmdefines.h

.c.o:
	cc -c $(CCOPTS) $(INCLUDES) -o $@ $<
