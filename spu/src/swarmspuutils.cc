/**************************
 * Implementation file for swarmspuutils.h
 */

#include "../include/swarmspuutils.h"


// toggle the reset pin on the daighterboard MCU
int resetOrbMCU(void)
{  
   volatile unsigned int *PBDR, *PBDDR;
   unsigned char *start;
   int fd = open("/dev/mem", O_RDWR|O_SYNC);

   start =(unsigned char*) mmap(0, getpagesize(), PROT_READ|PROT_WRITE, MAP_SHARED, fd, 0x80840000);
   PBDR = (unsigned int *)(start + 0x04);     // port b
   PBDDR = (unsigned int *)(start + 0x14);    // port b direction register


   *PBDDR = 0x0FF;                             // all output (just lower 2 bits)

   *PBDR = 0xFE;                              // pin 0 is MCU reset
   sleep(1);
   *PBDR = 0xFF; 
 
   close(fd);
   return 0;
}

int toggleSpuLed(const unsigned int ledState)
{  
   volatile unsigned int *PEDR, *PEDDR;
   unsigned char *start;
   int fd = open("/dev/mem", O_RDWR|O_SYNC);

   start =(unsigned char*) mmap(0, getpagesize(), PROT_READ|PROT_WRITE, MAP_SHARED, fd, 0x80840000);
   PEDR = (unsigned int *)(start + 0x20);     // port e data
   PEDDR = (unsigned int *)(start + 0x24);    // port e direction register

   *PEDDR = 0xff;                             // all output (just 2 bits)

     if(ledState == SPU_LED_RED_ON) 
     {
       *PEDR |= 0xFE; 
     }
     else if(ledState == SPU_LED_GREEN_ON)
     {
       *PEDR |= 0xFD; 
     }
     else if(ledState == SPU_LED_BOTH_ON)
     {
       *PEDR |= 0xFF; 
     }
     else if(ledState == SPU_LED_BOTH_OFF)
     {
       *PEDR &= 0xFC; 
     }
     else if(ledState == SPU_LED_RED_OFF)
     {
       *PEDR &= 0xFD; 
     }
     else if(ledState == SPU_LED_GREEN_OFF)
     {
       *PEDR &= 0xFE; 
     }
     else
     {
       fprintf(stderr,"\nUNKNOWN LED STATE");
     }
   

   close(fd);
   return 0;
}

