
This directory contains code directories for the SPU (swarm processing unit), an Technologic Systems TS-7260 (google it). 

The main subdirs are:

src:  production code goes here. Look at this first.
test: test and development routines
sys: config, init files, and other info for the SPU (not code)
include: .h files for all the above

nnm-kf-kalmanfilter: Code for implementing an extended Kalman filter (details on  wiki.orbswarm.com)


the makefiles are for cross-compiling. You will need an appropriate toolchain from embeddedarm.com (Linux or Cygwin [Windows])

transfer.sh is a shell script (/bin/sh) to ftp executables to each orb. 




