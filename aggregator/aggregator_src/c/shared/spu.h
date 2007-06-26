
enum eSpuStates {
  eSpuStateStart = 0x00,
  eSpuStateMsgType,
  eSpuStateMsgLength,
  eSpuStateMsgPayload
};

void handleSpuSerialRx(unsigned char c, int isError);
