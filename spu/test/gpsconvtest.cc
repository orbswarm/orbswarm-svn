#include <stdio.h>			/* printf			*/
#include <string.h>
#include <stdlib.h>
#include "../include/swarmdefines.h"
#include "../include/swarmGPSutils.h"
#include "../include/swarmGPSutils.h"

int main(int argc, char *argv[]) 
{
   FILE           *in_file;    /* input file */ 
   FILE           *out_file;    /* output file */ 
   FILE           *utm_out_file;    /* output file */ 
   char lineBuff[1024];
   swarmGpsData * gpsdata = NULL;
   int status = SWARM_SUCCESS;
   /*
   in_file = fopen(argv[1], "r");
   out_file = fopen(argv[2], "w");
   utm_out_file = fopen(argv[3], "w");

   fprintf(stderr,"\n Got infile name as :%s",argv[1]); 
   if (in_file == NULL) {
       printf("Cannot open %s\n", argv[1]);
       exit(8);
   }
   while(fgets(lineBuff,sizeof(lineBuff),in_file) != NULL)
   {
   */
     //fprintf(stderr,"\n Read line %s \n",lineBuff);

     gpsdata = (swarmGpsData*) malloc(sizeof(struct swarmGpsData)); 
     strcpy(lineBuff,"$GPGGA,061020.177,8960.000000,N,00000.000000,E,0,0,,137.000,M,13.000,M,,*47\n\n$GPVTG,0.00,T,,M,0.000,N,0.000,K,N*32");

     //fprintf(stderr, "\nRAW GPS DATA : %s\n",lineBuff);

     parseRawAggregatorGPSData(lineBuff, gpsdata); 

     status = parseGPSSentence(gpsdata);
     if(status == SWARM_SUCCESS)
     { 
        fprintf(stderr,"\n Parsed gps line %s \n",gpsdata->gpsSentence);
         /*
        status = convertNMEAGpsLatLonDataToDecLatLon(gpsdata);
        if(status == SWARM_SUCCESS)
        {
          //fprintf(stderr,"\n Decimal lat:%Lf lon:%Lf utctime:%s \n",gpsdata->latdd,gpsdata->londd,gpsdata->nmea_utctime);
          fprintf(out_file,"%s,%.12f,%.13f\n",gpsdata->nmea_utctime,gpsdata->londd,gpsdata->latdd);
          
          decimalLatLongtoUTM(WGS84_EQUATORIAL_RADIUS_METERS, WGS84_ECCENTRICITY_SQUARED, gpsdata);
          fprintf(utm_out_file,"Northing:%f,Easting:%f,UTMZone:%s\n",gpsdata->UTMNorthing,gpsdata->UTMEasting,gpsdata->UTMZone);
        }
        */
        
     }
     status = parseGPSVtgSentance(gpsdata);
     if(status == SWARM_SUCCESS)
        fprintf(stderr,"\n Parsed vtg line %s \n",gpsdata->vtgSentence);
   
     free(gpsdata);
   /*
   }
   fclose(in_file); 
   fclose(out_file); 
   */
   return 0;
}

