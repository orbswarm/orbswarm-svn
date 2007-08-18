#include <stdio.h>			/* printf			*/
#include <string.h>
#include <stdlib.h>
#include "../include/swarmdefines.h"
#include "../include/swarmspuutils.h"
#include "../include/swarmserial.h"

int main(int argc, char *argv[]) 
{
     fprintf(stderr,"\nBEGIN TEST PARSE AGGREGATOR DATA STREAM\n");
     //Some raw aggregator test data
     char * testQueryMotorControl = "$?*";
     char * testEffects = "<This is a sample effects cmd>";
     char * combinedMsg = "$test mot control*<effects msg>";
     char * testMsgPartial1 = "$test mot control*<";
     char * testMsgPartial2 = "effects msg>";
     char * testMsgPartial3 = "$test mot";
     char * testMsgPartial4 = " control*";

     char msgBuff[MAX_BUFF_SZ +1];
     int status = SWARM_SUCCESS;     
     int msgType = -1;
     int msgSize = 0;
      
     status = getMessageForDelims(msgBuff, MAX_BUFF_SZ + 1, &msgSize,testQueryMotorControl,
                                    3, MSG_HEAD_MOTOR_CONTROLER , MSG_END_MOTOR_CONTROLER,true); 
     //status = parseMessageStream(msgBuff, MAX_BUFF_SZ + 1, &msgType, &msgSize,
     //          testQueryMotorControl , 3); 
     fprintf(stderr,"\nTEST 1 GOT :%s",msgBuff);
     
     //memset(msgBuff,0,MAX_BUFF_SZ +1);
     status = getMessageForDelims(msgBuff, MAX_BUFF_SZ + 1, &msgSize, testEffects,
                                    strlen(testEffects), MSG_HEAD_LIGHTING, MSG_END_LIGHTING,true); 
     //status = parseMessageStream(msgBuff, MAX_BUFF_SZ + 1, &msgType, &msgSize,
     //          testEffects, strlen(testEffects)); 
     fprintf(stderr,"\nTEST 2 GOT :%s",msgBuff);

     status = getMessageForDelims(msgBuff, MAX_BUFF_SZ + 1 , &msgSize, combinedMsg,
                                    strlen(combinedMsg), MSG_HEAD_MOTOR_CONTROLER ,MSG_END_MOTOR_CONTROLER,true); 
     //status = parseMessageStream(msgBuff, MAX_BUFF_SZ + 1, &msgType, &msgSize,
              //combinedMsg, strlen(combinedMsg)); 
     fprintf(stderr,"\nTEST 3 GOT :%s",msgBuff);
     if(msgSize < strlen(combinedMsg))
     {
     status = getMessageForDelims(msgBuff, MAX_BUFF_SZ + 1 , &msgSize, &combinedMsg[msgSize],
                                    strlen(combinedMsg) - msgSize, MSG_HEAD_LIGHTING, MSG_END_LIGHTING,true); 
       //status = parseMessageStream(msgBuff, MAX_BUFF_SZ + 1, &msgType, &msgSize,
                //&combinedMsg[msgSize], strlen(combinedMsg) - msgSize); 
        fprintf(stderr,"\nTEST 3 PART 2 GOT :%s",msgBuff);
     }

     status = getMessageForDelims(msgBuff, MAX_BUFF_SZ + 1 , &msgSize, testMsgPartial1,
                                    strlen(testMsgPartial1), MSG_HEAD_MOTOR_CONTROLER ,MSG_END_MOTOR_CONTROLER,true); 
     //status = parseMessageStream(msgBuff, MAX_BUFF_SZ + 1, &msgType, &msgSize,
     //         testMsgPartial1 , strlen(testMsgPartial1)); 
     fprintf(stderr,"\nTEST 4 GOT :%s",msgBuff);
     if(msgSize < strlen(testMsgPartial1))
     {
     status = getMessageForDelims(msgBuff, MAX_BUFF_SZ + 1 , &msgSize, &testMsgPartial1[msgSize],
                                    strlen(testMsgPartial1) - msgSize, MSG_HEAD_LIGHTING, MSG_END_LIGHTING,true); 
       //status = parseMessageStream(msgBuff, MAX_BUFF_SZ + 1, &msgType, &msgSize,
       //         &testMsgPartial1[msgSize], strlen(testMsgPartial1) - msgSize); 
        if(status == AGGR_MSG_HEADER_WITHOUT_FOOTER_ERROR)
           fprintf(stderr,"\nTEST 4 PART 2 SUCCESS");
     }
     status = getMessageForDelims(msgBuff, MAX_BUFF_SZ + 1 , &msgSize, testMsgPartial2,
                                    strlen(testMsgPartial2), MSG_HEAD_LIGHTING, MSG_END_LIGHTING,true); 
     if(status == AGGR_MSG_FOOTER_WITHOUT_HEADER_ERROR)
        fprintf(stderr,"\nTEST 5 SUCCESS");

     //Test that we can packetize a char array with no null terminating character
     unsigned char testBuff2[1024] = {'{','$','?','*',';','$','?','*',';','$','?','*',';','$','?','*',';','$','?','*',';','$','?','*',';','$','?','*',';','$','?','*',';','$','?','*',';','$','?','*',';','$','?','*',';','$','?','*',';','$','?','*',';','$','?','*',';','$','?','*',';','$','?','*',';','$','?','*',';','$','?','*',';','$','?','*',';','$','?','*',';','$','?','*',';','$','?','*',';','$','?','*',';','$','?','*',';','$','?','*',';','$','?','*',';','$','?','*',';','$','?','*',';','$','?','*',';','$','?','*',';','$','?','*',';','$','?','*',';','$','?','*',';','$','?','*',';','$','?','*',';','$','?','*',';','$','?','*',';','$','?','*',';','$','?','*',';','$','?','*',';','$','?','*',';','$','?','*',';','$','?','*',';','$','?','*',';','$','?','*',';','$','?','*',';','$','?','*',';','$','?','*',';','$','?','*',';','$','?','*',';','$','?','*',';','$','?','*',';','}'};
     int testBuff2Sz = 210;
     status = getMessageForDelims(msgBuff, MAX_BUFF_SZ + 1 , &msgSize, (char*)testBuff2,
                                    testBuff2Sz, MSG_HEAD_AGG_STREAM, MSG_END_AGG_STREAM,false); 
     fprintf(stderr,"\nTEST 6 GOT :%s",msgBuff);
     
     fprintf(stderr,"\nEND TEST PARSE AGGREGATOR DATA STREAM\n");
     
 return 0;
}
