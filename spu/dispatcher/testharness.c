// ---------------------------------------------------------------------
// 
//	File: testharness.c
//      SWARM Orb SPU code http://www.orbswarm.com
//	
//      test harness code for LEMON-generated parser.
//      Takes stdin and tries to parse it, prints results to stdout
//      
//
//	Written by Jonathan Foote (Head Rotor at rotorbrain.com)
// -----------------------------------------------------------------------

#include <stdio.h>
#include <stdlib.h>
#include <ctype.h>  /* for isupper, etc. */
#include <string.h>
#include "scan.h"  /* created by lemon */
#include "scanner.h"
#include "gpsutils.h"

int parseDebug = 1;

/* Parser calls this when there is a complete MCU command */
void dispatchMCUCmd(int spuAddr, cmdStruct *c){
  printf("Orb %d Got MCU command: \"%s\"\n",spuAddr, c->cmd);
}

/* Parser calls this when there is a complete LED command */
void dispatchLEDCmd(int spuAddr, cmdStruct *c){
  printf("Orb %d Got LED command: \"%s\"\n",spuAddr, c->cmd);
}

/* Parser calls this when there is a complete SPU command */
void dispatchSPUCmd(int spuAddr, cmdStruct *c){
  printf("Orb %d Got SPU command: \"%s\"\n",spuAddr, c->cmd);
}

void dispatchGpggaMsg(cmdStruct * c){
  printf("got gps gpgga msg: \"%s\"\n",c->cmd);
  swarmGpsData gpsData;
  strncpy(gpsData.gpsSentence, c->cmd, c->cmd_len);
  int status=parseGPSSentence(&gpsData);
  printf("parseGPSSentence() return=%d\n", status);  
  printf("\n Parsed line %s \n",gpsData.gpsSentence);
  status = convertNMEAGpsLatLonDataToDecLatLon(&gpsData);
  if(status == SWARM_SUCCESS)
  {
      printf("\n Decimal lat:%lf lon:%lf utctime:%s \n",gpsData.latdd,gpsData.londd,gpsData.nmea_utctime);
           
      decimalLatLongtoUTM(WGS84_EQUATORIAL_RADIUS_METERS, WGS84_ECCENTRICITY_SQUARED, &gpsData);
      printf("Northing:%f,Easting:%f,UTMZone:%s\n",gpsData.UTMNorthing,gpsData.UTMEasting,gpsData.UTMZone);
   }
}

void dispatchGpvtgMsg(cmdStruct * c){
  printf("got gps gpvtg msg: \"%s\"\n",c->cmd);
}

int main()
{
  void* pParser = ParseAlloc (malloc);
  int nextchar = 1;

  printf("At main\n");


  while (nextchar > 0) {

    nextchar = (int)getc(stdin);
    //printf("Got char \"%c\"\n",(char)nextchar);

    doScanner(pParser, nextchar);
    
  }    
  ParseFree(pParser, free );
  return(1);
}


