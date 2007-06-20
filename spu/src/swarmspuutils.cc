/**************************
 * Implementation file for swarmspuutils.h
 */

#include "../include/swarmspuutils.h"

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
       *PEDR = 0xf0; 
     }
     else if(ledState == SPU_LED_GREEN_ON)
     {
       *PEDR = 0x0f; 
     }
     else if(ledState == SPU_LED_BOTH_ON)
     {
       *PEDR = 0xff; 
     }
     else if(ledState == SPU_LED_BOTH_OFF)
     {
       *PEDR = 0x00; 
     }
     else if(ledState == SPU_LED_RED_OFF)
     {
       *PEDR = *PEDR & 0x0f; 
     }
     else if(ledState == SPU_LED_GREEN_OFF)
     {
       *PEDR = *PEDR & 0xf0; 
     }
     else
     {
       fprintf(stderr,"\nUNKNOWN LED STATE");
     }
   
   // blink 5 times, sleep 1 second so it's visible
   /*
   for (i = 0; i < 5; i++) {
      *PEDR = 0xff;
      sleep(1);
      *PEDR = 0x00;
      sleep(1);
   }
   */
   close(fd);
   return 0;
}

