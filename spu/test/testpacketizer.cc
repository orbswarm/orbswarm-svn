#include <stdio.h>			/* printf			*/
#include <string.h>
#include <stdlib.h>
#include "../include/swarmdefines.h"
//#include "../include/swarmspuutils.h"
//#include "../include/swarmserial.h"

void genSpuDump(char* logBuffer, int maxBufSz, swarmGpsData * gpsData)
{
  char * scratchBuff = (char *)malloc(maxBufSz * 2);
  sprintf(scratchBuff,"NORTH=%lf\nEAST=%lf\nUTC_TIME=%s\nHEADING=%f\nSPEED=%f\nMSHIP_N=%lf\nMSHIP_E=%lf\n",
   gpsData->UTMNorthing,
   gpsData->UTMEasting,
   gpsData->nmea_utctime,
   gpsData->nmea_course,
   gpsData->speed, 
   gpsData->metFromMshipNorth, 
   gpsData->metFromMshipEast);
  strncpy(logBuffer,scratchBuff, maxBufSz -1);
  free(scratchBuff);
}

// treats porFd as a serial port fd. set portFd to -1 to avoid writing to 
// serial port useful for debugging.
int packetizeAndSendMotherShipData(int portFd, char* buffToWrite, int buffSz)
{
  int status = SWARM_SUCCESS;
  char aggPacket[MAX_AGG_PACKET_SZ];
  char aggPacketPayload[MAX_AGG_PACKET_PAYLOAD_SZ];
  int msgByteIdx = 0;
  int msgBytesLeft = buffSz;
   
  fprintf(stderr, "\n START packetizeAndSendMotherShipData BUFF SZ:%d",buffSz); 
  while(msgBytesLeft > 0)
  { 
    fprintf(stderr, "\n BUILD FULL PACKET INDEX : %d",msgByteIdx); 
    strncpy(aggPacketPayload,&buffToWrite[msgByteIdx],MAX_AGG_PACKET_PAYLOAD_SZ);
    msgByteIdx += strlen(aggPacketPayload);
    msgBytesLeft -= MAX_AGG_PACKET_PAYLOAD_SZ;
    fprintf(stderr, "\n BUILD FULL PACKET BYTES LEFT : %d",msgBytesLeft); 
 
    //compose final packet with header and footer  
    sprintf(aggPacket,"%s%s%s",AGGR_ZIGBEE_STREAM_WRITE_HEADER,aggPacketPayload,AGGR_ZIGBEE_STREAM_WRITE_END); 
    if(portFd < 0)
      fprintf(stderr,"\nFULL PACKET---%s--- SIZE:%d\n",aggPacket,strlen(aggPacket));
     aggPacketPayload[0] = '\0';
     aggPacket[0] = '\0';
    //else
     //writeCharsToSerialPort(portFd, aggPacket, strlen(aggPacket));
  }
  fprintf(stderr, "\n END packetizeAndSendMotherShipData"); 
  return status;
}

int main(int argc, char *argv[]) 
{
   FILE           *in_file;    /* input file */ 
   FILE           *out_file;    /* output file */ 
   char lineBuff[1024];
   /*
   in_file = fopen(argv[1], "r");
   //out_file = fopen(argv[2], "w");
   fprintf(stderr,"\n Got infile name as :%s",argv[1]); 
   if (in_file == NULL) {
       printf("Cannot open %s\n", argv[1]);
       exit(8);
   }
   while(fgets(lineBuff,sizeof(lineBuff),in_file) != NULL)
   {
   */
     swarmGpsData gpsdata;
     gpsdata.UTMZone[0] = 1.0; 
     gpsdata.nmea_utctime[0] = '\0'; 
     gpsdata.UTMNorthing = 1.0;
     gpsdata.UTMEasting = 1.0;
     gpsdata.nmea_course = 1.0;
     gpsdata.speed = 1.0;
     gpsdata.metFromMshipNorth = 1.0;
     gpsdata.metFromMshipEast = 1.0;

     genSpuDump(lineBuff, 1024, &gpsdata);

     fprintf(stderr,"\n BUFFER:%s\n",lineBuff);
     packetizeAndSendMotherShipData(-1, lineBuff, strlen(lineBuff));
   //}
   //fclose(in_file); 
   //fclose(out_file); 
  /*
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
  */
 return 0;
}
