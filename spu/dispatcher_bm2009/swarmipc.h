/*
 * swarmipc.h
 *
 *  Created on: Jul 27, 2008
 *      Author: niladrib
 */

#ifndef SWARMIPC_H_
#define SWARMIPC_H_

enum ECommandPipe {
	eGpsCommandPipeId = 1, eMcuCommandPipeId
};

///////////////////////////////////////////////////////////
// Semaphore to share com3 between multiple processes
/*
 * Tries to acquire lock and returns true=1 if it's successful.
 * Will also log to stderr in case of an error condition. After
 * lock is acquired should always call releaseCom3Lock() to
 * release the lock when done.
 */
int acquireCom3Lock(void);

/*
 * Tries to release lock and will write to stderr in case of an
 * error condition.
 */
void releaseCom3Lock(void);

//Same as above to read and write from the shared memory GPS structs
int acquireGpsStructLock(void);
void releaseGpsStructLock(void);

//Semaphores to synchronize access to shared memory waypoint struct
int acquireWaypointStructLock(void);
void releaseWaypointStructLock(void);
//////////////////////////////////////////////////////////


/*
 * Initializes all shared memory data structures and IPC mechanisms -
 * pipes, semaphores.
 */
int initSwarmIpc(void);

/*
 * Parent process will write out a single character instruction to
 * the pipe - usually to indicate that there is a message in one of the
 * queues(gps or mcu). This will block if the pipe is full which shouldn't
 * happen - famous last words.
 */
void tellChild(int nCommandPipeId);

/*
 * This is the other end of the pipe(see above).
 */
void waitParent(int nCommandPipeId);

/*
 * Does what it says - cleans up shared memory data structures
 */
void cleanupIPCStructs(int isParent) ;

#endif /* SWARMIPC_H_ */
