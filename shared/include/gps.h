enum EGpsStraightSerialRxStates {
  eGpsStraightSerialRxInit,
  eGpsStraightSerialRxStartMsg,
  eGpsStraightSerialRxPayload
};

//void initRxBuffers(void); 

//void handleXBeeRx(char c, int isError);

void  handleGpsSerial(unsigned char c, int isError);

void initGpsModule(void (*debugCallback)(void),
		    void (*debug)(const char*) );

