
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
#include <getopt.h>
#include "gpsutils.h"
#include "queues.h"
#include <sys/shm.h> 
#include <sys/stat.h> 

//#define LOCAL

int parseDebug = 1; 		/*  parser uses this for debug output */

int myOrbId =0;    		/* which orb are we?  */


int com1=0; /* File descriptor for the port */
int com2=0; /* File descriptor for the port */
int com3=0, com5=0; 	/* ditto */


Queue* gpsQueuePtr;
int gpsQueueSegmentId;
struct shmid_ds gpsQueueShmidDs; 

int initSharedMem(void)
{
  //Allocate shared memory
  gpsQueueSegmentId = shmget(IPC_PRIVATE, sizeof(Queue), 
                     IPC_CREAT | IPC_EXCL | S_IRUSR | S_IWUSR); 
  //Attach shared memory
  gpsQueuePtr = (Queue *)shmat(gpsQueueSegmentId, 0, 0); 

  //read shared memory data structure
  shmctl(gpsQueueSegmentId, IPC_STAT, &gpsQueueShmidDs);
  if(2==parseDebug)
    printf("\nsegment size=%d", gpsQueueShmidDs.shm_segsz);

  return 1;//TO DO: error handling
}

void doChildProcess(void)
{
  if(2==parseDebug)
    printf("\ndoChildProcess():START");
  while(1){
    char buffer[MSG_LENGTH];
    if(pop(buffer, gpsQueuePtr)){
      //we have some thing
      if(2==parseDebug)
	printf("\ngot GPS message=%s", buffer);
      char resp[96];
      swarmGpsData gpsData;
      strncpy(gpsData.gpsSentence, buffer, MSG_LENGTH);
      int status=parseGPSSentence(&gpsData);
      if(2==parseDebug){
	printf("parseGPSSentence() return=%d\n", status);
	printf("\n Parsed line %s \n",gpsData.gpsSentence);
      }
      status = convertNMEAGpsLatLonDataToDecLatLon(&gpsData);
      if(status == SWARM_SUCCESS)
	{
	  if(2==parseDebug)
	    printf("\n Decimal lat:%lf lon:%lf utctime:%s \n",gpsData.latdd,gpsData.londd,gpsData.nmea_utctime);
           
	  decimalLatLongtoUTM(WGS84_EQUATORIAL_RADIUS_METERS, WGS84_ECCENTRICITY_SQUARED, &gpsData);
	  if(2==parseDebug)
	    printf("Northing:%f,Easting:%f,UTMZone:%s\n",gpsData.UTMNorthing,gpsData.UTMEasting,gpsData.UTMZone);
	}
      sprintf(resp, "{orb=%d\rnorthing=%f\reasting=%f\rutmzone=%s}", myOrbId,gpsData.UTMNorthing,gpsData.UTMEasting,gpsData.UTMZone);
      writeCharsToSerialPort(com2, resp, strlen(resp));

    }
    else if(2== parseDebug)
      printf("\npop returned nothing");
  }
}

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

void dispatchGpggaMsg(cmdStruct * c){
 
  if(6==parseDebug)
    printf("got gps gpgga msg: \"%s\"\n",c->cmd);
  if(push(c->cmd, gpsQueuePtr)){
    if(6==parseDebug)
      printf("\n successfully pushed GPS msg");
  }
  else if(6==parseDebug)
    printf("\n push failed. Q full");
}

void dispatchGpvtgMsg(cmdStruct * c){
  if(6==parseDebug)
    printf("got gps gpvtg msg: \"%s\"\n",c->cmd);
}

int main(int argc, char *argv[]) 
{

  /* increment this counter every time through 10 hz timeout loop */
  int tenHzticks = 0;

  /* init lemon parser here */
  void* pParser = ParseAlloc (malloc);
  int i = 0; 	
  int seconds = 0; 		/* seconds we've been running */

  int optchar=0;
  int dbgflags=0;

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

   while ((optchar = getopt (argc, argv, "gilmo:st")) != EOF)  {

             switch(optchar) {

		case 'g':
		{
		  dbgflags |= DEBUG_GPS;
		  break;
		}
		case 'i':
		{
		  dbgflags |= DEBUG_IMU;
		  break;
		}
		case 'l':
		{
		  dbgflags |= DEBUG_LED;
		  break;
	        }
		case 'm':
		{
		  dbgflags |= DEBUG_MCU;
		  break;
	        }
                case 'o':
                {
                  myOrbId = atoi(optarg);
                  break;
		}
		case 's':
		{
		  dbgflags |= DEBUG_SPU;
		  break;
		}
		case 't':
		{
		  dbgflags |= DEBUG_T;
		  break;
		}
             }
     }

printf("debug flags are %d\n",dbgflags);

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

  if(initSharedMem()){
    if (2==parseDebug)
      printf("\n shared memory initialized successfully");
  }
  else{
    printf("\n shared memory init UNSUCCESSFUL");
    return(1);
  }
  //fork now 
  pid_t pid;
  if((pid = fork())<0){
    printf("\n fork() unsuccessful");
    return(2);
  }
  else if(pid ==0)
    doChildProcess();
  else{//parent
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
	  else if(bytesRead < 0)
	    printf("\nGot error from read");
#ifdef LOCAL
	  else { /* no bytes read means EOF */
	    return(0);
	  }
#endif
	} 
      }
    }// end while(1)
  }
  return(0); 			// keep the compiler happy
}
//END main() 


