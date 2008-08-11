/*
 * swarmipc.c
 * This module handles all the IPC stuff for the dispatcher -
 * 1. Creating the pipes to communicate between the parent and the children
 * 2. Creating the shared mem variable to store the latest GPS co-ordinates
 * 3. Creating semaphores to share the com ports and shared memory structs
 *  Created on: Jul 27, 2008
 *      Author: niladri bora
 */
#include <stdio.h>
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
#include "swarmipc.h"
#include "swarmdefines.h"
#include "scanner.h"
#include "queues.h"
#include "scanner.h"

//defined in the dispatcher
extern int gpsQueueSegmentId;
extern struct shmid_ds gpsQueueShmidDs;
extern int mcuQueueSegmentId;
extern struct shmid_ds mcuQueueShmidDs;
extern int latestGpsCoordinatesSegmentId;
extern struct shmid_ds latestGpsCoordinatesShmidDs;
extern swarmGpsData *latestGpsCoordinates;
extern Queue *gpsQueuePtr;
extern Queue *mcuQueuePtr;
extern int pfd1[2] /*Gps */, pfd2[2] /*mcu */;

static int com3SemId;
static int gpsStructSemId;


int acquireGpsStructLock(void){
	//fprintf(stderr,  "\nacquireGpsStructLock():START");
	struct sembuf getLockOps[1];
	getLockOps[0].sem_num =0;
	getLockOps[0].sem_op = -1;
	getLockOps[0].sem_flg = 0;
	//int nStatus =semop(com3SemId, getLockOps, 1);
	if(semop(gpsStructSemId, getLockOps, 1) < 0){
		perror("\n gps struct sem acquire failed");
		//fprintf(stderr,  "\nacquireGpsStructLock():END");
		return 0;
	}
	else{
		//fprintf(stderr,  "\nacquireGpsStructLock():END");
		return 1;
	}
}

void releaseGpsStructLock(void){
	//fprintf(stderr, "\nreleaseGpsStructLock():START");
	struct sembuf releaseLockOps[1];
	releaseLockOps[0].sem_num =0;
	releaseLockOps[0].sem_op = +1;
	releaseLockOps[0].sem_flg = 0;
	if(semop(gpsStructSemId, releaseLockOps, 1) < 0)
		perror("\n gps struct sem release failed");
	//fprintf(stderr, "\nreleaseGpsStructLock():END");
}


int acquireCom3Lock(void){
	struct sembuf getLockOps[1];
	getLockOps[0].sem_num =0;
	getLockOps[0].sem_op = -1;
	getLockOps[0].sem_flg = 0;
	//int nStatus =semop(com3SemId, getLockOps, 1);
	if(semop(com3SemId, getLockOps, 1) < 0){
		perror("\n com5 sem acquire failed");
		return 0;
	}
	else
		return 1;
}

void releaseCom3Lock(void){
	struct sembuf releaseLockOps[1];
	releaseLockOps[0].sem_num =0;
	releaseLockOps[0].sem_op = +1;
	releaseLockOps[0].sem_flg = 0;
	if(semop(com3SemId, releaseLockOps, 1) < 0)
		perror("\n com5 sem release failed");
}

/*
 * Sets up the pipes to communicate between parent and children
 */
void tellWait(void) {
	if (pipe(pfd1) < 0 || pipe(pfd2) < 0) {
		fprintf(stderr, "pipe error");
		exit(EXIT_FAILURE);
	}
}


/*
 * This function initialises all the shared memory structures viz
 * 1. The shared memory queues to pass data between the parent process and the child process
 * 2. The shared memory variable to hold the most current GPS position
 * 3. The semaphores to acces the com ports
 */
