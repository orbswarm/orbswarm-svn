void  handleXbeeSerial(char c, int isError);

void initXbeeModule( void (*pushSwarmMsgBus)(struct SWARM_MSG msg, 
					int isInterruptCtx),
			void (*debugCallback)(void),
		    void (*debug)(const char*) );

