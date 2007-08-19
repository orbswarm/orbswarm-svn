#include  <stdio.h>    /* Standard input/output definitions */
#include  <unistd.h>
#include  <sys/ioctl.h>
#include  <sys/types.h>
#include  <sys/time.h>
#include  <sys/select.h>
#include "../include/swarmserial.h"
#include "../include/swarmdefines.h"
#include "../include/swarmspuutils.h"


int main(int argc, char *argv[]) 
{
  int com1=0; /* File descriptor for the port */
  int com2=0; /* File descriptor for the port */
  int com3=0, com5=0; 	/* ditto */
  int tenHzticks = 0;
  int n;
  char buff2[MAX_BUFF_SZ + 1];

  int bytes2 = 0;
  int             max_fd;
  fd_set          input;
  struct timeval tv;

  //Get this orbs Address from its IP address
  char myIP[32];
  getIP("eth0", myIP);
  fprintf(stderr,"\nMY IP ADDRESS: %s\n",myIP);
  int myOrbId =0;
  char* orbAddStart = rindex(myIP,'.');
  myOrbId = atoi(&orbAddStart[1]);
  fprintf(stderr,"\nMY ORB ID: %d\n",myOrbId);


  com2 = initSerialPort(COM2, 38400);
  com3 = initSerialPort(COM3, 38400);
  com5 = initSerialPort(COM5, 38400);

  max_fd = (com2 > com1 ? com2 : com1) + 1;
  max_fd = (com3 > max_fd ? com3 : max_fd) + 1;
  max_fd = (com5 > max_fd ? com5 : max_fd) + 1;

  while(1)
  {
         /* Initialize the input set */
     FD_ZERO(&input);
     //FD_SET(com2, &input);
     //FD_SET(com5, &input);

     tv.tv_sec = 0; 
     tv.tv_usec = 100000; // 100 ms or 10 hz

     /* Do the select */
     n = select(max_fd, &input, NULL, NULL,&tv);

     /* See if there was an error */

     if (n <0){
       printf("Error during select\n");
       continue;
     }
     if(!n)
     {
       //printf("No data within 10 secs.");
       ++tenHzticks;
       if(tenHzticks == 5) {
	 toggleSpuLed(SPU_LED_GREEN_ON);  
       }

       if(tenHzticks == 10) {
	 tenHzticks = 0;
	 toggleSpuLed(SPU_LED_GREEN_OFF);  
       }
   
        char msgBuff[MAX_BUFF_SZ + 1];
        int msgSize = 0;
        int numTrys = 0;
        int totalBytes = 0;
        //Check to see if data is waiting on com2 
        bytes2 = 0; 
          readCharsFromSerialPort(com2, buff2, &bytes2,MAX_BUFF_SZ); 
          buff2[bytes2] = 0;
          totalBytes = bytes2;
         if(bytes2 > 0)
         {
           buff2[bytes2] = 0;
           printf("\n GOT BYTES \"%s\" from  com2\n",buff2);
           while(SWARM_SUCCESS != getMessageForDelims(msgBuff, MAX_BUFF_SZ, &msgSize,
                                   buff2, totalBytes, MSG_HEAD_AGG_STREAM,
                                   MSG_END_AGG_STREAM,true))  
           { 
              if(numTrys >= 10)
              {
                fprintf(stderr,"\n NUM TRIES EXPIRED AT: %d \n",numTrys);
                break;
              }
              usleep(5000);
              readCharsFromSerialPort(com2, &buff2[bytes2], &bytes2, MAX_BUFF_SZ - totalBytes); 
              totalBytes += bytes2;
              numTrys++;
           }

           bytes2 = totalBytes;
           buff2[bytes2] = 0;
           printf("\n GOT MESSAGE \"%s\" from  com2\n",msgBuff);
           char msgBuff2[MAX_BUFF_SZ + 1];
           int msgSize2 = 0;
           int status = SWARM_SUCCESS;
           int msgOrbId = 0; 
           totalBytes = 0;
           char dummyBuff[MAX_BUFF_SZ];
           while(SWARM_SUCCESS == status)
           {
             strncpy(dummyBuff,&msgBuff[1],3); //extract the orbid max 3 digits 
             msgOrbId = atoi(dummyBuff);  //atoi very forgiving does not complain if whitespace
            fprintf(stderr, "\nGOT MESSAGE ORB ID : %d\n",msgOrbId);
           if(msgOrbId == myOrbId)
           {
            fprintf(stderr, "\nGOT MESSAGE ORB ID MATCHED\n" );
           if(SWARM_SUCCESS == getMessageForDelims(msgBuff2, MAX_BUFF_SZ, &msgSize2,
                                   msgBuff, msgSize, MSG_HEAD_MOTOR_CONTROLER,
                                   MSG_END_MOTOR_CONTROLER,true))  
           {
              //write data to com5
              printf("\nWriting back to com5 \n");
              writeCharsToSerialPort(com5, msgBuff2, msgSize2);
           }
           if(SWARM_SUCCESS == getMessageForDelims(msgBuff2, MAX_BUFF_SZ, &msgSize2,
                                                        msgBuff, msgSize, MSG_HEAD_LIGHTING ,
                                                        MSG_END_LIGHTING ,true))  
           {
              //write data to com3
                printf("\nWriting lighting data back to com3 \n");
                writeCharsToSerialPort(com3, msgBuff2, msgSize2);
           } 
           if(SWARM_SUCCESS == getMessageForDelims(msgBuff2, MAX_BUFF_SZ, &msgSize2,
                                                        msgBuff, msgSize, MSG_HEAD_MOTHER_SHIP,
                                                        MSG_END_MOTHER_SHIP ,false))  
           {
                fprintf(stderr,"\n GOT MSHIP CMD ***%s*** \n",msgBuff2);
                if(strcmp(msgBuff2,MOTHER_SHIP_MSG_HEAD_STATUS) == 0)
                {
                  fprintf(stderr, "\nIDENTIFIED MESSAGE POLL MESSAGE\n");
                  writeCharsToSerialPort(com2, msgBuff2,msgSize2);
                }          
              /* 
                else if(strcmp(dummyBuff,MOTHER_SHIP_MSG_HEAD_TRAJECTORY) == 0)
                {
                  fprintf(stderr, "\nIDENTIFIED TRAJECTORY MESSAGE\n");
                }
                else if(strcmp(dummyBuff,MOTHER_SHIP_MSG_HEAD_LOCATION) == 0)
                {
                  fprintf(stderr, "\nIDENTIFIED  MESSAGE\n");
                }
               */
              //Mothership Data
          }
          }
          if(bytes2 > msgSize){
            totalBytes += msgSize; 
            status = getMessageForDelims(msgBuff, MAX_BUFF_SZ, &msgSize,
                                   &buff2[totalBytes],bytes2 - totalBytes, MSG_HEAD_AGG_STREAM,
                                   MSG_END_AGG_STREAM,true);  
          } 
          else
          {
            status = -1;
          }
        } 
           //printf("\n Read \"%s\" from  com2\n",buff2);
        }
              

        bytes2 = 0; 
          //Read data from com5
        readCharsFromSerialPort(com5, buff2, &bytes2,MAX_BUFF_SZ); 
        buff2[bytes2] = '\0';
        if(bytes2 > 0)
        {
          printf("\n Read data: \"%s\"  com5\n",buff2);
          writeCharsToSerialPort(com2, buff2,bytes2);
        }
      }
	 printf("main loop tick %d\n",tenHzticks);
  }
} //END main() 

    
