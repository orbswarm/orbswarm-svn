void  handleGpsSerial(char c, int isError, int isInterruptCtx);

void initGpsModule(void (*debugCallback)(void),
		    void (*debug)(const char*) );

void getPmtkMsg(char* returnBuffer,int isInterruptCtx);
void getGpsGpggaMsg(char* returnBuffer,int isInterruptCtx);
void getGpsGpvtgMsg(char* returnBuffer,int isInterruptCtx);


