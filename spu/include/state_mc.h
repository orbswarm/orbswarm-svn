#include "../include/swarmserial.h"

enum eAggregatorMsgStates {
  eAggregatorMsgStateInit,
  eAggregatorMsgStateMsgStart,
  eAggregatorMsgStateMsgBody,
};

int processRawSerial(int port_fd, char* buff, int* numBytesRead, int maxBufSz,long  maxTrys, char ackChar)
{
  //int status = SWARM_SUCCESS;
  int numReadBytes = 0;
  int nMsgIdx=0;
  char localBuff[maxBufSz];
  int state = eAggregatorMsgStateInit;
  long nTrys =0;
  while(nTrys < maxTrys){
    //printf("try=%dl state=%d\n", nTrys, state);
    numReadBytes = 0;
    readCharsFromSerialPort(port_fd, localBuff, &numReadBytes,maxBufSz); 
    printf("read=%s", localBuff);
    for(int i=0; i < numReadBytes; i++){
      char c = localBuff[i];
      //printf("reading byte=%d is=%c\n", i,c);
      //printf("state=%d\n", state);

      switch (state){
      case eAggregatorMsgStateInit:
	if( '!' == c){
	  state = eAggregatorMsgStateMsgStart;
	  break;
	}//end switch-case
      case eAggregatorMsgStateMsgStart:
	if('!' ==c)
	  state = eAggregatorMsgStateMsgStart;
	else{
	  buff[nMsgIdx++]=c;
	  state = eAggregatorMsgStateMsgBody;
	}
	break;
      case eAggregatorMsgStateMsgBody:
	if('!' == c){
	  *numBytesRead=nMsgIdx;
	  return SWARM_SUCCESS;
	}
	else
	  buff[nMsgIdx++]=c;
	break;
      }//end for
    nTrys++;
    }//end while
  }
  return SWARM_INVALID_GPS_SENTENCE;
}
