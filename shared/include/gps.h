enum EGpsStraightSerialRxStates {
  eGpsStraightSerialRxInit,
  eGpsStraightSerialRxPayload,//$
  eGpsStraightSerialRxPayloadG,//$G
  eGpsStraightSerialRxPayloadGP,//$GP
  eGpsStraightSerialRxPayloadGPG,
  eGpsStraightSerialRxPayloadGPGG,
  eGpsStraightSerialRxPayloadGPGGA,
  eGpsStraightSerialRxDiscard
};

void  handleGpsSerial(unsigned char c, int isError);

void initGpsModule(void (*debugCallback)(void),
		    void (*debug)(const char*) );


