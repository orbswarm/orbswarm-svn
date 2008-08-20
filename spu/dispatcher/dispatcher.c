/*
 *      File: dispatcher.c
 *      SWARM Orb SPU code http://www.orbswarm.com
 *      prototypes and #defs for swarm serial com routines
 *
 *      main loop here.
 *      read input data from COM2. Parse it and dispatch parsed commands
 *      written by Jonathan Foote (Head Rotor at rotorbrain.com)
 *      based on lots of code by Matt, Dillo, Niladri, Rick,
 *
 * How it works? - niladri.bora@gmail.com
 * The dispatcher is actually 3 processes -
 * 1. The main dispatcher process :
 * 			The main process that listens for messages coming on COM2 (via a timed
 * 	  select). Each message is tokenized by the tokenizer in doScanner() which
 * 	  in turn calls the lemon generated parser with the tokens. The grammar is
 * 	  defined in scan.y. On successful parsing of each message type the corresponding
 * 	  dispatch*()(dispatchMCUCmd(), dispatchLEDCmd(), dispatchSPUCmd(), dispatchGpsVelocityMsg()
 * 	  and dispatchGpsLocationMsg()) function is called from the action rule.
 *
 * 2. Gps message processor:
 * 			This(()) is a child process that reads messages from the gps data queue.
 * 	  The main dispatcher process writes all gps location and velocity messages
 *    (see  dispatchGpsVelocityMsg() and dispatchGpsLocationMsg()) into this queue.
 * 	  The queue is implemented using a circular buffer in shared memory. Additionally
 * 	  this process and the main dispatcher process synchronize the reading and writing to this
 * 	  queue via an IPC implemented using pipes. After pushing a message in the queue the
 *    main dispatcher process writes a character into the pipe. The gps message processor does a blocked read
 *    on the pipe and whenever it gets something it knows there is a corresponding message in the shared memory
 * 	  queue(see tellChild() and waitParent()). Each GPS location or velocity message
 * 	  is passed through the parsing routines in gpsutils.c. And the latest velocity and location
 *    data points are extracted and stored in 'latestGpsCoordinates' which is a shared memory
 *    data structure for the gronkulator to use.
 *
 * 3. Gronkulator:
 * 			This child process(started by startChildProcessToGronk())
 *    runs the 10 Hz loop that calls the Kalman Filter to
 * 	  do the estimate corrections. When the timer overflows every 100 ms(not accurate) or so
 *    it queries the daughterboard on COM5 to get the drive and steering measurements and outputs
 *    that along with the most current gps location and velocity values from the variable 'latestGpsCoordinates'
 * 	  in shared memory. The gronkulator also recieves MCU commands(sent from the remotes or the
 * 	  mothership via the aggregator) from the main dispatcher process through a shared memory queue.
 *    The communication and synchronization for this queue is done in a manner similar to that between
 *    the main dispatcher process and the gps message processor(see gps message processor above).
 *    All this happens every 10 ms tick of the timer.
 * */

#include <stdio.h>              /* Standard input/output definitions */
#include <unistd.h>
#include <getopt.h>
#include <stdlib.h>
#include <errno.h>
#include <signal.h>
#include <stdarg.h>
#include <sys/ioctl.h>
#include <sys/time.h>
#include <sys/select.h>
#include <sys/shm.h>
#include <sys/stat.h>
#include <sys/sem.h>
#include "imuutils.h"
#include "pathfollow.h"
#include "kalmanswarm.h"
#include "spuutils.h"
#include "serial.h"
#include "scanner.h"
#include "gpsutils.h"
#include "queues.h"
#include "swarmipc.h"
#include "feedback.h"
#include "gronkulator.h"

//#define LOCAL

//IMPORTANT: I think it makes life easier for everyone to have
//all externs in one file i.e. dispatcher.c or testharness.c
//                      --niladri.bora@gmail.com
//////////////////////////////////////////////////////////////////////////
int parseDebug = eMcuLog /*eDispatcherLog*/; /*  parser uses this for debug output */
int parseLevel = eLogDebug;
int myOrbId = 60; /* which orb are we?  */
int com1 = 0; /* File descriptor for the port */
int com2 = 0; /* File descriptor for the port */
int com3 = 0, com5 = 0; /* ditto */
int isParent = 1;

