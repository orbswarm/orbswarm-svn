#include <stdio.h>			/* printf			*/
#include <string.h>
#include "../include/swarmdefines.h"
#include "../include/swarmspuutils.h"

int main()
{
   for(int i = 0;i <1000;i++)
   {
      int bytesw = 0;
      char testEnt[128];
      sprintf(testEnt,"%d THIS IS TEST LOG ENT",i); 
      bytesw = spulog(testEnt,strlen(testEnt),NULL);    
   }
   return 0;
}

