// ---------------------------------------------------------------------
// 
//	File: swarmIMUutils.h
//      SWARM Orb SPU code http://www.orbswarm.com
//	Utilities to parse and convert Inertial Measurement Unit (IMU) readings
//
//	Written by Dillo & Jon F
//
// -----------------------------------------------------------------------
#include<stdio.h>
#include <math.h>
#include <math.h>
#include "swarmdefines.h"

void dumpIMUData(struct swarmImuData *imuData);
void calculateIMUBias(struct swarmImuData *imuProcData);
void imuIntToSI(struct swarmImuData *imuProcData);
double imuAccelToSI(int imuAccelInt, double bias);
double imuYawToSI(int imuYawInt, double bias);