int gpsQueueSegmentId = -1;
struct shmid_ds gpsQueueShmidDs;
int mcuQueueSegmentId = -1;
struct shmid_ds mcuQueueShmidDs;

int latestGpsCoordinatesSegmentId = -1;
struct shmid_ds latestGpsCoordinatesShmidDs;
swarmGpsData *latestGpsCoordinates;

int waypointStructSegmentId;
struct shmid_ds waypointStructShmidDs;
struct swarmCoord *latestWaypoint;

Queue *gpsQueuePtr;
Queue *mcuQueuePtr;
int pfd1[2] /*Gps */, pfd2[2] /*mcu */;

int isLogging(int nLogArea, int nLogLevel) {
	if (eLogError == nLogLevel || (nLogLevel >= parseLevel && nLogArea
			== parseDebug))
		return 1;
	else
		return 0;
}

void logit(int nLogArea, int nLogLevel, char *strFormattedSring, ...) {
	va_list fmtargs;
	char buffer[1024];
	va_start (fmtargs, strFormattedSring);
	vsnprintf(buffer, sizeof(buffer) - 1, strFormattedSring, fmtargs);
	va_end (fmtargs);
	if (eLogError == nLogLevel) {
		fprintf(stderr, "%s", buffer);
	} else if (nLogLevel >= parseLevel && nLogArea == parseDebug)
		fprintf(stdout, "%s", buffer);
}

void cleanup(int isParent)
{
	cleanupIPCStructs(isParent);
	cleanupSpuutils();
}



static void signalHandler(int signo) {
	if (SIGTERM == signo || SIGINT == signo || SIGQUIT == signo) {
		cleanup(isParent);
		exit(EXIT_SUCCESS);
	} else{
		cleanup(isParent);
		exit(EXIT_FAILURE);
	}
}


void startChildProcessToProcessGpsMsg(void) {
	logit(eGpsLog, eLogDebug, "\ndoChildProcessToProcessGpsMsg():START");
	while (1) {
		char buffer[MSG_LENGTH];
		waitParent(eGpsCommandPipeId);
		if (pop(buffer, gpsQueuePtr)) {
			//we have some thing
			logit(eGpsLog, eLogDebug, "\ngot GPS message=%s", buffer);
			if (0 == strncmp(buffer, "GPGGA", 5)) {
				logit(eGpsLog, eLogDebug, "\n+++++++got GGA message+++++++");
				//Acquire GPS data struct lock. Be careful as to not return without
				//releasing the lock
				//IMPORTANT: You shouldn't do any IO inside the critical section
				//Turn down logging if possible
				//fprintf(stderr, "\nabout to lock gps struct because I got a new gps message");
				if(acquireGpsStructLock()){
					strncpy(latestGpsCoordinates->ggaSentence, buffer, MSG_LENGTH);
					int status = parseGPSGGASentence(latestGpsCoordinates);

//					logit(eGpsLog, eLogDebug, "parseGPSSentence() return=%d\n",
//							status);
					status = convertNMEAGpsLatLonDataToDecLatLon(
							latestGpsCoordinates);
					if (status == SWARM_SUCCESS) {
//						logit(eGpsLog, eLogDebug,
//								"\n Decimal lat:%lf lon:%lf utctime:%s \n",
//								latestGpsCoordinates->latdd,
//								latestGpsCoordinates->londd,
//								latestGpsCoordinates->nmea_utctime);

						decimalLatLongtoUTM(WGS84_EQUATORIAL_RADIUS_METERS,
								WGS84_ECCENTRICITY_SQUARED, latestGpsCoordinates);
						latestGpsCoordinates->metFromMshipNorth = latestGpsCoordinates->UTMNorthing - latestGpsCoordinates->mshipNorth;
						latestGpsCoordinates->metFromMshipEast  = latestGpsCoordinates->UTMEasting  - latestGpsCoordinates->mshipEast;
//						logit(eGpsLog, eLogDebug,
//								"Northing:%f,Easting:%f,UTMZone:%s\n",
//								latestGpsCoordinates->UTMNorthing,
//								latestGpsCoordinates->UTMEasting,
//								latestGpsCoordinates->UTMZone);
					} else {
//						logit(eGpsLog, eLogError,
//								"\ncouldn't convertNMEAGpsLatLonDataToDecLatLon status="
//								"%d", status);
					}
					releaseGpsStructLock();
				}
			} //end if GPGGA
			else if (0 == strncmp(buffer+2, "GPVTG", 5)) {
				logit(eGpsLog, eLogDebug, "\n+++++++got VTG message+++++++");
				if(acquireGpsStructLock()){
					strncpy(latestGpsCoordinates->vtgSentence, buffer, MSG_LENGTH);
					parseGPSVTGSentance(latestGpsCoordinates);
//					logit(eGpsLog, eLogDebug,
//							"\n parsed vtg sentence=%s \nreturn=%d",
//							latestGpsCoordinates->vtgSentence, nStatus);
					releaseGpsStructLock();
				}
			} else{
				logit(eGpsLog, eLogError,
						"\n+++++++got unknown message, msg=%s++++++++++++++",
						buffer);

			}
		}
			else {
			logit(eGpsLog, eLogError,
					"\npop returned nothing. shouldn't be here");
		}
	}
}

