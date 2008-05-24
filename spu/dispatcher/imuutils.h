// ---------------------------------------------------------------------
// 
//	File: imuutils.h
//      SWARM Orb SPU code http://www.orbswarm.com
//	Utilities to parse and convert Inertial Measurement Unit (IMU) readings
//
//	Written by Dillo & Jon F
//
// -----------------------------------------------------------------------
#include <stdio.h>
#include <math.h>
#include <sys/mman.h>
#include <math.h>
#include <string.h>
#include "swarmdefines.h"


void dumpImuData(struct swarmImuData *imuData);
void calculateImuBias(struct swarmImuData *imuProcData);
void imuIntToSI(struct swarmImuData *imuProcData);
double imuAccelToSI(int imuAccelInt, double bias);
double imuYawToSI(int imuYawInt, double bias);
int parseImuMsg(char *imuBuf, struct swarmImuData *imuData);
int parseSteerMsg(char *steerBuf, struct swarmMotorData *motData);
int parseDriveMsg(char *driveBuf, struct swarmMotorData *motData);
void dumpMotorData(struct swarmMotorData *motData);
void logImuDataString(struct swarmImuData *imuData, char *imuDataString);