int initSwarmIpc() {
	//Create semaphore for COM5
	gpsStructSemId = semget(IPC_PRIVATE, 1, S_IRUSR | S_IWUSR);
	if(gpsStructSemId < 0){
		perror("Failed to get com3 semaphore");
		return 0;
	}
	else{
		union {
			int              val;    /* Value for SETVAL */
			struct semid_ds *buf;    /* Buffer for IPC_STAT, IPC_SET */
			unsigned short  *array;  /* Array for GETALL, SETALL */
			struct seminfo  *__buf;  /* Buffer for IPC_INFO
	                                (Linux specific) */
		}  arg;
		arg.val=1;
		if(semctl(gpsStructSemId, 0, SETVAL, arg)<0){
			perror("Failed to init com3 semaphore");
			return 0;
		}
	}

	//Create semaphore for shared memory GPS struct
	gpsStructSemId = semget(IPC_PRIVATE, 1, S_IRUSR | S_IWUSR);
	if(gpsStructSemId < 0){
		perror("Failed to get com3 semaphore");
		return 0;
	}
	else{
		union {
			int              val;    /* Value for SETVAL */
			struct semid_ds *buf;    /* Buffer for IPC_STAT, IPC_SET */
			unsigned short  *array;  /* Array for GETALL, SETALL */
			struct seminfo  *__buf;  /* Buffer for IPC_INFO
	                                (Linux specific) */
		}  arg;
		arg.val=1;//available
		if(semctl(gpsStructSemId, 0, SETVAL, arg)<0){
			perror("Failed to init com3 semaphore");
			return 0;
		}
	}

    //Allocate shared memory for GPS struct that represents latest co-ordintaes
	latestGpsCoordinatesSegmentId = shmget(IPC_PRIVATE, sizeof(swarmGpsData),
			IPC_CREAT | IPC_EXCL | S_IRUSR | S_IWUSR);
	if (-1 == latestGpsCoordinatesSegmentId)
		return 0;
	//Attach
	latestGpsCoordinates = (swarmGpsData *) shmat(latestGpsCoordinatesSegmentId,
			0, 0);
	if (-1 == (int) latestGpsCoordinates)
		return 0;
	//read shared memory data structure
	if (-1 == shmctl(latestGpsCoordinatesSegmentId, IPC_STAT,
			&latestGpsCoordinatesShmidDs))
		return 0;
	logit(eDispatcherLog, eLogDebug,
			"\nsegment size for latest gps co-ord data struct=%d",
			gpsQueueShmidDs.shm_segsz);

	//Allocate shared memory for GPS data from the aggregator
	gpsQueueSegmentId = shmget(IPC_PRIVATE, sizeof(Queue), IPC_CREAT | IPC_EXCL
			| S_IRUSR | S_IWUSR);
	if (-1 == gpsQueueSegmentId)
		return 0;
	//Attach shared memory
	gpsQueuePtr = (Queue *) shmat(gpsQueueSegmentId, 0, 0);
	if (-1 == (int) gpsQueuePtr)
		return 0;
	//read shared memory data structure
	if (-1 == shmctl(gpsQueueSegmentId, IPC_STAT, &gpsQueueShmidDs))
		return 0;
	logit(eDispatcherLog, eLogDebug, "\nsegment size for gps msg queue=%d",
			gpsQueueShmidDs.shm_segsz);

	//Allocate shared memory for mcu commands coming from the aggregator
	mcuQueueSegmentId = shmget(IPC_PRIVATE, sizeof(Queue), IPC_CREAT | IPC_EXCL
			| S_IRUSR | S_IWUSR);
	if (-1 == mcuQueueSegmentId)
		return 0;
	//Attach shared memory
	mcuQueuePtr = (Queue *) shmat(mcuQueueSegmentId, 0, 0);
	if (-1 == (int) mcuQueuePtr)
		return 0;
	//read shared memory data structure
	if (-1 == shmctl(mcuQueueSegmentId, IPC_STAT, &mcuQueueShmidDs))
		return 0;
	logit(eDispatcherLog, eLogDebug, "\nsegment size for spu msg queue=%d",
			mcuQueueShmidDs.shm_segsz);
	tellWait();//init IPC pipes
	return 1;
}

void waitParent(int nCommandPipeId) {
	char c;
	if (eGpsCommandPipeId == nCommandPipeId) {
		if (read(pfd1[0], &c, 1) != 1)
			fprintf(stderr, "read error");

		if (c != 'g')
			fprintf(stderr, "WAIT_PARENT: incorrect data");
	} else if (eMcuCommandPipeId == nCommandPipeId) {
		if (read(pfd2[0], &c, 1) != 1)
			fprintf(stderr, "read error");

		if (c != 'm')
			fprintf(stderr, "WAIT_PARENT: incorrect data");
	}
}

void tellChild(int nCommandPipeId) {
	if (eGpsCommandPipeId == nCommandPipeId) {
		if (write(pfd1[1], "g", 1) != 1)
			fprintf(stderr, "shm write error");
	} else if (eMcuCommandPipeId == nCommandPipeId) {
		if (write(pfd2[1], "m", 1) != 1)
			fprintf(stderr, "shm write error");
	}
}

void cleanupIPCStructs(int isParent) {
	if (isParent && -1 != gpsQueueSegmentId) {
		shmctl(gpsQueueSegmentId, IPC_RMID, 0);
		fprintf(stderr, "\n cleaned shared mem for gps message queue");
	}
	if (isParent && -1 != mcuQueueSegmentId) {
		shmctl(mcuQueueSegmentId, IPC_RMID, 0);
		fprintf(stderr, "\n cleaned shared mem for mcu message queue");
	}
	if (isParent && -1 != latestGpsCoordinatesSegmentId) {
		shmctl(latestGpsCoordinatesSegmentId, IPC_RMID, 0);
		fprintf(stderr,
				"\n cleaned shared mem for latest gps co-ord data struct");
	}
}
