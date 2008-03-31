
// ---------------------------------------------------------------------
// 
//	File: gronkulator.c
//      SWARM Orb SPU code http://www.orbswarm.com
//      prototypes and #defs for swarm serial com routines
//
//      main loop here. 
//      read input data from COM2. Parse it and dispatch parsed commands
//      written by Jonathan Foote (Head Rotor at rotorbrain.com)
//      based on lots of code by Matt, Dillo, Niladri, Rick, 
// -----------------------------------------------------------------------


#include <stdio.h>    /* Standard input/output definitions */
#include <unistd.h>
#include <sys/ioctl.h>
#include <sys/time.h>
#include <sys/select.h>
#include "spuutils.h"
#include "serial.h""
#include <getopt.h>


int parseDebug = 1; 		/*  parser uses this for debug output */




int main(int argc, char *argv[]) 
{

 
  return(0); 			// keep the compiler happy
}
//END main() 


