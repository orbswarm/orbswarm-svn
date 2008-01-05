#include <stdio.h>
#include <string.h>
#include <avr/interrupt.h>
#include "include/swarm_common_defines.h"
#include "include/gps.h"

enum EGpsStraightSerialRxStates
  {
    eGpsStraightSerialRxInit,
    eGpsStraightSerialRxPayload,	//$
    eGpsStraightSerialRxPayloadG,	//$G
    eGpsStraightSerialRxPayloadGP,	//$GP
    eGpsStraightSerialRxPayloadGPG,
    eGpsStraightSerialRxPayloadGPGG,
    //  eGpsStraightSerialRxPayloadGPGGA,
    eGpsStraightSerialRxPayloadGPGGAMsg,
    eGpsStraightSerialRxPayloadGPVTGMsg,
    eGpsStraightSerialRxDiscard,
    eGpsStraightSerialRxPMTKMsgStart,
    eGpsStraightSerialRxPMTKMsgPayload
  };


volatile static int s_nGpsRxState = eGpsStraightSerialRxInit;
volatile static unsigned long s_nGpsRxStateByteNum = 0;
volatile static char s_gpsRx_Packet[MAX_GPS_PACKET_LENGTH];

volatile static char s_strGpsGpggaMsg[MAX_GPS_PACKET_LENGTH];
volatile static int s_nGpsGpggaRecordSeq = 0;
volatile static char s_strGpsGpvtgMsg[MAX_GPS_PACKET_LENGTH];
volatile static int s_nGpsGpvtgRecordSeq = 0;
volatile static char s_strGpsPmtkMsg[MAX_GPS_PACKET_LENGTH];

static void (*volatile s_debugCallback) (void) = 0;
static void (*volatile s_debug) (const char *debugMsg) = 0;

static void
debugCallback (void)
{
  if (0 != s_debugCallback)
    (*s_debugCallback) ();
}

static void
debug (const char *debugMsg)
{
  if (0 != s_debug)
    (*s_debug) (debugMsg);
}


static void
clearAck (volatile char *s)
{
  while (*s)
    {
      *s = '\0';
      s++;
    }
}

void
getPmtkMsg (char *returnBuffer, int isInterruptCtx)
{
  strcpy (returnBuffer, (char *)s_strGpsPmtkMsg);
  clearAck (s_strGpsPmtkMsg);
}

void
getGpsGpggaMsg (char *returnBuffer, int isInterruptCtx)
{
  int isDone = 0;
  while (!isDone)
    {
      //critical section start
      if (!isInterruptCtx)
        cli ();
      int nRecSeq = s_nGpsGpggaRecordSeq;
      if (!isInterruptCtx)
        sei ();
      //end critical section              
      strcpy (returnBuffer, (char *)s_strGpsGpggaMsg);

      //critical section start
      if (!isInterruptCtx)
        cli ();
      if (s_nGpsGpggaRecordSeq == nRecSeq)
        {
          debug ("\r\ngot message");
          isDone = 1;
        }
      else
        debug ("\r\n discarding stale message");
      if (!isInterruptCtx)
        sei ();
      //end critical section              
    }
}

void
getGpsGpvtgMsg (char *returnBuffer, int isInterruptCtx)
{
  //  while(1){
  //    int nRecSeq= s_nGpsGpvtgRecordSeq;
  //    strcpy(returnBuffer, s_strGpsGpvtgMsg);
  //    if(s_nGpsGpvtgRecordSeq == nRecSeq)
  //      break;//message has not been overwritten
  //  }
  int isDone = 0;
  while (!isDone)
    {
      //critical section start
      if (!isInterruptCtx)
        cli ();
      int nRecSeq = s_nGpsGpvtgRecordSeq;
      if (!isInterruptCtx)
        sei ();
      //end critical section              
      strcpy (returnBuffer, (char *)s_strGpsGpvtgMsg);

      //critical section start
      if (!isInterruptCtx)
        cli ();
      if (s_nGpsGpvtgRecordSeq == nRecSeq)
        isDone = 1;
      if (!isInterruptCtx)
        sei ();
      //end critical section              
    }
}

void
initGpsModule (void (*debugCallback) (void), void (*debug) (const char *))
{
  s_debugCallback = debugCallback;
  s_debug = debug;
}

static void
initGpsMsgStart (char c)
{
  s_nGpsRxStateByteNum = 0;
  s_gpsRx_Packet[s_nGpsRxStateByteNum] = c;
}

void handleGpsSerial (char c, int isError)
{
	if(isError){
		s_nGpsRxState=eGpsStraightSerialRxDiscard;
		debugCallback();
      	return;
	}
	switch(s_nGpsRxState)
	{
		case eGpsStraightSerialRxInit:
			if('$' == c){
				initGpsMsgStart(c);
				//s_nGpsRxState stays in state=eGpsStraightSerialRxInit
			}
			else 
	    	{
				s_gpsRx_Packet[++s_nGpsRxStateByteNum] = c;
				s_nGpsRxState=eGpsStraightSerialRxPayload;
			}
			break;
		case eGpsStraightSerialRxPayload:
			if('$' == c){
				//dispatch current message
				s_gpsRx_Packet[++s_nGpsRxStateByteNum] = '\0';
				if(0 == strncmp((char *)s_gpsRx_Packet, "$GPGGA", 6)){
					strcpy((char *)s_strGpsGpggaMsg, (char *)s_gpsRx_Packet);
					s_nGpsGpggaRecordSeq++;
				}
				else if(0 == strncmp((char *)s_gpsRx_Packet, "$GPVTG", 6)){
					strcpy((char *)s_strGpsGpvtgMsg, (char *)s_gpsRx_Packet);
					s_nGpsGpvtgRecordSeq++;
				}
				//initialize
				initGpsMsgStart(c);
				s_nGpsRxState=eGpsStraightSerialRxInit;
			}
			else if(++s_nGpsRxStateByteNum <= MAX_GPS_PACKET_LENGTH)
	    	{
				s_gpsRx_Packet[s_nGpsRxStateByteNum] = c;
				//s_nGpsRxState stays in state=eGpsStraightSerialRxInit;
			}
			break;
		case eGpsStraightSerialRxDiscard:
			if('$' == c){
				initGpsMsgStart(c);
				s_nGpsRxState=eGpsStraightSerialRxInit;
			}
			break;	
		default:
			break;
	}
}

