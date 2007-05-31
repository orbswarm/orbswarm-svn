#include <avr/io.h>		// include I/O definitions (port names, pin names, etc)
#include <avr/interrupt.h>	// include interrupt support
/**
   Typical usage:
void handle(char ch)
{
  	switch(ch)
	  {
	  case 'a':
	    sendmsg("Got it");
	    break;
	  default:
	    sendmsg("huh!");
	  }
}

int main(void)
{
  init(handle);
  loop();
  return 0;
}
 */

void loop(void);

int init(void (*handle)(char) );

void sendmsg(char *s);
