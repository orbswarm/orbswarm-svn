#include "../include/spu_aggregator.h"


static int gps_state=eGpsStateInit;
static char nmea_buffer[1024];
static long nmea_buffer_idx=0;//increment and use
static void (*  _handleGpsSerialRxStartCallback)(void)=0;
static void (* _handleNmeaMsg)(const char* msg, long nLen);

void setupNmeaCallbacks(void (*  handleGpsSerialRxStartCallback)(void),
		        void (* handleNmeaMsg)(const char* msg, long nLen))
{
  _handleGpsSerialRxStartCallback=handleGpsSerialRxStartCallback;
  _handleNmeaMsg=handleNmeaMsg;
}

void handleNmeaSerial(const char* buff, long nNumBytes)
{
  long i;
  for(i=0; i < nNumBytes; i++)
    {
      char c = buff[i];
      switch(gps_state){
      case eGpsStateInit:
	//wait for $
	if('$' == c){
	  if(0 != _handleGpsSerialRxStartCallback)
	    (*_handleGpsSerialRxStartCallback)();
	  gps_state=eGpsStateNMEAPayload;
	  nmea_buffer_idx=0;
	  nmea_buffer[nmea_buffer_idx]=c;
	}
	break;
      case eGpsStateNMEAPayload:
	if('$' == c)
	  {
	    //nmea_buffer[++nmea_buffer_idx]=0;
	    (*_handleNmeaMsg)(nmea_buffer, nmea_buffer_idx);
	    //init
	    if(0 != _handleGpsSerialRxStartCallback)
	      (*_handleGpsSerialRxStartCallback)();
	    gps_state=eGpsStateNMEAPayload;
	    nmea_buffer_idx=0;
	    nmea_buffer[nmea_buffer_idx]=c;
	  }
	else
	  nmea_buffer[++nmea_buffer_idx]=c;
	break;
      }
    }
}

enum eMcuStates{
  eMcuStateStartMsg,
  eMcuStatePayload,
  eMcuStateEndMsg};

static int s_mcuState=eMcuStateStartMsg;
static char s_mcuBuffer[512];
static long s_mcuBufferIdx=0;
static void (* _handleMcuMsg)(const char* msg, long nLen);

void setupMcuCallbacks( void (* handleMcuMsg)(const char* msg, long nLen) )
{
  _handleMcuMsg=handleMcuMsg;
}

void handleMcuSerial(const char* buff, long nNumBytes)
{
  long i;
  for(i=0; i < nNumBytes; i++)
    {
      char c = buff[i];
      switch(s_mcuState){
      case eMcuStateStartMsg:
	if('$' == c){
	  s_mcuState=eMcuStatePayload;
	  s_mcuBufferIdx=0;
	  s_mcuBuffer[ s_mcuBufferIdx]=c;
	}
	break;
      case eMcuStatePayload:
	if('*' == c)
	  {
	    s_mcuBuffer[++ s_mcuBufferIdx]=c;
	    (*_handleMcuMsg)(s_mcuBuffer,s_mcuBufferIdx); 
	    s_mcuState=eMcuStateStartMsg;
	  }
	else
	  s_mcuBuffer[++ s_mcuBufferIdx]=c;
	break;
      }
    }
}


//int main(void)
//{
//}
