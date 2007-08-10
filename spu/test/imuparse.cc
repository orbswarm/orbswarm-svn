#include  <stdio.h>    /* Standard input/output definitions */
#include  <stdarg.h>
#include  <unistd.h>
#include  <sys/types.h>
#include  <strings.h>
#include  "../include/swarmdefines.h"
#include   "../include/swarmspuutils.h"

int parseImuMsg(char *imuBuf, struct swarmImuData *imuMsgPtr) 
{
char msg_type[6];
char *bufpos;
int buf_offset=0;
int msg_data=0;
int success=0;

	bufpos=imuBuf;
	success=sscanf(imuBuf,"%s%d",msg_type,&msg_data);
	if (success >0) { 
		imuMsgPtr->mv_adc=msg_data;
        	bufpos=(char *)memchr(imuBuf,'\n',strlen(imuBuf));
		if (bufpos != NULL) {
			buf_offset=bufpos-imuBuf+1;
			imuBuf=imuBuf+buf_offset;
		}
	} else {
		return -1;
	}
	success=sscanf(imuBuf,"%s%d",msg_type,&msg_data);
	if (success >0) {
		imuMsgPtr->mv_ratex=msg_data;
        	bufpos=(char *)memchr(imuBuf,'\n',strlen(imuBuf));
		if (bufpos != NULL) {
			buf_offset=bufpos-imuBuf+1;
			imuBuf +=buf_offset;
		}
	}

	success=sscanf(imuBuf,"%s%d",msg_type,&msg_data);
	if (success >0) {
		imuMsgPtr->mv_ratey=msg_data;
        	bufpos=(char *)memchr(imuBuf,'\n',strlen(imuBuf));
		if (bufpos != NULL) {
			buf_offset=bufpos-imuBuf+1;
			imuBuf=imuBuf+buf_offset;
		}
	}
	success=sscanf(imuBuf,"%s%d",msg_type,&msg_data);
	if (success >0) {
		imuMsgPtr->mv_accz=msg_data;
        	bufpos=(char *)memchr(imuBuf,'\n',strlen(imuBuf));
		if (bufpos != NULL) {
			buf_offset=bufpos-imuBuf+1;
			imuBuf=imuBuf+buf_offset;
		}
	}

	success=sscanf(imuBuf,"%s%d",msg_type,&msg_data);
	if (success >0) {
		imuMsgPtr->mv_accx=msg_data;
        	bufpos=(char *)memchr(imuBuf,'\n',strlen(imuBuf));
		if (bufpos != NULL) {
		buf_offset=bufpos-imuBuf+1;
		imuBuf=imuBuf+buf_offset;
		}
	}
	success=sscanf(imuBuf,"%s%d",msg_type,&msg_data);
	if (success >0) {
		imuMsgPtr->mv_spare=msg_data;
        	bufpos=(char *)memchr(imuBuf,'\n',strlen(imuBuf));
		if (bufpos != NULL) {
			buf_offset=bufpos-imuBuf+1;
			imuBuf=imuBuf+buf_offset;
		}
	}
	success=sscanf(imuBuf,"%s%d",msg_type,&msg_data);
	if (success >0) {
		imuMsgPtr->mv_accy=msg_data;
	}	
	return 0;
}

int main(int argc, char **argv) {

struct  swarmImuData imuMsg;
struct  swarmImuData *imuMsgPtr;
char *imubuf="ADC1:  +00019 \nRATEX:  +00019 \nRATEY:  +00013 \nACCZ:  +00018 \nACCX:  +00017 \nSPARE:  +00021 \nACCY:  +00021";

   imuMsgPtr=&imuMsg;

   parseImuMsg(imubuf,imuMsgPtr);
   imuMvToSI(imuMsgPtr);
   printf("RATEX: %4.10F\nRATEY:%4.10F\nACCZ:%4.10F\nACCX:%4.10F\nACCY:%4.10F\n",imuMsgPtr->si_ratex,imuMsgPtr->si_ratey,imuMsgPtr->si_accz,imuMsgPtr->si_accx,imuMsgPtr->si_accy);
return 0;   
}
