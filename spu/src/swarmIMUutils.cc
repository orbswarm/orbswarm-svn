
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
void calculateIMUBias(struct swarmImuData *imuData) {
  int i=0;

  imuData->ratex_bias = 0.0;
  imuData->ratey_bias = 0.0;
  imuData->accx_bias = 0.0;
  imuData->accy_bias = 0.0;
  imuData->accz_bias = 0.0;

  for(i=0;i<10;i++){
    // get 10 new IMU readings
  }
  //and divide each by 10 to get the bias value

}

void imuIntToSI(struct swarmImuData *imuData)
{

  imuData->si_ratex = imuYawToSI(imuData->int_ratex,imuData->ratex_bias);
  imuData->si_ratey = imuYawToSI(imuData->int_ratey,imuData->ratey_bias);
  imuData->si_accx = imuAccelToSI(imuData->int_accx,imuData->accx_bias);
  imuData->si_accy = imuAccelToSI(imuData->int_accy,imuData->accy_bias);
  imuData->si_accz = imuAccelToSI(imuData->int_accz,imuData->accz_bias);
}


double imuAccelToSI(int imuAccelInt, double bias)
{
  double accel_f;
  double msec=10.780000;
  
  accel_f = (double)(imuAccelInt) - bias;
  return (accel_f/1024)*msec;
}

double imuYawToSI(int imuYawInt, double bias) {
    double yaw_f;
    double msec=28.783000;

    yaw_f = (double)(imuYawInt) - bias;
    return (yaw_f/1024)*msec;
}


// print out IMU struct for debug
void dumpIMUData(struct swarmImuData *imuData) {

  printf("RateX raw: %d SI: %f bias: %f\n",
	 imuData->int_ratex,
	 imuData->si_ratex,
	 imuData->ratex_bias);
  printf("Ratey raw: %d SI: %f bias: %f\n",
	 imuData->int_ratey,
	 imuData->si_ratey,
	 imuData->ratey_bias);
  printf("Accx raw: %d SI: %f bias: %f\n",
	 imuData->int_accx,
	 imuData->si_accx,
	 imuData->accx_bias);
  printf("AccY raw: %d SI: %f bias: %f\n",
	 imuData->int_accy,
	 imuData->si_accy,
	 imuData->accy_bias);
  printf("AccZ raw: %d SI: %f bias: %f\n",
	 imuData->int_accz,
	 imuData->si_accz,
	 imuData->accz_bias);
}

