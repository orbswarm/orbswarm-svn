/*
 * This is the parser for all messages coming from the spu
 */

enum ESpuStraightSerialRxStates {
  eSpuStraightSerialRxInit=0x00,
  eSpuStraightSerialRxStartMsg,
  eSpuStraightSerialRxPayload
};

void handleSpuSerial(char c, int isError);

void initSpuModule(void (*debugCallback)(void),
		    void (*debug)(const char*) );
