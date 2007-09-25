
// ---------------------------------------------------------------------
// 
//	File: dispatcher.c
//      SWARM Orb SPU code http://www.orbswarm.com
//      prototypes and #defs for swarm serial com routines
//
//      main loop here. 
//      read input data from COM2. Parse it and dispatch parsed commands
//      written by Jonathan Foote (Head Rotor at rotorbrain.com)
//      based on lots of code by Matt, Dillo, Niladri, Rick, 
// -----------------------------------------------------------------------

#include <stdio.h>    /* Standard input/output definitions */
#include <unistd.h>
#include <sys/ioctl.h>
#include <sys/time.h>
#include <sys/select.h>
#include "spuutils.h"
#include "serial.h"
#include "scanner.h"



int parseDebug = 1; 		/*  parser uses this for debug output */


int myOrbId =0;    		/* which orb are we?  */


int com1=0; /* File descriptor for the port */
int com2=0; /* File descriptor for the port */
int com3=0, com5=0; 	/* ditto */


/* Parser calls this when there is a complete MCU command */
/* if the addr matches our IP, send the command str out COM2 */
void dispatchMCUCmd(int spuAddr, cmdStruct *c){
  if(spuAddr != myOrbId)
    return;
  if(parseDebug == 5) 
    printf("Orb %d Got MCU command: \"%s\"\n",spuAddr, c->cmd);
  writeCharsToSerialPort(com5, c->cmd, c->cmd_len);
}

/* Parser calls this when there is a complete LED command */
/* if the addr matches our IP, send the command str out COM3 */
void dispatchLEDCmd(int spuAddr, cmdStruct *c){
  if(spuAddr != myOrbId)
    return;
  if(parseDebug == 3)
    printf("Orb %d Got LED command: \"%s\"\n",spuAddr, c->cmd);
  writeCharsToSerialPort(com3, c->cmd, c->cmd_len);
}

/* Parser calls this when there is a complete SPU command */
/* if the addr matches our IP, handle it */
void dispatchSPUCmd(int spuAddr, cmdStruct *c){
  if(spuAddr != myOrbId)
    return;
  
  if(parseDebug == 4) 
    printf("Orb %d Got SPU command: \"%s\"\n",spuAddr, c->cmd);
  /* handle the command here */

}


int main(int argc, char *argv[]) 
{

  /* increment this counter every time through 10 hz timeout loop */
  int tenHzticks = 0;

  /* init lemon parser here */
  void* pParser = ParseAlloc (malloc);
  int i = 0; 	
  int seconds = 0; 		/* seconds we've been running */

  /* vars for the select() call */
  int             max_fd;
  fd_set          input;
  struct timeval tv; 		/* store select() timeout here */
  int selectResult;

  /* store input data from COM2 here */
  char buff[BUFLENGTH + 1];
  int bytesRead = 0;  

  //Get this orbs Address from its IP address
  char myIP[32];
  getIP("eth0", myIP);

  printf("\ndispatcher gotIP\n");
  //if(enableDebug)
    //fprintf(stderr,"\nMY IP ADDRESS: %s\n",myIP);
  char* orbAddStart = rindex(myIP,'.');
  myOrbId = atoi(&orbAddStart[1]);
  
  if(argc >= 3){
    parseDebug = atoi(argv[2]);
    fprintf(stderr,"\ndispatcher:  verbose %d\n", parseDebug);
  }

  if(parseDebug) {
    fprintf(stderr,"Dispatcher running for Orb ID: %d\n",myOrbId);
  }

#ifdef LOCAL
#warning "compiling dispatcher.c for LOCAL use (not SPU)"
  /* simulate serial i/o with files */
  com2 = initSerialPort("bigtestinput", 0);
  com3 = initSerialPort("./com3out", 0);
  com5 = initSerialPort("./com5out", 0);

#else
  /* Command stream comes in on COM2 */
  com2 = initSerialPort(COM2, 38400);

  /* LED commands go out on COM3 (write only) */
  com3 = initSerialPort(COM3, 38400);

  /* Motor Control commands go to and from COM5 */
  com5 = initSerialPort(COM5, 38400);
#endif  


  /* find maximum fd for the select() */
  max_fd = (com2 > com1 ? com2 : com1) + 1;
  max_fd = (com3 > max_fd ? com3 : max_fd) + 1;
  max_fd = (com5 > max_fd ? com5 : max_fd) + 1;
  
  while(1){
    
    
    /* Initialize the input set for select() */
    FD_ZERO(&input);
    FD_SET(com2, &input);
    //FD_SET(com5, &input);
    
    /* set select timout value */
    tv.tv_sec = 0; 
    tv.tv_usec = 100000; // 100 ms or 10 hz
    
    /* Do the select */
    selectResult = select(max_fd, &input, NULL, NULL,&tv);
    
    /* See if there was an error */
    if (selectResult <0){
      printf("Error during select\n");
      continue;
    }
    if(selectResult==0) { 	/* select times out with a result of 0 */
       ++tenHzticks;
       // if we've turned it on to signal data in
       setSpuLed(SPU_LED_RED_OFF);  
       if(tenHzticks == 5) {
	 setSpuLed(SPU_LED_GREEN_ON);  
       }
       
       if(tenHzticks == 10) {
	 tenHzticks = 0;
	 setSpuLed(SPU_LED_GREEN_OFF);  
	 printf(" Runtime: %d\n",seconds++);
       } 
    }
    else { /* we got a select; handle it */
      
       if (FD_ISSET(com2, &input)){
	 
	 bytesRead = readCharsFromSerialPort(com2, buff,BUFLENGTH); 
	 
	 
         if(bytesRead > 0){ 	/* if we actually got some data, then parse it */
	   buff[bytesRead] = 0; 	 /* null-term buffer if not done already */
	   /* got some input so flash red LED for indication */
	   setSpuLed(SPU_LED_RED_ON);  
	   if(parseDebug == 2){
	     printf("\nReceived \"%s\" from  com2\n",buff);
	     fflush(stdout);
	   }
	   /* send bytes down to the parser. When it gets a command it
	      will call the dispatch*() functions */				
	   for(i=0;i<bytesRead;i++){
	     doScanner(pParser,(int)buff[i]);
	   }
	 }
#ifdef LOCAL
	 else { /* no bytes read means EOF */
	   return(0);
	 }
#endif
       } 
    }
  }// end while(1)
  return(0); 			// keep the compiler happy
}
//END main() 


