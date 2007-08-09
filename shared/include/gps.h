#include "swarm_common_defines.h"

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

void  handleGpsSerial(char c, int isError);

void initGpsModule(void (*debugCallback)(void),
		    void (*debug)(const char*) );

void getPmtkMsg(volatile char* returnBuffer);
void getGpsGpggaMsg(volatile char* returnBuffer);
void getGpsGpvtgMsg(volatile char* returnBuffer);


