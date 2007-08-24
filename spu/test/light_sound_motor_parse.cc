#include  <stdio.h>    /* Standard input/output definitions */
#include  <stdarg.h>
#include  <unistd.h>
#include  <sys/types.h>
#include  <strings.h>
#include  <ctype.h>
#include  "../include/swarmdefines.h"
#include   "../include/swarmspuutils.h"

int ParseMsg(char *msgptr,int msglen) {

int i=0;
char inchar;
char motorbuf[10];
char lnsbuf[1024];
char addrbyte[2]={0,0};
static int inmsg=0;
static int inlns=0;
static int inmtr=0;
static int msgpos=0;
static int addrcnt=0;
static int motorpos=0;
static int lnspos=0;


     memset(motorbuf,0,sizeof(motorbuf));
     memset(lnsbuf,0,sizeof(lnsbuf));

     for (i=0;i < msglen;i++) {
		inchar = *msgptr++;
	/*	printf("%c %d",inchar,i);  */
		if (inmsg ==0) {
			if (inchar='{') {
				printf("INPKT\n");
		    		inmsg=1;
				
			}
		} else {
			if (((isdigit(inchar)) >0) &&(addrcnt !=2)) {
				addrbyte[addrcnt++]=inchar;
			} else if (addrcnt==2){
					printf("INMSG\n");
					switch (inchar){
						case ' ':
							if(inlns==1) {
								lnsbuf[lnspos++]=inchar;
							}
							if (inmtr==1) {
								motorbuf[motorpos++]=inchar;
							}
							break;
						case '{':
							addrcnt=0;
							lnspos=0;
							motorpos=0;
							inlns=0;
							inmtr=0;
     							memset(motorbuf,0,sizeof(motorbuf));
     							memset(lnsbuf,0,sizeof(lnsbuf));
							break;
						case '<':
							inlns=1;
							lnsbuf[lnspos++]=inchar;
							break;
						case '>':
							inlns=2;
							lnsbuf[lnspos++]=inchar;
							break;
						case '$':
							inmtr=1;
							motorbuf[motorpos++]=inchar;
							break;
						case '*':
							inmtr=2;
							motorbuf[motorpos++]=inchar;
							break;
						case '}':
							inmsg=0;
							printf("*ENDMSG*\n");
							break;
						default:
							if (inlns==1) {
							 if( ((isalnum(inchar))>0) || ((isspace(inchar))>0))
							    	lnsbuf[lnspos++]=inchar;
							}
							if (inmtr==1) {
							  if( ((isalnum(inchar))>0) || ((isspace(inchar))>0))
							   	motorbuf[motorpos++]=inchar;
							}
							break;
					}
					if (inmsg==0) {
						if ((inlns==2) && (lnspos > 6)){
							printf("lnsbuf=%s lnspos=%d\n",lnsbuf,lnspos);
							/*send2LightNSound(&lnsbuf); */
							inlns=0;
						} 

						if ( (inmtr==2) && (motorpos >3)) {
						      printf("motorbuf=%s motorpos=%d\n",motorbuf,motorpos);
							/*send2Motor(&mtrbuf); */
							inmtr=0;
						} 

					addrcnt=0;
					inlns=0;
					inmtr=0;
					lnspos=0;
					motorpos=0;
     					memset(motorbuf,0,sizeof(motorbuf));
     					memset(lnsbuf,0,sizeof(lnsbuf));
					}
						
				} 
	     		}
			
		}									
}

int main(int argc, char **argv) {
   FILE           *in_file;    /* input file */
   char lineBuff[80];
   char *msgptr;
   
   in_file = fopen(argv[1], "r");

   fprintf(stderr,"\n Got infile name as :%s",argv[1]);
   if (in_file == NULL) {
       printf("Cannot open %s\n", argv[1]);
       exit(8);
   }
   while(fgets(lineBuff,sizeof(lineBuff),in_file) != NULL)
   {
    msgptr=lineBuff;
    ParseMsg(msgptr,sizeof(lineBuff));
   }
}
