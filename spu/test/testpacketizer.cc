#include <stdio.h>			/* printf			*/
#include <string.h>
#include <stdlib.h>
#include "../include/swarmdefines.h"
#include "../include/swarmspuutils.h"
#include "../include/swarmserial.h"

int main(int argc, char *argv[]) 
{
     fprintf(stderr,"\nBEGIN TEST PACKETIZE SAMPLE AGGREGATOR DATA \n");
     //Some raw aggregator test data
     char * testBuff1 = "$GPGGA,061020.177,8960.000000,N,00000.000000,E,0,0,,137.000,M,13.000,M,,*47\n;$GPVTG,0.00,T,,M,0.000,N,0.000,K,N*32\n;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;$?*;";

     packetizeAndSendMotherShipData(-1, testBuff1, strlen(testBuff1));
     fprintf(stderr,"\nEND TEST PACKETIZE SAMPLE AGGREGATOR DATA \n");
     fprintf(stderr,"\nBEGIN TEST PACKETIZE DATA WITH NO NULL TERMINATOR \n");
     //Test that we can packetize a char array with no null terminating character
     unsigned char testBuff2[1024] = {'$','?','*',';','$','?','*',';','$','?','*',';','$','?','*',';','$','?','*',';','$','?','*',';','$','?','*',';','$','?','*',';','$','?','*',';','$','?','*',';','$','?','*',';','$','?','*',';','$','?','*',';','$','?','*',';','$','?','*',';','$','?','*',';','$','?','*',';','$','?','*',';','$','?','*',';','$','?','*',';','$','?','*',';','$','?','*',';','$','?','*',';','$','?','*',';','$','?','*',';','$','?','*',';','$','?','*',';','$','?','*',';','$','?','*',';','$','?','*',';','$','?','*',';','$','?','*',';','$','?','*',';','$','?','*',';','$','?','*',';','$','?','*',';','$','?','*',';','$','?','*',';','$','?','*',';','$','?','*',';','$','?','*',';','$','?','*',';','$','?','*',';','$','?','*',';','$','?','*',';','$','?','*',';','$','?','*',';','$','?','*',';','$','?','*',';','$','?','*',';','$','?','*',';','$','?','*',';'};
     int testBuff2Sz = 208;

     packetizeAndSendMotherShipData(-1, (char*)testBuff2, testBuff2Sz);
     fprintf(stderr,"\nEND TEST PACKETIZE DATA WITH NO NULL TERMINATOR \n");

     fprintf(stderr,"\nBEGIN TEST PACKETIZE genSpuDump OUTPUT \n");
    //test that we can packetize gen spu dump output
     char lineBuff[1024];
     spuADConverterStatus dummyStatus; 
     dummyStatus.ad_vals[0] = 1.5;
     dummyStatus.ad_vals[1] = 1.4;
     dummyStatus.ad_vals[2] = 1.3;
     dummyStatus.ad_vals[3] = 1.2;
     dummyStatus.ad_vals[4] = 1.1;
     dummyStatus.sonar = 100.25;
     dummyStatus.battery_voltage = 13.43;
     swarmGpsData gpsdata;
     gpsdata.UTMZone[0] = 1.0; 
     gpsdata.nmea_utctime[0] = '\0'; 
     gpsdata.UTMNorthing = 1.0;
     gpsdata.UTMEasting = 1.0;
     gpsdata.nmea_course = 1.0;
     gpsdata.speed = 1.0;
     gpsdata.metFromMshipNorth = 1.0;
     gpsdata.metFromMshipEast = 1.0;

     genSpuDump(lineBuff, 1024, &gpsdata,&dummyStatus);
     // fprintf(stderr,"\n BUFFER:%s\n",testBuff1);

     packetizeAndSendMotherShipData(-1, lineBuff, strlen(lineBuff));

     fprintf(stderr,"\nEND TEST PACKETIZE genSpuDump OUTPUT \n");
 return 0;
}