/* Parser calls this when there is a complete MCU command */
/* if the addr matches our IP, send the command str out COM2 */
void dispatchMCUCmd(int spuAddr, cmdStruct * c) {
	if (spuAddr != myOrbId)
		return;
	//blinkGreen();
	if (parseDebug == 5)
		printf("Orb %d Got MCU command: \"%s\"\n", spuAddr, c->cmd);
	//    writeCharsToSerialPort(com5, c->cmd, c->cmd_len);
	if (push(c->cmd, mcuQueuePtr)) {
		logit(eMcuLog, eLogDebug, "\n successfully pushed mcu msg");
		tellChild(eMcuCommandPipeId);
	} else {
		logit(eMcuLog, eLogWarn, "\n push failed. mcu Q full");
	}
}

/* Parser calls this when there is a complete LED command *//* if the addr matches our IP, send the command str out COM3 */
void dispatchLEDCmd(int spuAddr, cmdStruct * c) {
	if (spuAddr != myOrbId)
		return;
	if (parseDebug == 3)
		printf("Orb %d Got LED command: \"%s\"\n", spuAddr, c->cmd);
	if(acquireCom3Lock()){
		writeCharsToSerialPort(com3, c->cmd, c->cmd_len);
		releaseCom3Lock();
	}
}

/* Parser calls this when there is a complete SPU command *//* if the addr matches our IP, handle it */
void dispatchSPUCmd(int spuAddr, cmdStruct * c) {
	char resp[96];

	if (spuAddr != myOrbId)
		return;

	logit(eMcuLog, eLogInfo, "Orb %d Got SPU command: \"%s\"\n", spuAddr, c->cmd);
	/* handle the command here */
	if(0==strncmp(c->cmd, "[p?", 3)){
		fprintf(stderr, "\n about to acquire lock on gps struct before reading it for query");
		if(acquireGpsStructLock()){
			sprintf (resp, "{\n@%d p e=%f n=%f\n}",
					myOrbId, latestGpsCoordinates->metFromMshipEast,
					latestGpsCoordinates->metFromMshipNorth);
			releaseGpsStructLock();
			logit(eMcuLog, eLogInfo, "\n sending p? response to spu=%s", resp);
			writeCharsToSerialPort(com2, resp, strlen(resp));

		}
	}
	else if(0==strncmp(c->cmd, "[i?", 3)){

	}
	else if(0==strncmp(c->cmd, "[w", 2)){
		float x,y,psi,psidot,v;
		sscanf(c->cmd, "[w x=%f y=%f p=%f pdot=%f v=%f",&x,
				&y, &psi,
				&psidot, &v);
		if(acquireWaypointStructLock()){
			latestWaypoint->x=x;
			latestWaypoint->y=y;
			latestWaypoint->psi=psi;
			latestWaypoint->psidot=psidot;
			latestWaypoint->v=v;
			releaseWaypointStructLock();
			//send ack back to mo-ship
			sprintf(resp, "{\n@%d w \n}",
					myOrbId);
			logit(eMcuLog, eLogInfo, "\n sending w response to spu=%s", resp);
			writeCharsToSerialPort(com2, resp, strlen(resp));
		}
	}
	else if(0==strncmp(c->cmd, "[HALT", 5)){
		logit(eMcuLog, eLogDebug, "got HALT msg=%s", c->cmd);
		if (push(c->cmd, mcuQueuePtr)) {
			logit(eMcuLog, eLogDebug, "\n successfully pushed HALT msg");
			tellChild(eMcuCommandPipeId);
		} else {
			logit(eMcuLog, eLogWarn, "\n push failed. mcu Q full");
		}
	}
	else if(0==strncmp(c->cmd, "[MODE", 5)){
		logit(eMcuLog, eLogDebug, "got MODE msg=%s", c->cmd);
		if (push(c->cmd, mcuQueuePtr)) {
			logit(eMcuLog, eLogDebug, "\n successfully pushed MODE msg");
			tellChild(eMcuCommandPipeId);
		} else {
			logit(eMcuLog, eLogWarn, "\n push failed. mcu Q full");
		}
	}
	else
		fprintf(stderr, "ERRRRRRRRRRRRRRRROR");

}

