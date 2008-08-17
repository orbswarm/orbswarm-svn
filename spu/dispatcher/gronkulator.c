
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

//#define JOYSTICK

extern Queue *mcuQueuePtr;
extern swarmGpsData *latestGpsCoordinates;
extern int pfd2[2] /*mcu */;
extern int myOrbId; /* which orb are we?  */
extern int com1; /* File descriptor for the port */
extern int com2; /* File descriptor for the port */
extern int com3, com5; /* ditto */
static swarmGpsData latestGpsCoordinatesInternalCopy;

/*
 * Deep copies the values of src struct to dest in a safe
 * manner - acquires lock on semaphore first
 */
static void safeCopyGpsStruct(swarmGpsData * dest,swarmGpsData * src)
{
	if(acquireGpsStructLock()){
		fprintf(stderr, "\nstart of safe copy");
		strncpy(dest->gpsSentenceType, src->gpsSentenceType, 31);
		fprintf(stderr, "\nhere");
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
		fprintf(stderr, "\nend of safe copy");
		releaseGpsStructLock();
	}
}

void startChildProcessToGronk(void) {
	logit(eMcuLog, eLogDebug, "\n STARTING GRONK()");
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
	int thisSteeringValue;
	//double distanceToCarrot;
	double thisYawRate;
	struct swarmCircle circle;
	struct swarmFigEight figEight;
	int pathMode = 0;
	int nextPath = 2;

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

	int kalmanDataFileFD = open("sensordata", O_RDWR | O_CREAT | O_NONBLOCK
			| O_TRUNC, 0x777);
	if (kalmanDataFileFD < 0)
		perror("Failed to open sensordata");

	int kalmanResulstFileFD = open("kalmandata", O_RDWR | O_CREAT
			| O_NONBLOCK | O_TRUNC, 0x777);
	if (kalmanResulstFileFD < 0)
		perror("Failed to open kalmandata");

	int controlFileFD = open("controldata", O_RDWR | O_CREAT
			| O_NONBLOCK | O_TRUNC, 0x777);
	if (controlFileFD < 0)
		perror("Failed to open controldata");

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
			fprintf(stderr, "\nbefore safe copy");
			safeCopyGpsStruct(&latestGpsCoordinatesInternalCopy, latestGpsCoordinates);
			fprintf(stderr, "\nafter safe copy");
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

			enum {GRONK_WAITFORFIX, GRONK_BIAS, GRONK_KALMANINIT, GRONK_RUN, GRONK_COMPLETE};

			switch (gronkMode)
			{
			case GRONK_WAITFORFIX:
				// set to purple
				if(initCounter==0  && acquireCom3Lock()){
					logit(eMcuLog, eLogDebug, "\ninit state=0");
					char* msg="<LB255><LR255><LG0><LT0><LF>";
					writeCharsToSerialPort(com3, msg, strlen(msg));
					releaseCom3Lock();
					sprintf(buffer, "$p0*");
					writeCharsToSerialPort(com5, buffer, strlen(buffer) + 1);
					sprintf(buffer, "$s0*");
					writeCharsToSerialPort(com5, buffer, strlen(buffer) + 1);
					drainSerialPort(com5);


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

#ifndef JOYSTICK
					sprintf(buffer, "$p60*");
					writeCharsToSerialPort(com5, buffer, strlen(buffer) + 1);
					sprintf(buffer, "$s0*");
					writeCharsToSerialPort(com5, buffer, strlen(buffer) + 1);
					drainSerialPort(com5);
#endif
					gronkMode = GRONK_RUN;
				}

				initCounter++;
			break;

			case GRONK_RUN: // normal running mode

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

				enum {PATH_JOYSTICK, PATH_CIRCLE, PATH_FIGEIGHT, PATH_CIRCLESYNCHRO};
				switch (pathMode)
				{
					case PATH_JOYSTICK:
							if (nextPath == 1)
							{
								//carrot.x = 2000 * cos(stateEstimate.psi + PI/6);
								//carrot.y = 2000 * sin(stateEstimate.psi + PI/6);
								circle.carrotDistance = 2.0;
								circle.radius = 9.0;
								circle.direction = 1.0;
								circleInit( &stateEstimate, &circle );

								pathMode = PATH_CIRCLE;
							}
							if (nextPath == 2)
							{
								figEight.carrotDistance = 2.0;
								figEight.radius = 10.0;
								figEightInit( &stateEstimate, &figEight );

								pathMode = PATH_FIGEIGHT;
							}
							if (nextPath == 3)
							{
								//carrot.x = 2000 * cos(stateEstimate.psi + PI/6);
								//carrot.y = 2000 * sin(stateEstimate.psi + PI/6);
								circle.carrotDistance = 2.0;
								circle.radius = 9.0;
								circle.direction = 1.0;
								circle.rate = 0.1;
								circleInit( &stateEstimate, &circle );

								pathMode = PATH_CIRCLESYNCHRO;
							}
						break;

					case PATH_CIRCLE:
							circlePath( &circle, &stateEstimate, &carrot );
							swarmFeedbackProcess( &stateEstimate, &carrot, &feedback, buffer );
						break;

					case PATH_FIGEIGHT:
							figEightPath( &figEight, &stateEstimate, &carrot );
							swarmFeedbackProcess( &stateEstimate, &carrot, &feedback, buffer );
						break;
					case PATH_CIRCLESYNCHRO:
							circleSynchro( &circle, &stateEstimate, &carrot );
							swarmFeedbackProcess( &stateEstimate, &carrot, &feedback, buffer );
#ifndef JOYSTICK
							sprintf(buffer, "$p%d*", (int)rint(feedback.vDes + 50.0) );
							logit(eMcuLog, eLogDebug, buffer);
							if (1)
							{
								writeCharsToSerialPort(com5, buffer, strlen(buffer) + 1);
								drainSerialPort(com5);
							}
#endif

						break;
				}

				sprintf(controlFileBuffer, "\n%f,%f,%f,%f,%f,%f,%f %s",
						(double)nowGronkTime.tv_sec + (double)nowGronkTime.tv_usec / 1000000,
						carrot.x-stateEstimate.x,
						carrot.y-stateEstimate.y,
						circle.current.x,
						circle.current.y,
						circle.center.x,
						circle.center.y,
						buffer);
				logit(eMcuLog, eLogDebug, controlFileBuffer);

				thisSteeringValue = (int)rint(feedback.deltaDes * 190.0);
				if(thisSteeringValue > 100)
					thisSteeringValue = 100;
				if(thisSteeringValue < -100)
					thisSteeringValue = -100;

#ifndef JOYSTICK
				sprintf(buffer, "$s%d*", thisSteeringValue );
				logit(eMcuLog, eLogDebug, buffer);
				if (1)
				{
					writeCharsToSerialPort(com5, buffer, strlen(buffer) + 1);
					drainSerialPort(com5);
				}
#endif

				break;

			case GRONK_COMPLETE: // made it to goal
#ifndef JOYSTICK
				// stop drive
				sprintf(buffer, "$p0*");
				writeCharsToSerialPort(com5, buffer, strlen(buffer) + 1);
				// set steering to 0
				sprintf(buffer, "$s0*");
				writeCharsToSerialPort(com5, buffer, strlen(buffer) + 1);
				drainSerialPort(com5);
#endif
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
						logit(eMcuLog, eLogDebug,
								"\ngot mcu message from parent=%s", buffer);
						writeCharsToSerialPort(com5, buffer, strlen(buffer) + 1);
						drainSerialPort(com5);
					} else {
						logit(eMcuLog, eLogError,
								"\npop returned nothing. shouldn't be here");
					}
				} else
					logit(eMcuLog, eLogDebug,
							"\n selected data no longer available");

				if (FD_ISSET (kalmanDataFileFD, &writeSet)) {
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

				if (FD_ISSET (kalmanResulstFileFD, &writeSet)) {
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

				if (FD_ISSET (controlFileFD, &writeSet)) {
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

