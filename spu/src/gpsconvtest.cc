#include <stdio.h>			/* printf			*/
#include <string.h>
#include <stdlib.h>
#include "../include/swarmdefines.h"
#include "../include/swarmspuutils.h"

int main(int argc, char *argv[]) 
{
   FILE           *in_file;    /* input file */ 
   FILE           *out_file;    /* output file */ 
   char lineBuff[1024];
   swarmGpsData * gpsdata = NULL;
   int status = SWARM_SUCCESS;
   in_file = fopen(argv[1], "r");
   out_file = fopen(argv[2], "w");

   fprintf(stderr,"\n Got infile name as :%s",argv[1]); 
   if (in_file == NULL) {
       printf("Cannot open %s\n", argv[1]);
       exit(8);
   }
   while(fgets(lineBuff,sizeof(lineBuff),in_file) != NULL)
   {
     //fprintf(stderr,"\n Read line %s \n",lineBuff);
     gpsdata = (swarmGpsData*) malloc(sizeof(struct swarmGpsData)); 
     strcpy(gpsdata->gpsSentance,lineBuff);
     status = parseGPSSentance(gpsdata);
     if(status == SWARM_SUCCESS)
     { 
        //fprintf(stderr,"\n Parsed line %s \n",gpsdata->gpsSentance);
        status = convertNMEAGpsLatLonDataToDecLatLon(gpsdata);
        if(status == SWARM_SUCCESS)
        {
          //fprintf(stderr,"\n Decimal lat:%Lf lon:%Lf utctime:%s \n",gpsdata->latdd,gpsdata->londd,gpsdata->nmea_utctime);
          fprintf(out_file,"%s,%.12Lf,%.13Lf\n",gpsdata->nmea_utctime,gpsdata->londd,gpsdata->latdd);
        }
     }
   
     free(gpsdata);
   }
   fclose(in_file); 
   fclose(out_file); 
   return 0;
}

