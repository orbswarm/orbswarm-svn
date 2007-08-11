
// ---------------------------------------------------------------------
// 
//	File: swarmIMUutils.cc
//      SWARM Orb SPU code http://www.orbswarm.com
//	Utilities to parse and convert Inertial Measurement Unit (IMU) readings
//
//	Written by Dillo & Jon F
// -----------------------------------------------------------------------
#include "../include/swarmIMUutils.h"

//These convert voltage values from the IMU into m/sec^2
//These are actually condensed from the full formulae which are:
// For millivolts to acceleration in m/sec^2:
// (N-512)/1024 * 3.3V * (9.8 m/s^2)/(0.300 V/g)
// For millivolts to yaw in m/sec^2:
// (N-512)/1024 * 3.3V * (1 deg/s)/.002V * (Pi radians)/(180 deg)
//
//These are useful to know because various components might need to be
//tweaked in the future to accomodate for real-world detected noise,
// bias, etc.

// Average IMU data over several readings and return an estimate of bias 
//(zero reading). NOTE: ORB MUST BE STATIONARY and PERFECTLY UPRIGHT WHEN 
// THIS IS CALLED
void calculateIMUBias(struct swarmImuData *imuProcData) {



}

void imuIntToSI(struct swarmImuData *imuData)
{

  imuData->si_ratex = imuYawToSI(imuData->mv_ratex);
  imuData->si_ratey = imuYawToSI(imuData->mv_ratey);
  imuData->si_accz = imuAccelToSI(imuData->mv_accz);
  imuData->si_accx = imuAccelToSI(imuData->mv_accx);
  imuData->si_accy = imuAccelToSI(imuData->mv_accy);
}

void dumpImuData

double imuAccelToSI(int imuAccelInt)
{
    double accel;
    double mv_accel_f;
    double msec=10.780000;

        mv_accel_f = imuAccelInt;
        accel = ((mv_accel_f-512)/1024)*msec;
        return accel;
}

double imuYawToSI(int imuYawInt) {
    double yaw;

    double mv_yaw_f;
    double msec=28.783000;

    mv_yaw_f = imuYawInt;

    yaw = ((mv_yaw_f-512)/1024)*msec;
    return yaw;
}


