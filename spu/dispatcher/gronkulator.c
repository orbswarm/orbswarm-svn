
// ---------------------------------------------------------------------
//
//	File: gronkulator.c
//      SWARM Orb SPU code http://www.orbswarm.com
//      prototypes and #defs for swarm serial com routines
// -----------------------------------------------------------------------


#include <stdio.h>    /* Standard input/output definitions */
#include <unistd.h>
#include <sys/ioctl.h>
#include <sys/time.h>
#include <sys/select.h>
#include "spuutils.h"
#include "serial.h"
#include <getopt.h>
#include "swarmdefines.h"
#include "scanner.h"
#include "queues.h"
#include "kalmanswarm.h"
#include "feedback.h"
#include "imuutils.h"
#include "swarmipc.h"
#include "pathfollow.h"

#define MOTORSPEED 60

// States of high level GRONK state machine
enum {GRONK_WAITFORFIX, GRONK_BIAS, GRONK_KALMANINIT, GRONK_RUN, GRONK_COMPLETE};

// States of lower level PATH state machine
enum {PATH_JOYSTICK, PATH_CIRCLE, PATH_FIGEIGHT, PATH_CIRCLESYNCHRO, PATH_FOLLOWING};

/* set this define to eliminate autonomous control for joystick-only use */
//#define JOYSTICK

/* set this define to log sensordata, kalmandata, and controldata */
#define LOGDATA


#ifdef JOYSTICK
#warning JOYSTICK mode set: motor control output is disabled
#endif

extern Queue *mcuQueuePtr;
extern swarmGpsData *latestGpsCoordinates;
extern int pfd2[2] /*mcu */;
extern int myOrbId; /* which orb are we?  */
extern int com1; /* File descriptor for the port */
extern int com2; /* File descriptor for the port */
extern int com3, com5; /* ditto */
extern struct swarmCoord *latestWaypoint;

static swarmGpsData latestGpsCoordinatesInternalCopy;

/*
 * Deep copies the values of src struct to dest in a safe
 * manner - acquires lock on semaphore first
 */
static void safeCopyGpsStruct(swarmGpsData * dest,swarmGpsData * src)
{
	if(acquireGpsStructLock()){
		//fprintf(stderr, "\nstart of safe copy");
		strncpy(dest->gpsSentenceType, src->gpsSentenceType, 31);
		//fprintf(stderr, "\nhere");
		strncpy(dest->ggaSentence, src->ggaSentence, MAX_GPS_SENTENCE_SZ);
		strncpy(dest->vtgSentence, src->vtgSentence, MAX_GPS_SENTENCE_SZ);
		strncpy(dest->nmea_utctime, src->nmea_utctime, 64);
		dest->nmea_latddmm=src->nmea_latddmm;
		dest->nmea_londdmm=src->nmea_londdmm;
		dest->nmea_latsector=src->nmea_latsector;
		dest->nmea_lonsector=src->nmea_lonsector;
		dest->latdd=src->latdd;
		dest->londd=src->londd;
		dest->utcTime=src->utcTime;
		dest->mshipNorth=src->mshipNorth;
		dest->mshipEast=src->mshipEast;
		dest->metFromMshipNorth=src->metFromMshipNorth;
		dest->metFromMshipEast=src->metFromMshipEast;
		dest->utcTimeMship=src->utcTimeMship;
		dest->UTMNorthing=src->UTMNorthing;
		dest->UTMEasting=src->UTMEasting;
		strncpy(dest->UTMZone, src->UTMZone, 32);
		dest->nmea_course=src->nmea_course;
		dest->speed=src->speed;
		dest->mode=src->mode;
		//fprintf(stderr, "\nend of safe copy");
		releaseGpsStructLock();
	}
}

