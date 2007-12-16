/*
 * This is the parser for all messages coming from the spu
 */

void handleSpuSerial(char c, int isError);

void initSpuModule( void (*pushSwarmMsgBus)(struct SWARM_MSG msg, 
											int isInterruptCtx), 
			void (*debugCallback)(void),
		    void (*debug)(const char*) );
