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

int acquireCom3Lock(void);

void releaseCom3Lock(void);

int initSwarmIpc(void);

void tellWait(void);

void waitParent(int nCommandPipeId);

void tellChild(int nCommandPipeId);

void cleanupIPCStructs(int isParent) ;

#endif /* SWARMIPC_H_ */
