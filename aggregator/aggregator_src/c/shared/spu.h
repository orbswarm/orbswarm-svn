
enum eSpuStates {
  eSpuStateStart ,
  eSpuStateMsgType,
  eSpuStateMsgLength,
  eSpuStateMsgPayload
};

void handleSpuSerialRx(unsigned char c, int isError);

void setupSpuCallbacks(void (*spuSerialRxStartCallback)(void),
		       void (*handleSpuSwarmMsgStartCallback)(void));