void dispatchGpsLocationMsg(cmdStruct * c) {
	//logit(eGpsLog, eLogDebug, "got gps gpgga msg: \"%s\"\n", c->cmd);

	if (push(c->cmd, gpsQueuePtr)) {
		//logit(eGpsLog, eLogDebug, "\n successfully pushed GPS msg");
		tellChild(eGpsCommandPipeId);
	} else {
		//logit(eGpsLog, eLogWarn, "\n push failed. gps Q full");
	}
}

void dispatchGpsVelocityMsg(cmdStruct * c) {
	logit(eGpsLog, eLogDebug, "\ngot gps gpvtg msg: \"%s\"\n", c->cmd);
	if (push(c->cmd, gpsQueuePtr)) {
		logit(eGpsLog, eLogDebug, "\n successfully pushed GPS VTG msg");
		tellChild(eGpsCommandPipeId);
	} else {
		//logit(eGpsLog, eLogWarn, "\n push failed. gps Q full");
	}
}

int main(int argc, char *argv[]) {
	/* handle SIGINT, but only if it isn't ignored */
	if (signal(SIGINT, SIG_IGN) != SIG_IGN) {
		if (signal(SIGINT, signalHandler) == SIG_ERR) {
			fprintf(stderr, "\nFailed to handle SIGINT!\n");
			exit(EXIT_FAILURE);
		}
	}

	/* handle SIGQUIT, but only if it isn't ignored */
	if (signal(SIGQUIT, SIG_IGN) != SIG_IGN) {
		if (signal(SIGQUIT, signalHandler) == SIG_ERR) {
			fprintf(stderr, "\nFailed to handle SIGQUIT!\n");
			exit(EXIT_FAILURE);
		}
	}

	/*handle SIGTERM */
	if (signal(SIGTERM, signalHandler) == SIG_ERR) {
		fprintf(stderr, "\nFailed to handle SIGTERM");
		exit(EXIT_FAILURE);
	}

	/* increment this counter every time through 10 hz timeout loop */
	int tenHzticks = 0;

	/* init lemon parser here */
	void *pParser = ParseAlloc(malloc);
	//  int i = 0;
	//int seconds = 0;            /* seconds we've been running */

	int optchar = 0;
	int dbgflags = 0;

	/* vars for the select() call */
	fd_set input;
	struct timeval tv; /* store select() timeout here */
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
	char *orbAddStart = rindex(myIP, '.');
	myOrbId = atoi(&orbAddStart[1]);

	if (argc >= 3) {
		parseDebug = atoi(argv[2]);
		fprintf(stderr, "\ndispatcher:  verbose %d\n", parseDebug);
	}

	while ((optchar = getopt(argc, argv, "gilmo:st")) != EOF) {

		switch (optchar) {

		case 'g': {
			dbgflags |= DEBUG_GPS;
			break;
		}
		case 'i': {
			dbgflags |= DEBUG_IMU;
			break;
		}
		case 'l': {
			dbgflags |= DEBUG_LED;
			break;
		}
		case 'm': {
			dbgflags |= DEBUG_MCU;
			break;
		}
		case 'o': {
			myOrbId = atoi(optarg);
			break;
		}
		case 's': {
			dbgflags |= DEBUG_SPU;
			break;
		}
		case 't': {
			dbgflags |= DEBUG_T;
			break;
		}
		}
	}

	printf("debug flags are %d\n", dbgflags);

	if (parseDebug) {
		fprintf(stderr, "Dispatcher running for Orb ID: %d\n", myOrbId);
	}
#ifdef LOCAL
#warning "compiling dispatcher.c for LOCAL use (not SPU)"
	/* simulate serial i/o with files */
	com2 = initSerialPort ("bigtestinput", 0);
	com3 = initSerialPort ("./com3out", 0);
	com5 = initSerialPort ("./com5out", 0);

#else
	/* Command stream comes in on COM2 */
	com2 = initSerialPort(COM2, 38400);

	/* LED commands go out on COM3 (write only) */
	com3 = initSerialPort(COM3, 38400);

	/* Motor Control commands go to and from COM5 */
	//com5 = initSerialPort (COM5, 38400);
	com5 = initSerialPortBlocking(COM5, 115200);
#endif

	/* find maximum fd for the select() */
	int max_fd;
	max_fd = (com2 > com1 ? com2 : com1);
	max_fd = (com3 > max_fd ? com3 : max_fd);
	max_fd = (com5 > max_fd ? com5 : max_fd);
	max_fd++;

	if (initSwarmIpc()) {
		logit(eGpsLog, eLogError, "\n shared memory initialized successfully");
	} else {
		fprintf(stderr, "\n swarm IPC init UNSUCCESSFUL");
		return (1);
	}

	if (initSpuutils()){
		logit(eDispatcherLog, eLogError, "\n init spu utils successful");
	}else {
		fprintf(stderr, "\n spuutils init UNSUCCESSFUL");
		return (1);
	}
	//set up pipes
	pid_t pid;
	//First fork startChildProcessToProcessGpsMsg()
	if ((pid = fork()) < 0) {
		fprintf(stderr, "\n fork() unsuccessful");
		cleanup(isParent);
		return (2);
	} else if (pid == 0) {
		isParent = 0;
		startChildProcessToProcessGpsMsg();
	}
#if LOCAL
	//
	else
	{ //We don't run the gronkulator in LOCAL mode
#else
	else { //parent
		//Now fork gronkulator if not running in LOCAL mode
		//The gronkulator writes queries to the daughterboard/mcu com port and
		//expects to get reuslts back. This is not easy to simulate in the test
		//mode. TBD
		if ((pid = fork()) < 0) {
			fprintf(stderr, "\ngronkulator fork() unsuccessful");
			cleanup(isParent);
			return (2);
		} else if (pid == 0) {
			isParent = 0;
			startChildProcessToGronk();
		} else { //still the parent
#endif
			while (1) {
				//blinkRed();
				blinkGreen();
				/* Initialize the input set for select() */
				FD_ZERO (&input);
				FD_SET (com2, &input);
				//FD_SET(com5, &input);

				/* set select timout value */
				tv.tv_sec = 0;
				tv.tv_usec = 100000; // 100 ms or 10 Hz

				/* Do the select */
				selectResult = select(max_fd, &input, NULL, NULL, &tv);

				/* See if there was an error */
				if (selectResult < 0) {
					printf("Error during select\n");
					continue;
				}
				if (selectResult == 0) { /* select times out with a result of 0 */
					++tenHzticks;
					// if we've turned it on to signal data in
					//setSpuLed(SPU_LED_RED_OFF);
					if (tenHzticks == 5) {
						//setSpuLed(SPU_LED_GREEN_ON);
					}

					if (tenHzticks == 10) {
						tenHzticks = 0;
						//setSpuLed(SPU_LED_GREEN_OFF);
						//printf(" Runtime: %d\n", seconds++);
					}
				} else { /* we got a select; handle it */

					if (FD_ISSET (com2, &input)) {

						bytesRead = readCharsFromSerialPort(com2, buff,
								BUFLENGTH);
						if (bytesRead > 0) { /* if we actually got some data, then parse it */
							buff[bytesRead] = 0; /* null-term buffer if not done already */
							/* got some input so flash red LED for indication */
							//setSpuLed(SPU_LED_RED_ON);
							logit(eDispatcherLog, eLogInfo,
									"\nReceived \"%s\" from  com2\n", buff);
							if (isLogging(eDispatcherLog, eLogInfo))
								fflush(stdout);
							/* send bytes down to the parser. When it gets a command it
							 will call the dispatch*() functions */
							doScanner(pParser, buff);
						} else if (bytesRead < 0)
							fprintf(stderr, "\nGot error from read");
#ifdef LOCAL
						else
						{ /* no bytes read means EOF */
							cleanup (isParent);
							return (0);
						}
#endif
					}
				}
			} // end while(1)
		}
#ifndef LOCAL
	}
#endif
	return (0); // keep the compiler happy
}

//END main()
