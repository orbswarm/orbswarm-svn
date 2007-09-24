
// ---------------------------------------------------------------------
// 
//	File: spuutils.c
//      SWARM Orb SPU code http://www.orbswarm.com
//	general SPU utilities
//
//
//      
//
//	Written by Matt C, Dillo, Niladri, Jesse Z, refactored by Jon F
// -----------------------------------------------------------------------
#include <stdio.h>
#include <unistd.h>   /* for )_RDONLY, etc. */
#include <sys/mman.h> /* for mmap, etc */
#include <fcntl.h>
#include <string.h> /* for strncpy, etc. */
#include "spuutils.h"

int setSpuLed(const unsigned int ledState)
{  
#ifdef LOCAL
#warning "compiling spuutils.c for LOCAL use (not SPU)"
   switch(ledState) {
   case SPU_LED_RED_ON: 
     printf("SPU_LED_RED_ON\n");
     break;
   case SPU_LED_GREEN_ON:
     printf("SPU_LED_GREEN_ON\n");
     break;
   case SPU_LED_BOTH_ON:
     printf("SPU_LED_BOTH_ON\n");
     break;
   case SPU_LED_BOTH_OFF:
     printf("SPU_LED_BOTH_OFF\n");
     break;
   case SPU_LED_RED_OFF:
     printf("SPU_LED_RED_OFF\n");
     break;
   case SPU_LED_GREEN_OFF:
     printf("SPU_LED_GREEN_OFF\n");
     break;
   default:
     fprintf(stderr,"\nUNKNOWN LED STATE");
   }
   return(1);

#else
   volatile unsigned int *PEDR, *PEDDR;
   unsigned char *start;
   int fd = open("/dev/mem", O_RDWR|O_SYNC);

   start =(unsigned char*) mmap(0, getpagesize(), PROT_READ|PROT_WRITE, MAP_SHARED, fd, 0x80840000);
   PEDR = (unsigned int *)(start + 0x20);     // port e data
   PEDDR = (unsigned int *)(start + 0x24);    // port e direction register

   *PEDDR = 0xff;                             // all output (just 2 bits)

   switch(ledState) {
   case SPU_LED_RED_ON: 
     *PEDR |= 0xFE; 
     break;
   case SPU_LED_GREEN_ON:
     *PEDR |= 0xFD; 
     break;
   case SPU_LED_BOTH_ON:
     *PEDR |= 0xFF; 
     break;
   case SPU_LED_BOTH_OFF:
     *PEDR &= 0xFC; 
     break;
   case SPU_LED_RED_OFF:
     *PEDR &= 0xFD; 
     break;
   case SPU_LED_GREEN_OFF:
     *PEDR &= 0xFE; 
     break;
   default:
     fprintf(stderr,"\nUNKNOWN LED STATE");
   }
   close(fd);
   return 0;
#endif
}


/* retrieve IP address using ioctl interface */
int getIP(const char *Interface, char *ip)
{
#ifdef LOCAL
  sprintf(ip,"%s","192.168.1.61");
  return(0);
#else
  int                  s;
  struct ifreq         ifr;
  int                  err;
  struct sockaddr_in * addr_in;

  s = socket( AF_INET, SOCK_DGRAM, IPPROTO_UDP );
  if ( -1 == s )
  {
    perror("Getting interface socket");
    close(s);
    return(-1);
  }

  if(Interface == NULL) Interface="eth0";

  strncpy( ifr.ifr_name, Interface, IFNAMSIZ );
  err = ioctl( s, SIOCGIFADDR, &ifr );
  if ( -1 == err )
  {
    perror("Getting IP address");
    close(s);
    return(-1);
  }
  addr_in = (struct sockaddr_in *)&ifr.ifr_addr;

  if(ip != NULL)
  {
    sprintf(ip,"%s",inet_ntoa(addr_in->sin_addr));
  }

  close(s);
  return(0);
#endif
}

