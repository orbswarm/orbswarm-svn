#include <stdio.h>
#include <avr/interrupt.h>
#include "include/swarm_common_defines.h"
#include "include/gps.h"

enum EGpsStraightSerialRxStates {
  eGpsStraightSerialRxInit,
  eGpsStraightSerialRxPayload,//$
  eGpsStraightSerialRxPayloadG,//$G
  eGpsStraightSerialRxPayloadGP,//$GP
  eGpsStraightSerialRxPayloadGPG,
  eGpsStraightSerialRxPayloadGPGG,
  //  eGpsStraightSerialRxPayloadGPGGA,
  eGpsStraightSerialRxPayloadGPGGAMsg,
  eGpsStraightSerialRxPayloadGPVTGMsg,
  eGpsStraightSerialRxDiscard,
  eGpsStraightSerialRxPMTKMsgStart,
  eGpsStraightSerialRxPMTKMsgPayload
};


volatile static int s_nGpsRxState=eGpsStraightSerialRxInit;
volatile static int s_isGpsRxIsError=0;
volatile static unsigned long s_nGpsRxStateByteNum=0;
volatile static char s_gpsRx_Packet[MAX_GPS_PACKET_LENGTH];

volatile static char  s_strGpsGpggaMsg[ MAX_GPS_PACKET_LENGTH];
volatile static int s_nGpsGpggaRecordSeq=0;
volatile static char  s_strGpsGpvtgMsg[ MAX_GPS_PACKET_LENGTH];
volatile static int s_nGpsGpvtgRecordSeq=0;
volatile static char s_strGpsPmtkMsg[MAX_GPS_PACKET_LENGTH ];

static void (*volatile s_debugCallback)(void)=0;
static void (*volatile s_debug)(const char* debugMsg)=0;

static char volatile* strcpy(char volatile*  dst, 
		     const char volatile*  src)
{
    /* Do the copying in a loop.  */
    while ((*dst++ = *src++) != '\0')
        ;
    /* Return the destination string.  */
    return dst;
}

static void clearAck(volatile char* s)
{
  while(*s){
    *s='\0';
     s++;
  }
}

void getPmtkMsg(char* returnBuffer,int isInterruptCtx)
{
  strcpy(returnBuffer, s_strGpsPmtkMsg);
  clearAck(s_strGpsPmtkMsg);
}

void  getGpsGpggaMsg(char* returnBuffer,int isInterruptCtx)
{
  int isDone=0;
  while(!isDone){
  	//critical section start
  	if(isInterruptCtx)
  		cli();
  	int nRecSeq  = s_nGpsGpggaRecordSeq;
    if(isInterruptCtx)
  		sei();
	//end critical section  		
    strcpy(returnBuffer, s_strGpsGpggaMsg);
    
    //critical section start
  	if(isInterruptCtx)
  		cli();
  	if(s_nGpsGpggaRecordSeq == nRecSeq)
  		isDone=1;
  	if(isInterruptCtx)
  		sei();
	//end critical section  		
  }
}

void getGpsGpvtgMsg(char* returnBuffer,int isInterruptCtx)
{
//  while(1){
//    int nRecSeq= s_nGpsGpvtgRecordSeq;
//    strcpy(returnBuffer, s_strGpsGpvtgMsg);
//    if(s_nGpsGpvtgRecordSeq == nRecSeq)
//      break;//message has not been overwritten
//  }
  int isDone=0;
  while(!isDone){
  	//critical section start
  	if(isInterruptCtx)
  		cli();
  	int nRecSeq  = s_nGpsGpvtgRecordSeq;
    if(isInterruptCtx)
  		sei();
	//end critical section  		
    strcpy(returnBuffer, s_strGpsGpvtgMsg);
    
    //critical section start
  	if(isInterruptCtx)
  		cli();
  	if(s_nGpsGpvtgRecordSeq == nRecSeq)
  		isDone=1;
  	if(isInterruptCtx)
  		sei();
	//end critical section  		
  }
}

void initGpsModule(void (*debugCallback)(void),
		    void (*debug)(const char*) )
{
  s_debugCallback=debugCallback;
  s_debug=debug;
}

static void debugCallback(void)
{
  if(0 != s_debugCallback)
    (*s_debugCallback)();
}

static void debug(const char* debugMsg)
{
  if(0 != s_debug)
    (*s_debug)(debugMsg);
}

static void initGpsMsgStart(char c)
{
  s_nGpsRxStateByteNum=0;
  s_isGpsRxIsError=0;
  s_gpsRx_Packet[s_nGpsRxStateByteNum]=c;
}


