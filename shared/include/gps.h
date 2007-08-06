#define MAX_GPS_PACKET_LENGTH 256

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

void  handleGpsSerial(unsigned char c, int isError);

void initGpsModule(void (*debugCallback)(void),
		    void (*debug)(const char*) );

void getPmtkMsg(volatile unsigned char* returnBuffer);
void getGpsGpggaMsg(volatile unsigned char* returnBuffer);
void getGpsGpvtgMsg(volatile unsigned char* returnBuffer);


