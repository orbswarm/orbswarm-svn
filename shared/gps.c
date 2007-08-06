//#include <string.h>
//#include <swarm_messaging.h>
#include "include/gps.h"
#include <packet_type.h>


volatile static int gps_rx_state=eGpsStraightSerialRxInit;
volatile static int gps_rx_is_error=0;
volatile static unsigned long gps_rx_state_byte_num=0;
volatile static unsigned char gps_rx_packet[MAX_GPS_PACKET_LENGTH];

volatile static unsigned char  gps_gpggaMsg[ MAX_GPS_PACKET_LENGTH];
volatile static unsigned char  gps_gpvtgMsg[ MAX_GPS_PACKET_LENGTH];
volatile static unsigned char gps_pmtkMsg[MAX_GPS_PACKET_LENGTH ];

static void (*_debugCallback)(void)=0;
static void (*_debug)(const char* debugMsg)=0;

unsigned char *strcpy(volatile unsigned char *restrict s1, 
		      volatile const unsigned char *restrict s2)
{
    volatile unsigned char *dst = s1;
    volatile const unsigned char *src = s2;
    /* Do the copying in a loop.  */
    while ((*dst++ = *src++) != '\0')
        ;
    /* Return the destination string.  */
    return s1;
}

void clearAck(unsigned char* s)
{
  while(*s){
    *s='\0';
     s++;
  }
}

void getPmtkMsg(volatile unsigned char* returnBuffer)
{
  strcpy(returnBuffer, gps_pmtkMsg);
  clearAck( gps_pmtkMsg);
}

void  getGpsGpggaMsg(volatile unsigned char* returnBuffer)
{
  strcpy(returnBuffer, gps_gpggaMsg);
}

void getGpsGpvtgMsg(volatile unsigned char* returnBuffer)
{
  strcpy(returnBuffer, gps_gpvtgMsg);
}

void initGpsModule(void (*debugCallback)(void),
		    void (*debug)(const char*) )
{
  _debugCallback=debugCallback;
  _debug=debug;
}

static void debugCallback(void)
{
  if(0 != _debugCallback)
    (*_debugCallback)();
}

static void debug(const char* debugMsg)
{
  if(0 != _debug)
    (*_debug)(debugMsg);
}

static void initGpsMsgStart(unsigned char c)
{
  gps_rx_state_byte_num=0;
  gps_rx_is_error=0;
  gps_rx_packet[gps_rx_state_byte_num]=c;
}

 //we are interested in only these messages
  //$GPGGA,000243.000,3743.980645,N,12222.595411,W,1,8,1.18,28.298,M,-25.322,M,,*52
void dummyHandleGpsSerial(unsigned char c, int isError)
{
  debugCallback();
  debug("dummy");
}

