#include "uart.h"

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
