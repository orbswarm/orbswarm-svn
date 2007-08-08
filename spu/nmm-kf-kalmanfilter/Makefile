#  Makefile for kalman filtering code
#
#  Modified:    JT Szeto, 06/05/2007            Modified to compile for EP9302.
#  Modified:    JT Szeto, 05/20/2007            Modified to compile for Linux.
#  Original:    J. Watlington, 11/7/1995
#

CFLAGS := -g -Wall -mcpu=arm920t #-mcpu=ep9312 -mfix-crunch-d1

PREFIX=/opt/crosstool/gcc-3.3.4-glibc-2.3.2
CC := ${PREFIX}/bin/arm-linux-gcc-3.3.4
LIBS := -lm -static
INCLUDES :=
OBJS := mrqmin.o eval_camera.o kalmanswarm.o kalman.o matmath.o random.o test.o
SRCS := mrqmin.c eval_camera.c kalmanswarm.c kalman.c matmath.c random.c test.c
HDRS := matmath.h kalman.h test.h kalmanswarm.h

all: test 

# The variable $@ has the value of the target. In this case $@ = psort
test: ${OBJS} ${HDRS}
	${CC} ${CFLAGS} ${INCLUDES} -o $@ ${OBJS} ${LIBS}

.c.o:
	${CC} ${CFLAGS} ${INCLUDES} -c $<

depend: 
	makedepend ${SRCS}

clean:
	rm *.o test *~