void handleGpsSerial(unsigned char c, int isError)
{
  char dmesgc[2];
  dmesgc[0]=c;
  dmesgc[1]='\0';
  //debug(dmesgc);
  //if it's an error flag and discard till the start of the next message
  if(isError){
    gps_rx_is_error=isError;
    return;
  }
  switch(gps_rx_state){
  case eGpsStraightSerialRxDiscard:
    if('$' ==c){
      initGpsMsgStart(c);
      gps_rx_state=eGpsStraightSerialRxPayload;
    }
    break;
  case eGpsStraightSerialRxInit:
    //debug("got $");
    if('$'==c){
      initGpsMsgStart(c);
      gps_rx_state=eGpsStraightSerialRxPayload;
    }
    break;
  case eGpsStraightSerialRxPayload://have $, expecting G or P
    if('$'==c){
      initGpsMsgStart(c);
      gps_rx_state=eGpsStraightSerialRxPayload;
    }
    else if('G' ==c)
      {
	gps_rx_state_byte_num++;
	gps_rx_packet[gps_rx_state_byte_num]=c;
	gps_rx_state=eGpsStraightSerialRxPayloadG;
      }
    else if( 'P' ==c)
      {
	gps_rx_state_byte_num++;
	gps_rx_packet[gps_rx_state_byte_num]=c;
	gps_rx_state=eGpsStraightSerialRxPMTKMsgStart;
      }
    else
      gps_rx_state=eGpsStraightSerialRxDiscard;
    break;
  case eGpsStraightSerialRxPMTKMsgStart:
    //debug(dmesgc);
    gps_rx_state_byte_num++;
    if((2 == gps_rx_state_byte_num && 'M' == c) ||
       (3 == gps_rx_state_byte_num && 'T' ==c) ||
       (4 == gps_rx_state_byte_num && 'K' ==c))
      {
	gps_rx_packet[gps_rx_state_byte_num]=c;
	if('K' == c)
	  gps_rx_state= eGpsStraightSerialRxPMTKMsgPayload;
      }
    else
      gps_rx_state=eGpsStraightSerialRxDiscard;
    break;
  case eGpsStraightSerialRxPMTKMsgPayload:
    if('$'==c)
       {
	 //if not error first dispatch old message
	 if(!gps_rx_is_error){
	   gps_rx_packet[gps_rx_state_byte_num+1]='\0';
	   strcpy(gps_pmtkMsg, gps_rx_packet);
	 }
	 initGpsMsgStart(c);
         gps_rx_state=eGpsStraightSerialRxPayload;
       }
     else
       {
	 //check for max size
	 if(gps_rx_state_byte_num <= MAX_GPS_PACKET_LENGTH)
	   gps_rx_packet[++gps_rx_state_byte_num]=c;
	 //else ignore till the end
       }
    break;
  case eGpsStraightSerialRxPayloadG://have $G, expecting P
    if('$'==c){
      initGpsMsgStart(c);
      gps_rx_state=eGpsStraightSerialRxPayload;
    }
    else if('P' ==c)
      {

	gps_rx_state_byte_num++;
	gps_rx_packet[gps_rx_state_byte_num]=c;
	gps_rx_state=eGpsStraightSerialRxPayloadGP;
      }
    else
      gps_rx_state=eGpsStraightSerialRxDiscard;
    break;
  case eGpsStraightSerialRxPayloadGP://$GP, expecting G or V
    if('$'==c){
      initGpsMsgStart(c);
      gps_rx_state=eGpsStraightSerialRxPayload;
    }
    else if('G' ==c)
      {

	gps_rx_state_byte_num++;
	gps_rx_packet[gps_rx_state_byte_num]=c;
	gps_rx_state=eGpsStraightSerialRxPayloadGPGGAMsg;
      }
    else if('V' ==c)
      {

	gps_rx_state_byte_num++;
	gps_rx_packet[gps_rx_state_byte_num]=c;
	gps_rx_state=eGpsStraightSerialRxPayloadGPVTGMsg;
      }
    else
      gps_rx_state=eGpsStraightSerialRxDiscard;
    break;
  case eGpsStraightSerialRxPayloadGPGGAMsg:
    if((4 == (gps_rx_state_byte_num + 1) && 'G' ==c) ||
       (5 == (gps_rx_state_byte_num + 1) && 'A' ==c) || 
       (5 < (gps_rx_state_byte_num+1)))
      gps_rx_state_byte_num++;//fall through
    else{
      gps_rx_state=eGpsStraightSerialRxDiscard;
      break;
    }
  case eGpsStraightSerialRxPayloadGPVTGMsg:
    if(eGpsStraightSerialRxPayloadGPGGAMsg == gps_rx_state)/* fall through from above*/
      ;
    else if((4 == (gps_rx_state_byte_num+1) && 'T' ==c) ||
	    (5 == (gps_rx_state_byte_num+1) && 'G' ==c) || 
	    (5 < (gps_rx_state_byte_num+1)))
      gps_rx_state_byte_num++;
    else {
      gps_rx_state=eGpsStraightSerialRxDiscard;
      break;
    }
  default :
    //debug(dmesgc);
    if('$'==c)
       {
	 //if not error first dispatch old message
	 if(!gps_rx_is_error){
	   gps_rx_packet[gps_rx_state_byte_num+1]='\0';
	   if(eGpsStraightSerialRxPayloadGPVTGMsg == gps_rx_state)
	     strcpy(gps_gpvtgMsg, gps_rx_packet);
	   else if(eGpsStraightSerialRxPayloadGPGGAMsg == gps_rx_state)
	     strcpy(gps_gpggaMsg, gps_rx_packet);
	 }
	 initGpsMsgStart(c);
         gps_rx_state=eGpsStraightSerialRxPayload;
       }
     else
       {
	 //check for max size
	 if(gps_rx_state_byte_num <= MAX_GPS_PACKET_LENGTH)
	   gps_rx_packet[gps_rx_state_byte_num]=c;
	 //else ignore till the end
       }
     break;
  }

}

void handleGpsSerialBuf(const char* buf, long nNumBytes)
{
  for(int i=0; i < nNumBytes; i++)
    {
      handleGpsSerial((unsigned char)buf[i], 0);
    }
}

