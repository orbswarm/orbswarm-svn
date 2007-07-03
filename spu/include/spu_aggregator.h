enum eGpsStates {
  eGpsStateInit,
  //  eGpsStateStartOfMsg,
  eGpsStateNMEAPayload
};

void setupNmeaCallbacks(void (*  handleGpsSerialRxStartCallback)(void),
		       void (* handleNmeaMsg)(const char* msg, long nLen));

void handleNmeaSerial(const char* buff, long nNumBytes);

void setupMcuCallbacks( void (* handleMcuMsg)(const char* msg, long nLen) );

void handleMcuSerial(const char* buff, long nNumBytes);
