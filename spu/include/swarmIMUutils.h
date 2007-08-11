// ---------------------------------------------------------------------
// 
//	File: swarmIMUutils.h
//      SWARM Orb SPU code http://www.orbswarm.com
//	Utilities to parse and convert Inertial Measurement Unit (IMU) readings
//
//	Written by Dillo & Jon F
//
// -----------------------------------------------------------------------

#include <math.h>
#include "swarmdefines.h"

void imuIntToSI(struct swarmImuData *imuProcData);
double imuAccelToSI(int imuAccelInt);
double imuYawToSI(int imuYawInt);