void sendMotorControl ( int steeringValue, int propValue )
{
#ifndef JOYSTICK

	char buffer[MSG_LENGTH + 1];
	int limitedSteeringValue = 0;
	int limitedPropValue     = 0;

	limitedSteeringValue = steeringValue;
	limitedPropValue     = propValue;

	if(steeringValue > 100)
		limitedSteeringValue = 100;
	if(steeringValue < -100)
		limitedSteeringValue = -100;

	if(propValue > 100)
		limitedPropValue = 100;
	if(propValue < -100)
		limitedPropValue = -100;

	sprintf(buffer, "$s%d*", limitedSteeringValue );
	logit(eMcuLog, eLogDebug, buffer);
	writeCharsToSerialPort(com5, buffer, strlen(buffer) + 1);
	drainSerialPort(com5);

	sprintf(buffer, "$p%d*", limitedPropValue );
	logit(eMcuLog, eLogDebug, buffer);
	writeCharsToSerialPort(com5, buffer, strlen(buffer) + 1);
	drainSerialPort(com5);

#endif
}

void startChildProcessToGronk(void) {
	logit(eMcuLog, eLogDebug, "\n STARTING GRONK()");

// enum {PATH_JOYSTICK, PATH_CIRCLE, PATH_FIGEIGHT, PATH_CIRCLESYNCHRO, PATH_FOLLOWING};

	int pathMode = PATH_JOYSTICK;
	int nextPath = PATH_JOYSTICK;
	int logData = 0;
	struct timeval lastGronkTime;
	gettimeofday(&lastGronkTime, NULL);
	struct timeval nowGronkTime;
	struct timeval timeout;
	char buffer[MSG_LENGTH + 1];
	int i_bytesRead = 0;
	fd_set readSet;
	fd_set writeSet;
	struct swarmImuData imuData;
	struct swarmStateEstimate stateEstimate;
	char dataFileBuffer[1024];
	char outputFileBuffer[1024];
	char controlFileBuffer[1024];
	int initCounter = 0;
	int gronkMode = 0; // 4 states - 0. initializing bias 1. initializing kf 2. running 3. found goal
	struct swarmCoord carrot;
	struct swarmFeedback feedback;
	int thisSteeringValue = 0;
	int thisPropValue = 0;
	double thisYawRate;
	struct swarmCircle circle;
	struct swarmFigEight figEight;
	int kalmanDataFileFD=-1;
	int kalmanResulstFileFD=-1;
	int controlFileFD=-1;

#ifdef LOGDATA
	logData = 1;
	logit(eMcuLog, eLogDebug, "\n LOGDATA is set in Gronk");
#else
	logit(eMcuLog, eLogDebug, "\n No LOGDATA in Gronk");
#endif



#ifdef JOYSTICK
	logit(eMcuLog, eLogDebug, "\n Gronk is in JOYSTICK MODE (no motor control)");
#else
	logit(eMcuLog, eLogDebug, "\n Gronk is not in joy mode. Normal motor control.");
#endif

	zeroStateEstimates(&stateEstimate);

	if(acquireGpsStructLock()){
		latestGpsCoordinates->mshipNorth 	= 0.0; //until we get real data
		latestGpsCoordinates->mshipEast 	= 0.0; //until we get real data
		releaseGpsStructLock();
	}
	else
		fprintf(stderr, "\n gps struct lock acquire failed during init");

	swarmFeedbackInit();

	initYawSensor();

	if (logData == 1)
	{
		kalmanDataFileFD = open("sensordata", O_RDWR | O_CREAT | O_NONBLOCK
				| O_TRUNC, 0x777);
		if (kalmanDataFileFD < 0)
			perror("Failed to open sensordata");

		kalmanResulstFileFD = open("kalmandata", O_RDWR | O_CREAT
				| O_NONBLOCK | O_TRUNC, 0x777);
		if (kalmanResulstFileFD < 0)
			perror("Failed to open kalmandata");

		controlFileFD = open("controldata", O_RDWR | O_CREAT
				| O_NONBLOCK | O_TRUNC, 0x777);
		if (controlFileFD < 0)
			perror("Failed to open controldata");
	}

	while (1) {
		//logit(eMcuLog, eLogDebug, "\n running while loop");
		gettimeofday(&nowGronkTime, NULL);
		time_t deltaSecs = nowGronkTime.tv_sec - lastGronkTime.tv_sec;
		long deltaMillis = (nowGronkTime.tv_usec - lastGronkTime.tv_usec)
				/ 1000;
		if (deltaMillis < 0) {
			deltaMillis += 1000;
			deltaSecs--;
		}
		if ((deltaSecs * 1000 + deltaMillis) > GRONKULATOR_FREQ_IN_MILLIS) {
			//Time up. Query daughterboard for IMU data, gather GPS data and
			//call the Kalman Filter

			flushSerialPort(com5);
			//Read shaft encoder
			//read IMU
			writeCharsToSerialPort(com5, "$QI*", 4);
			drainSerialPort(com5);
			logit(eMcuLog, eLogInfo, "\nread resp to $QI*:START");
			i_bytesRead = readCharsFromSerialPortBlkd(com5, buffer, 91, 40); // new=91 old=115
			logit(eMcuLog, eLogInfo, "\nread resp to $QI*:END");
			logit(eMcuLog, eLogInfo, "\n IMU data=%s bytes read=%d", buffer,
					i_bytesRead);
			safeCopyGpsStruct(&latestGpsCoordinatesInternalCopy, latestGpsCoordinates);
			char parsedAndFormattedGpsCoordinates[96];
			sprintf(parsedAndFormattedGpsCoordinates,
					"{orb=%d northing=%f easting=%f utmzone=%s}", myOrbId,
					latestGpsCoordinatesInternalCopy.UTMNorthing,
					latestGpsCoordinatesInternalCopy.UTMEasting,
					latestGpsCoordinatesInternalCopy.UTMZone);
			logit(eMcuLog, eLogDebug, parsedAndFormattedGpsCoordinates);

			parseImuMsg(buffer, &imuData);

			logImuDataString(&imuData, buffer);

			thisYawRate = getYawRate();

			logit(eMcuLog, eLogInfo, "\n Yaw Gyro Raw=%f", thisYawRate);

			imuData.si_yawRate = thisYawRate;

			// enum {GRONK_WAITFORFIX, GRONK_BIAS, GRONK_KALMANINIT, GRONK_RUN, GRONK_COMPLETE};

			switch (gronkMode)
			{
			case GRONK_WAITFORFIX:
				logit(eMcuLog, eLogDebug, "\nGRONK_WAITFORFIX");
				// set to purple
				if(initCounter==0  && acquireCom3Lock()){
					logit(eMcuLog, eLogDebug, "\ninit state=0");
					char* msg="<LB255><LR255><LG0><LT0><LF>";
					writeCharsToSerialPort(com3, msg, strlen(msg));
					releaseCom3Lock();
					sendMotorControl ( 0, 0 );

				}

				if ((strncmp(latestGpsCoordinatesInternalCopy.UTMZone,
						"31Z",3) != 0) || (initCounter > 1199))
				{
					gronkMode = GRONK_BIAS;
					initCounter = 0;
				} else {
					initCounter++;
				}
			break;

			case GRONK_BIAS:  //initialize bias
				logit(eMcuLog, eLogDebug, "\nGRONK_BIAS");
				//set to red, and initialize stateEstimate
				if(initCounter==0  && acquireCom3Lock()){
					logit(eMcuLog, eLogDebug, "\ninit state=0");
					char* msg="<LB0><LR255><LG0><LT0><LF>";
					writeCharsToSerialPort(com3, msg, strlen(msg));
					releaseCom3Lock();
					initStateEstimates(&latestGpsCoordinatesInternalCopy, &imuData, &stateEstimate);
				}

				kalmanInitialBias(&latestGpsCoordinatesInternalCopy, &imuData, &stateEstimate);
				initCounter++;
				if (initCounter > 599)    // 60 sec
				//if (initCounter > 101)  // 10 sec
					gronkMode = GRONK_KALMANINIT;
			break;

			case GRONK_KALMANINIT: // found biases, now initialize kalman filter
				logit(eMcuLog, eLogDebug, "\nGRONK_KALMANINIT");
				if (initCounter > 100)
				{
					initCounter = 0;
					// stand in until we get real mship data
					// all GPS positions will be relative to starting point
					fprintf(stderr, "\nacquiring lock on gps struct before updating "
							" m ship positions");
					if(acquireGpsStructLock()){
						latestGpsCoordinates->mshipNorth =
							latestGpsCoordinatesInternalCopy.mshipNorth=
								stateEstimate.y;
						latestGpsCoordinates->mshipEast  =
							latestGpsCoordinatesInternalCopy.mshipEast  =
								stateEstimate.x;
						releaseGpsStructLock();
					}
					stateEstimate.x = 0;
					stateEstimate.y = 0;
					kalmanInit( &stateEstimate );
				}

				if (initCounter > 10){
					logit(eMcuLog, eLogDebug, "\ninit state=2");
					// set to green = go!
					if(acquireCom3Lock()){
						char* msg="<LB0><LR0><LG255><LT0><LF>";
						writeCharsToSerialPort(com3, msg, strlen(msg));
						releaseCom3Lock();
					}

					gronkMode = GRONK_RUN;
				}

				initCounter++;
			break;

			case GRONK_RUN: // normal running mode
				logit(eMcuLog, eLogDebug, "\nGRONK_RUN");

				kalmanProcess(&latestGpsCoordinatesInternalCopy, &imuData, &stateEstimate);

				stateEstimate.time = (double)nowGronkTime.tv_sec + (double)nowGronkTime.tv_usec / 1000000;

				sprintf(dataFileBuffer, "\n%f,%s,%f,%f,%f,%f,%f,%f,%s",
						(double)nowGronkTime.tv_sec + (double)nowGronkTime.tv_usec / 1000000,
						buffer,				// formatted IMU data
						latestGpsCoordinatesInternalCopy.metFromMshipEast,
						latestGpsCoordinatesInternalCopy.metFromMshipNorth,
						latestGpsCoordinatesInternalCopy.nmea_course,
						latestGpsCoordinatesInternalCopy.speed,
						imuData.omega,
						imuData.si_yawRate, // new yaw rate gyro
						latestGpsCoordinatesInternalCopy.UTMZone);
				logit(eMcuLog, eLogDebug, dataFileBuffer);
				sprintf(outputFileBuffer, "\n%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f",
						(double)nowGronkTime.tv_sec + (double)nowGronkTime.tv_usec / 1000000,
						stateEstimate.vdot,
						stateEstimate.v,
						stateEstimate.phidot,
						stateEstimate.phi,
						stateEstimate.theta,
						stateEstimate.psi,
						stateEstimate.x,
						stateEstimate.y,
						stateEstimate.xab,
						stateEstimate.yab,
						stateEstimate.zab,
						stateEstimate.xrb,
						stateEstimate.zrb,
						stateEstimate.yawb);
				logit(eMcuLog, eLogDebug, outputFileBuffer);

				// enum {PATH_JOYSTICK, PATH_CIRCLE, PATH_FIGEIGHT, PATH_CIRCLESYNCHRO, PATH_FOLLOWING};
				switch (pathMode)
				{
					case PATH_JOYSTICK:
						logit(eMcuLog, eLogDebug,
								"\nPath State: PATH_JOYSTICK");
							if (nextPath == PATH_CIRCLE)
							{
								//carrot.x = 2000 * cos(stateEstimate.psi + PI/6);
								//carrot.y = 2000 * sin(stateEstimate.psi + PI/6);
								circle.carrotDistance = 2.0;
								circle.radius = 9.0;
								circle.direction = 1.0;
								circleInit( &stateEstimate, &circle );
								if(acquireCom3Lock()){
									logit(eMcuLog, eLogDebug, "\ninit state=0");
									char* msg="<LB255><LR255><LG0><LT0><LF>";
									writeCharsToSerialPort(com3, msg, strlen(msg));
									releaseCom3Lock();
								}
								sendMotorControl ( 0, MOTORSPEED );
								swarmFeedbackInit();
								pathMode = PATH_CIRCLE;
							}
							if (nextPath == PATH_FIGEIGHT)
							{
								figEight.carrotDistance = 2.0;
								figEight.radius = 10.0;
								figEightInit( &stateEstimate, &figEight );
								if(acquireCom3Lock()){
									logit(eMcuLog, eLogDebug, "\ninit state=0");
									char* msg="<LB255><LR255><LG0><LT0><LF>";
									writeCharsToSerialPort(com3, msg, strlen(msg));
									releaseCom3Lock();
								}
								sendMotorControl ( 0, MOTORSPEED );
								swarmFeedbackInit();
								pathMode = PATH_FIGEIGHT;
							}
							if (nextPath == PATH_CIRCLESYNCHRO)
							{
								//carrot.x = 2000 * cos(stateEstimate.psi + PI/6);
								//carrot.y = 2000 * sin(stateEstimate.psi + PI/6);
								circle.carrotDistance = 2.0;
								circle.radius = 9.0;
								circle.direction = 1.0;
								circle.rate = 0.1;
								circleInit( &stateEstimate, &circle );
								if(acquireCom3Lock()){
									logit(eMcuLog, eLogDebug, "\ninit state=0");
									char* msg="<LB255><LR255><LG0><LT0><LF>";
									writeCharsToSerialPort(com3, msg, strlen(msg));
									releaseCom3Lock();
								}
								swarmFeedbackInit();
								pathMode = PATH_CIRCLESYNCHRO;
							}

							if (nextPath == PATH_FOLLOWING)
							{
								if(acquireCom3Lock()){
									logit(eMcuLog, eLogDebug, "\ninit state=0");
									char* msg="<LB255><LR255><LG0><LT0><LF>";
									writeCharsToSerialPort(com3, msg, strlen(msg));
									releaseCom3Lock();
								}
								swarmFeedbackInit();
								pathMode = PATH_FOLLOWING;
							}
						break;

					case PATH_CIRCLE:
						logit(eMcuLog, eLogDebug,
								"\nPath State: PATH_CIRCLE");
						    if (nextPath == PATH_JOYSTICK)
						    {
						    	pathMode = PATH_JOYSTICK;
								if(acquireCom3Lock()){
									char* msg="<LB0><LR0><LG255><LT0><LF>";
									writeCharsToSerialPort(com3, msg, strlen(msg));
									releaseCom3Lock();
								}
						    } else {
						    	circlePath( &circle, &stateEstimate, &carrot );
						    	swarmFeedbackProcess( &stateEstimate, &carrot, &feedback, buffer );
						    	thisSteeringValue = (int)rint(feedback.deltaDes * 190.0);
						    	sendMotorControl ( thisSteeringValue, MOTORSPEED );
						    }
						break;

					case PATH_FIGEIGHT:
						logit(eMcuLog, eLogDebug,
								"\nPath State: PATH_FIGEIGHT");
					    if (nextPath == PATH_JOYSTICK)
					    {
					    	pathMode = PATH_JOYSTICK;
					    	if(acquireCom3Lock()){
					    		char* msg="<LB0><LR0><LG255><LT0><LF>";
					    		writeCharsToSerialPort(com3, msg, strlen(msg));
					    		releaseCom3Lock();
					    	}
				    } else {
							figEightPath( &figEight, &stateEstimate, &carrot );
							swarmFeedbackProcess( &stateEstimate, &carrot, &feedback, buffer );
							thisSteeringValue = (int)rint(feedback.deltaDes * 190.0);
							sendMotorControl ( thisSteeringValue, MOTORSPEED );
					    }
						break;
					case PATH_CIRCLESYNCHRO:
						logit(eMcuLog, eLogDebug,
								"\nPath State: PATH_CIRCLESYNCHRO");
					    if (nextPath == PATH_JOYSTICK)
					    {
					    	pathMode = PATH_JOYSTICK;
					    	if(acquireCom3Lock()){
					    		char* msg="<LB0><LR0><LG255><LT0><LF>";
					    		writeCharsToSerialPort(com3, msg, strlen(msg));
					    		releaseCom3Lock();
					    	}
				    } else {
							circleSynchro( &circle, &stateEstimate, &carrot );
							swarmFeedbackProcess( &stateEstimate, &carrot, &feedback, buffer );

							thisSteeringValue = (int)rint(feedback.deltaDes * 190.0);
							sendMotorControl ( thisSteeringValue, (int)rint(feedback.vDes + 50.0) );
					    }
						break;

					case PATH_FOLLOWING:
						logit(eMcuLog, eLogDebug,
								"\nPath State: PATH_FOLLOWING");
					    if (nextPath == PATH_JOYSTICK)
					    {
					    	pathMode = PATH_JOYSTICK;
					    	if(acquireCom3Lock()){
					    		char* msg="<LB0><LR0><LG255><LT0><LF>";
					    		writeCharsToSerialPort(com3, msg, strlen(msg));
					    		releaseCom3Lock();
					    	}

					    } else {
					    	if(acquireWaypointStructLock()){
					    		logit(eMcuLog, eLogDebug, "\n waypoint vals x=%f y=%f"
									" psi=%f psidot=%f v=%f",
									latestWaypoint->x, latestWaypoint->y,
									latestWaypoint->psi, latestWaypoint->psidot, latestWaypoint->v);
								pathFollow( latestWaypoint, &stateEstimate, &carrot );
								releaseWaypointStructLock();
							}

							swarmFeedbackProcess( &stateEstimate, &carrot, &feedback, buffer );

							thisSteeringValue = (int)rint(feedback.deltaDes * 190.0);
							thisPropValue = (int)rint(feedback.vDes);

							thisSteeringValue += potFeedForward( &stateEstimate, &carrot );
							thisPropValue += propFeedForward( &stateEstimate, &carrot );

							sendMotorControl ( thisSteeringValue, thisPropValue );
					    }
					break;
				}

				if (pathMode != PATH_JOYSTICK)
				{
					sprintf(controlFileBuffer, "\n%f,%f,%f %s",
						(double)nowGronkTime.tv_sec + (double)nowGronkTime.tv_usec / 1000000,
						carrot.x-stateEstimate.x,
						carrot.y-stateEstimate.y,
						buffer);
					logit(eMcuLog, eLogDebug, controlFileBuffer);
				}
				break;

			case GRONK_COMPLETE: // made it to goal

				sendMotorControl ( 0, 0 );

				if(acquireCom3Lock()){
					char* msg="<LB255><LR255><LG255><LT0><LF>";
					writeCharsToSerialPort(com3, msg, strlen(msg));
					releaseCom3Lock();
				}

			break;
			}
			//reset timer and start over
			lastGronkTime = nowGronkTime;
		} else {
			//Not time to gronk yet. Process mcu commands
			//and write any buffered output to data file

			int nMaxFd = 0;
			FD_ZERO (&readSet);
			FD_ZERO (&writeSet);
			FD_SET (pfd2[0], &readSet);
			nMaxFd = pfd2[0];

			if (logData == 1)
			{

				if (dataFileBuffer[0] != 0) {
					FD_SET (kalmanDataFileFD, &writeSet);
					if (kalmanDataFileFD > nMaxFd)
						nMaxFd = kalmanDataFileFD;
				}
				if (outputFileBuffer[0] != 0) {
					FD_SET (kalmanResulstFileFD, &writeSet);
					if (kalmanResulstFileFD > nMaxFd)
						nMaxFd = kalmanResulstFileFD;
				}
				if (controlFileBuffer[0] != 0) {
					FD_SET (controlFileFD, &writeSet);
					if (controlFileFD > nMaxFd)
						nMaxFd = controlFileFD;
				}
			}

			nMaxFd++;

			timeout.tv_sec = 0;
			timeout.tv_usec = 10000; //10 milli secs is the smallest we can safely set..I think
			int nSelectResult = select(nMaxFd, &readSet, &writeSet, NULL,
					&timeout);
			if (nSelectResult < 0)
				logit(eMcuLog, eLogError, "\nError in select on mcu pipe");
			else if (0 == nSelectResult)
				;//logit (eMcuLog, eLogDebug, "\nselect() from gronkulator returned nothing");
			else {
				if (FD_ISSET (pfd2[0], &readSet) /*1 */) {
					waitParent(eMcuCommandPipeId); //guranteed to not block
					if (pop(buffer, mcuQueuePtr)) {
						if(0 == strncmp(buffer, "$", 1)){
							//This is a mcu command
							logit(eMcuLog, eLogDebug,
									"\ngot mcu message from parent=%s", buffer);
							writeCharsToSerialPort(com5, buffer, strlen(buffer) + 1);
							drainSerialPort(com5);
						}
						else if(0 == strncmp(buffer, "[HALT", 5)){
							logit(eMcuLog, eLogDebug,
									"\ngot HALT message from parent=%s", buffer);
							sendMotorControl ( 0, 0 );
							pathMode = PATH_JOYSTICK;
							nextPath = PATH_JOYSTICK;
						}
						else if(0 == strncmp(buffer, "[MODE", 5)){
							logit(eMcuLog, eLogDebug,
									"\ngot MODE message from parent=%s", buffer);
							int nMode;
							// enum {PATH_JOYSTICK, PATH_CIRCLE, PATH_FIGEIGHT, PATH_CIRCLESYNCHRO, PATH_FOLLOWING};
							sscanf(buffer, "[MODE %d]", &nMode);
							logit(eMcuLog, eLogDebug, "\n got MODE=%d", nMode);
							if (nMode == 0)
								nextPath = PATH_JOYSTICK;
							if (nMode == 1)
								nextPath = PATH_CIRCLE;
							if (nMode == 2)
								nextPath = PATH_FIGEIGHT;
							if (nMode == 3)
								nextPath = PATH_FOLLOWING;

						}
						else
							logit(eMcuLog, eLogError, "\ngronkulator un-expected "
									" and unhandled message=%s", buffer);
					} else {
						logit(eMcuLog, eLogError,
								"\npop returned nothing. shouldn't be here");
					}
				} else
					logit(eMcuLog, eLogDebug,
							"\n selected data no longer available");

				if ((FD_ISSET (kalmanDataFileFD, &writeSet)) && (logData ==1)) {
					//the fact that we are here means that we have something to write
					logit(eMcuLog, eLogDebug, "\nwriting to data file=%s",
							dataFileBuffer);
					char *ptr = dataFileBuffer;
					int nToWrite = strlen(ptr);
					while (nToWrite > 0) {
						int nWritten;
						if ((nWritten = write(kalmanDataFileFD, ptr, nToWrite))
								< 0)
							perror("error in writing to kalman.data");
						else if (nWritten > 0) {
							ptr += nWritten;
							nToWrite -= nWritten;
						}
						//now set buffer to "\0" to indicate it has been written to file
						dataFileBuffer[0] = 0;
					}
				}

				if ((FD_ISSET (kalmanResulstFileFD, &writeSet)) && (logData ==1)) {
					logit(eMcuLog, eLogDebug,
							"\n writing to results file=%s",
							outputFileBuffer);
					char *ptr = outputFileBuffer;
					int nToWrite = strlen(ptr);
					while (nToWrite > 0) {
						int nWritten;
						if ((nWritten = write(kalmanResulstFileFD, ptr,
								nToWrite)) < 0)
							perror("error in writing to kalman.output");
						else if (nWritten > 0) {
							ptr += nWritten;
							nToWrite -= nWritten;
						}
					}
					//now set buffer to "\0" to indicate it has been written to file
					outputFileBuffer[0] = 0;
				}

				if ((FD_ISSET (controlFileFD, &writeSet))&& (logData ==1)) {
					logit(eMcuLog, eLogDebug,
							"\n writing to results file=%s",
							controlFileBuffer);
					char *ptr = controlFileBuffer;
					int nToWrite = strlen(ptr);
					while (nToWrite > 0) {
						int nWritten;
						if ((nWritten = write(controlFileFD, ptr,
								nToWrite)) < 0)
							perror("error in writing to controldata");
						else if (nWritten > 0) {
							ptr += nWritten;
							nToWrite -= nWritten;
						}
					}
					//now set buffer to "\0" to indicate it has been written to file
					controlFileBuffer[0] = 0;
				}

			}
		}
	}
}