void handleGpsSerial(char c, int isError, int isInterruptCtx)
{
	//critical section start
  	if(isInterruptCtx)
  		cli();
	(*debugCallback)();
  	char strDebugMsg[1024];
/*   debug("\n got char:"); */
/*   debug(dmesgc); */

  if(isError)
    debug("errror flag set");

  //if it's an error flag and discard till the start of the next message
  if(isError){
    s_isGpsRxIsError=isError;
    return;
  }
  switch(s_nGpsRxState){
  case eGpsStraightSerialRxDiscard:
    if('$' ==c){
      initGpsMsgStart(c);
      s_nGpsRxState=eGpsStraightSerialRxPayload;
    }
    break;
  case eGpsStraightSerialRxInit:
    //debug("got $");
    if('$'==c){
      initGpsMsgStart(c);
      s_nGpsRxState=eGpsStraightSerialRxPayload;
    }
    break;
  case eGpsStraightSerialRxPayload://have $, expecting G or P
    if('$'==c){
      initGpsMsgStart(c);
      s_nGpsRxState=eGpsStraightSerialRxPayload;
    }
    else if('G' ==c)
      {
	s_nGpsRxStateByteNum++;
	s_gpsRx_Packet[s_nGpsRxStateByteNum]=c;
	s_nGpsRxState=eGpsStraightSerialRxPayloadG;
      }
    else if( 'P' ==c)
      {
	s_nGpsRxStateByteNum++;
	s_gpsRx_Packet[s_nGpsRxStateByteNum]=c;
	s_nGpsRxState=eGpsStraightSerialRxPMTKMsgStart;
      }
    else
      s_nGpsRxState=eGpsStraightSerialRxDiscard;
    break;
  case eGpsStraightSerialRxPMTKMsgStart:
    //debug(dmesgc);
    s_nGpsRxStateByteNum++;
    if((2 == s_nGpsRxStateByteNum && 'M' == c) ||
       (3 == s_nGpsRxStateByteNum && 'T' ==c) ||
       (4 == s_nGpsRxStateByteNum && 'K' ==c))
      {
	s_gpsRx_Packet[s_nGpsRxStateByteNum]=c;
	if('K' == c)
	  s_nGpsRxState= eGpsStraightSerialRxPMTKMsgPayload;
      }
    else
      s_nGpsRxState=eGpsStraightSerialRxDiscard;
    break;
  case eGpsStraightSerialRxPMTKMsgPayload:
    if('$'==c)
       {
	 //if not error first dispatch old message
	 if(!s_isGpsRxIsError){
	   s_gpsRx_Packet[s_nGpsRxStateByteNum+1]='\0';

/* 	   debug("\n assembled PMTK msg:"); */
/* 	   debug(gps_rx_packet); */

	   strcpy(s_strGpsPmtkMsg, s_gpsRx_Packet);
	 }
	 initGpsMsgStart(c);
         s_nGpsRxState=eGpsStraightSerialRxPayload;
       }
    else
       {
	 //check for max size
	 if(s_nGpsRxStateByteNum <= MAX_GPS_PACKET_LENGTH)
	   s_gpsRx_Packet[++s_nGpsRxStateByteNum]=c;
	 //else ignore till the end
       }
    break;
  case eGpsStraightSerialRxPayloadG://have $G, expecting P
    if('$'==c){
      initGpsMsgStart(c);
      s_nGpsRxState=eGpsStraightSerialRxPayload;
    }
    else if('P' ==c)
      {

	s_nGpsRxStateByteNum++;
	s_gpsRx_Packet[s_nGpsRxStateByteNum]=c;
	s_nGpsRxState=eGpsStraightSerialRxPayloadGP;
      }
    else
      s_nGpsRxState=eGpsStraightSerialRxDiscard;
    break;
  case eGpsStraightSerialRxPayloadGP://$GP, expecting G or V
    if('$'==c){
      initGpsMsgStart(c);
      s_nGpsRxState=eGpsStraightSerialRxPayload;
    }
    else if('G' ==c)
      {

	s_nGpsRxStateByteNum++;
	s_gpsRx_Packet[s_nGpsRxStateByteNum]=c;
	s_nGpsRxState=eGpsStraightSerialRxPayloadGPGGAMsg;
      }
    else if('V' ==c)
      {

	s_nGpsRxStateByteNum++;
	s_gpsRx_Packet[s_nGpsRxStateByteNum]=c;
	s_nGpsRxState=eGpsStraightSerialRxPayloadGPVTGMsg;
      }
    else
      s_nGpsRxState=eGpsStraightSerialRxDiscard;
    break;
  case eGpsStraightSerialRxPayloadGPGGAMsg:
    if((4 == (s_nGpsRxStateByteNum + 1) && 'G' ==c) ||
       (5 == (s_nGpsRxStateByteNum + 1) && 'A' ==c) || 
       (5 < (s_nGpsRxStateByteNum+1)))
      s_nGpsRxStateByteNum++;//fall through
    else{
      s_nGpsRxState=eGpsStraightSerialRxDiscard;
      break;
    }
  case eGpsStraightSerialRxPayloadGPVTGMsg:
    if(eGpsStraightSerialRxPayloadGPGGAMsg == s_nGpsRxState)/* fall through from above*/
      ;
    else if((4 == (s_nGpsRxStateByteNum+1) && 'T' ==c) ||
	    (5 == (s_nGpsRxStateByteNum+1) && 'G' ==c) || 
	    (5 < (s_nGpsRxStateByteNum+1)))
      s_nGpsRxStateByteNum++;
    else {
      s_nGpsRxState=eGpsStraightSerialRxDiscard;
      break;
    }
  default :
    //debug(dmesgc);
    if('$'==c)
       {
	 		//if not error first dispatch old message
	 		if(!s_isGpsRxIsError){
	   			s_gpsRx_Packet[s_nGpsRxStateByteNum+1]='\0';

		   		if(eGpsStraightSerialRxPayloadGPVTGMsg == s_nGpsRxState){
		     		strcpy(s_strGpsGpvtgMsg, s_gpsRx_Packet);
	    			s_nGpsGpvtgRecordSeq++;
	   			}
	   			else if(eGpsStraightSerialRxPayloadGPGGAMsg == s_nGpsRxState){
	     			strcpy(s_strGpsGpggaMsg, s_gpsRx_Packet);
	     			s_nGpsGpggaRecordSeq++;
	   			}
	 		}
	 		initGpsMsgStart(c);
        	s_nGpsRxState=eGpsStraightSerialRxPayload;
       }
     else
       {
	 		//check for max size
	 		if(s_nGpsRxStateByteNum <= MAX_GPS_PACKET_LENGTH)
	   			s_gpsRx_Packet[s_nGpsRxStateByteNum]=c;
	 		//else ignore till the end
       	}
     break;
  }
  //critical section start
  if(isInterruptCtx)
  		sei();
}


