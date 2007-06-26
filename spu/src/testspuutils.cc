#include "../include/swarmspuutils.h"

int main(int argc, char *argv[]) 
{
  fprintf(stderr,"\nSTART swarmspuutils toggleSpuLed TESTS");

  fprintf(stderr,"\nReset daughterboard MCU\n");
  toggleSpuLed(SPU_LED_RED_ON);  
  resetOrbMCU();
  toggleSpuLed(SPU_LED_RED_OFF);  

  return(0);
  fprintf(stderr,"\nSwitching on Red spu led for 5 seconds");
  toggleSpuLed(SPU_LED_RED_ON);  
  sleep(5);
  fprintf(stderr,"\nSwitching Red spu led off");
  toggleSpuLed(SPU_LED_RED_OFF);  
  fprintf(stderr,"\nSwitching on Green spu led for 5 seconds");
  toggleSpuLed(SPU_LED_GREEN_ON);  
  sleep(5);
  fprintf(stderr,"\nSwitching Green spu led off");
  toggleSpuLed(SPU_LED_RED_OFF);  
  fprintf(stderr,"\nSwitching on Green and Red spu led's for 5 seconds");
  toggleSpuLed(SPU_LED_BOTH_ON);  
  sleep(5);
  fprintf(stderr,"\nSwitching both spu leds off");
  toggleSpuLed(SPU_LED_BOTH_OFF);  
  fprintf(stderr,"\nEND swarmspuutils toggleSpuLed TESTS\n");
 return 0;
}
