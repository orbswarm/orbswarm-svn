
// ---------------------------------------------------------------------
// 
//	File: swarmIMUutils.cc
//      SWARM Orb SPU code http://www.orbswarm.com
//	Utilities to parse and convert Inertial Measurement Unit (IMU) readings
//
//	Written by Dillo & Jon F
// -----------------------------------------------------------------------

#include "../include/swarmIMUutils.h"


// Average IMU data over several readings and return an estimate of bias 
//(zero reading). NOTE: ORB MUST BE STATIONARY and PERFECTLY UPRIGHT WHEN 
// THIS IS CALLED
void calculateImuBias(struct swarmImuData *imuData) {
  int i=0;

  imuData->ratex_bias = 512.0;
  imuData->ratey_bias = 512.0;
  imuData->accx_bias = 512.0;
  imuData->accy_bias = 512.0;
  imuData->accz_bias = 512.0;

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


//These convert voltage values from the IMU into m/sec^2
//These are actually condensed from the full formulae which are:
// For millivolts to acceleration in m/sec^2:
// (N-bias)/1024 * 3.3V * (9.8 m/s^2)/(0.300 V/g)
// For millivolts to yaw in m/sec^2:
// (N-bias)/1024 * 3.3V * (1 deg/s)/.002V * (Pi radians)/(180 deg)
// bias can be estimated as 512 in the absence of a measurement


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
void dumpImuData(struct swarmImuData *imuData) {
  
  printf("RateX raw: %s%d SI: %f bias: %f\n",
	 imuData->ratex_str,
	 imuData->int_ratex,
	 imuData->si_ratex,
	 imuData->ratex_bias);
  printf("Ratey raw: %s%d SI: %f bias: %f\n",
	 imuData->ratey_str,
	 imuData->int_ratey,
	 imuData->si_ratey,
	 imuData->ratey_bias);
  printf("Accx raw: %s%d SI: %f bias: %f\n",
	 imuData->accx_str,
	 imuData->int_accx,
	 imuData->si_accx,
	 imuData->accx_bias);
  printf("AccY raw: %s%d SI: %f bias: %f\n",
	 imuData->accy_str,
	 imuData->int_accy,
	 imuData->si_accy,
	 imuData->accy_bias);
  printf("AccZ raw: %s%d SI: %f bias: %f\n",
	 imuData->accz_str,
	 imuData->int_accz,
	 imuData->si_accz,
	 imuData->accz_bias);
  printf("ADC0 raw: %s%d\n",
	 imuData->adc0_str,
	 imuData->int_adc0);
  printf("ADC1 raw: %s%d\n",
	 imuData->adc1_str,
	 imuData->int_adc1);
  printf("vref raw: %s%d\n",
	 imuData->vref_str,
	 imuData->int_vref);
}

/* Parse IMU data returned from daughterboard motor controller. */
/* WARNING THIS IS NOT ROBUST: DEPENDS ON SPECIFIC ORDER OF IMU DATA VALS */
/* IF YOU CHANGE THE ORDER YOU WILL BREAK THIS ROUTINE */
int parseImuMsg(char *imuBuf, struct swarmImuData *imuData)
{
  char msg_type[10];
  char *bufpos;
  int buf_offset=0;
  int msg_data=0;
  int success=0;
  int fail = 0;
  int advance=0;
  bufpos=imuBuf;



  // FIRST Value is ADC0
  success=sscanf(imuBuf,"%s%d%n",msg_type,&msg_data,&advance);
  if (success ==2) {
    imuData->int_adc0=msg_data;
    strncpy(imuData->adc0_str,msg_type,10);
    imuBuf += advance + 1;
  } 
  else return(-1);


  // Second Value is ADC1
  success=sscanf(imuBuf,"%s%d%n",msg_type,&msg_data,&advance);
  if (success ==2) {
    imuData->int_adc1=msg_data;
    strncpy(imuData->adc1_str,msg_type,10);
    imuBuf += advance + 1;
  }
  else return(-1);

  // Third Value is RateX
  success=sscanf(imuBuf,"%s%d%n",msg_type,&msg_data,&advance);
  if (success ==2) {
    imuData->int_ratex=msg_data;
    strncpy(imuData->ratex_str,msg_type,10);
    imuBuf += advance + 1;
  } 
  else return(-1);

  // Fourth Value is RateY
  success=sscanf(imuBuf,"%s%d%n",msg_type,&msg_data,&advance);
  if (success ==2) {
    imuData->int_ratey=msg_data;
    strncpy(imuData->ratey_str,msg_type,10);
    imuBuf += advance + 1;
  }
  else return(-1);

  /* Fifth value is AccZ */
  success=sscanf(imuBuf,"%s%d%n",msg_type,&msg_data,&advance);
  if (success ==2) {
    imuData->int_accz=msg_data;
    strncpy(imuData->accz_str,msg_type,10);
    imuBuf += advance + 1;
  }
  else return(-1);

  // Sixth value is AccX
  success=sscanf(imuBuf,"%s%d%n",msg_type,&msg_data,&advance);
  if (success ==2) {
    imuData->int_accx=msg_data;
    strncpy(imuData->accx_str,msg_type,10);
    imuBuf += advance + 1;
  }
  else return(-1);

  // Seventh value is vref (SPARE)
  success=sscanf(imuBuf,"%s%d%n",msg_type,&msg_data,&advance);
  if (success ==2) {
    imuData->int_vref=msg_data;
    strncpy(imuData->vref_str,msg_type,10);
    imuBuf += advance + 1;
  }
  else return(-1);

  // last (eighth) value is AccY
  success=sscanf(imuBuf,"%s%d",msg_type,&msg_data);
  if (success ==2) {
    imuData->int_accy=msg_data;
    strncpy(imuData->accy_str,msg_type,10);
  }
  else return(-1);

  return(0);

}
