// ---------------------------------------------------------------------
// 
//	File: setled.cc
//      SWARM Orb SPU code http://www.orbswarm.com
//	command-line utility to turn TS-7260 leds on and off
//
//
//      usage: usage: setled <color> n
//         where <color> is a string starting with r or g. 
//         If n is nonzero the led is lit, otherwise it is turned off.
//
//	Written by Jonathan Foote (Head Rotor at rotorbrain.com)
// -----------------------------------------------------------------------
#include <stdlib.h>
#include "../include/swarmspuutils.h"


// setled: light and clear status LEDs from the command line
// usage setled <LED> <0,1> where LED is a string starting with r or g and
// 1 lights the led while 0 clears it. Example: setled red 1 to light red led

void print_usage() {
  fprintf(stderr, "usage: setled <color> n\n where <color> is a string starting with r or g. If n is nonzero the led is lit, otherwise it is cleared\n");
}
int main(int argc, char *argv[]) 
{
  char color;
  int onoff=0;

  if (argc != 3) {
    print_usage();
    return(-1);
  }    
  // first char of first argument
  color = argv[1][0];
  // numerical value of second arg
  onoff = atoi(argv[2]);

  if ((color == 'r') || (color == 'R')) {
    if(onoff)
      toggleSpuLed(SPU_LED_RED_ON);  
    else
      toggleSpuLed(SPU_LED_RED_OFF);  
  }
  else if ((color == 'g') || (color == 'G')) {
    if(onoff)
      toggleSpuLed(SPU_LED_GREEN_ON);  
    else
      toggleSpuLed(SPU_LED_GREEN_OFF);  
  }

  else {
    print_usage();
    return(-1);
  }
  return(0);

}
