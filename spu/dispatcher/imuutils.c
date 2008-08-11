
// ---------------------------------------------------------------------
//
//	File: imuutils.c
//      SWARM Orb SPU code http://www.orbswarm.com
//	Utilities to parse and convert Inertial Measurement Unit (IMU) readings
//
//	Written by Dillo & Jon F
// -----------------------------------------------------------------------

#include "imuutils.h"

#include "spi.c"
#include "scanner.h"

#include <unistd.h>

// defines for SPI Com
#define DIOBASE 0xE8000000
#define CS_PIN 7
#define RW_REG(ptr) *(ptr + 0x08/sizeof(unsigned int))

// module variables
int spiFd;
volatile unsigned int *dioptr;

double countsToRPS(int counts);

// Average IMU data over several readings and return an estimate of bias
//(zero reading). NOTE: ORB MUST BE STATIONARY and PERFECTLY UPRIGHT WHEN
// THIS IS CALLED
void calculateImuBias(struct swarmImuData *imuData) {
  int i=0;

  imuData->ratex_bias = 512.0;
  imuData->ratez_bias = 512.0;
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
  imuData->si_ratez = imuYawToSI(imuData->int_ratez,imuData->ratez_bias);
  imuData->si_accx = imuAccelToSI(imuData->int_accx,imuData->accx_bias);
  imuData->si_accy = imuAccelToSI(imuData->int_accy,imuData->accy_bias);
  imuData->si_accz = imuAccelToSI(imuData->int_accz,imuData->accz_bias);
}


//These convert voltage values from the IMU into m/sec^2
//These are actually condensed from the full formulae which are:
// For millivolts to acceleration in m/sec^2:
// (N-bias)/1024 * 3.3V * (9.81 m/s^2)/(0.300 V/g)
// For millivolts to yaw in m/sec^2:
// (N-bias)/1024 * 3.3V * (1 deg/s)/.002V * (Pi radians)/(180 deg)
// bias can be estimated as 512 in the absence of a measurement


double imuAccelToSI(int imuAccelInt, double bias)
{
  double accel_f;
  double msec=107.9;

  accel_f = (double)(imuAccelInt) - bias;
  return (accel_f/1024)*msec;
}

double imuYawToSI(int imuYawInt, double bias) {
    double yaw_f;
    double msec=28.783000;

    yaw_f = (double)(imuYawInt) - bias;
    return (yaw_f/1024)*msec;
}

void logImuDataString(struct swarmImuData *imuData, char *imuDataString) {

  sprintf(imuDataString, "%f,%f,%f,%f,%f", imuData->si_ratex, imuData->si_ratez ,
		imuData->si_accx , imuData->si_accy , imuData->si_accz );
}

void logDriveDataString(struct swarmMotorData *motorData, char *motorDataString) {
	sprintf(motorDataString, "%d,%d,%d,%d,%d,%f",motorData->driveTarget,motorData->driveActual,motorData->drivePWM,motorData->odometer,motorData->rawCurrent,motorData->speedRPS);

}

void logSteerDataString(struct swarmMotorData *motorData, char *motorDataString) {
	sprintf(motorDataString, "%d,%d,%d", motorData->steerTarget, motorData->steerActual, motorData->steerPWM);

}
// print out IMU struct for debug
void dumpImuData(struct swarmImuData *imuData) {

  printf("RateX raw: %s%d SI: %f bias: %f\n",
	 imuData->ratex_str,
	 imuData->int_ratex,
	 imuData->si_ratex,
	 imuData->ratex_bias);
  printf("RateZ raw: %s%d SI: %f bias: %f\n",
	 imuData->ratez_str,
	 imuData->int_ratez,
	 imuData->si_ratez,
	 imuData->ratez_bias);
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
/* parse return to $QI* query IMU message */
int parseImuMsg(char *imuBuf, struct swarmImuData *imuData)
{
  char msg_type[10];		// temp storage for scanned string
  int msg_data=0;		// temp storage for scanned int data
  int success=0;		// how many fields have we read from sscanf
  int advance=0; 		// advance this many chars after each sscanf


  // First Value is RateX
  success=sscanf(imuBuf,"%s%d%n",msg_type,&msg_data,&advance);
  if (success ==2) {
    imuData->int_ratex=msg_data;
    strncpy(imuData->ratex_str,msg_type,10);
    imuBuf += advance + 1;
  }
  else return(-1);

  // Second Value is RateZ
  success=sscanf(imuBuf,"%s%d%n",msg_type,&msg_data,&advance);
  if (success ==2) {
    imuData->int_ratez=msg_data;
    strncpy(imuData->ratez_str,msg_type,10);
    imuBuf += advance + 1;
  }
  else return(-1);

  // Third value is AccX
  success=sscanf(imuBuf,"%s%d%n",msg_type,&msg_data,&advance);
  if (success ==2) {
    imuData->int_accx=msg_data;
    strncpy(imuData->accx_str,msg_type,10);
    imuBuf += advance + 1;
  }
  else return(-1);

  // Fourth value is AccY
  success=sscanf(imuBuf,"%s%d%n",msg_type,&msg_data,&advance);
  if (success ==2) {
    imuData->int_accy=msg_data;
    strncpy(imuData->accy_str,msg_type,10);
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

  // sixth value is drive actual speed
  success=sscanf(imuBuf,"%s%d",msg_type,&msg_data);
  if (success ==2) {;
    imuData->omega = countsToRPS(msg_data);
  }
  else return(-2);

  calculateImuBias(imuData);

  imuIntToSI(imuData);

  return(0);
}

/* Parse steering motor data returned from daughterboard motor controller. */
/* WARNING THIS IS NOT ROBUST: DEPENDS ON SPECIFIC ORDER OF DATA VALS */
/* IF YOU CHANGE THE ORDER YOU WILL BREAK THIS ROUTINE */
/* parse return to $QS* query steering message */
int parseSteerMsg(char *steerBuf, struct swarmMotorData *motData)
{
  char msg_type[10];		// temp storage for scanned string
  int msg_data=0;		// temp storage for scanned int data
  int success=0;		// how many fields have we read from sscanf
  int advance=0; 		// advance this many chars after each sscanf


  // first value is steer target
  success=sscanf(steerBuf,"%s%d%n",msg_type,&msg_data,&advance);
  if (success ==2) {
    motData->steerTarget=msg_data;
    strncpy(motData->steerTarget_str,msg_type,10);
    steerBuf += advance + 1;
  }
  else return(-1);

  // second value is steer actual
  success=sscanf(steerBuf,"%s%d%n",msg_type,&msg_data,&advance);
  if (success ==2) {
    motData->steerActual=msg_data;
    strncpy(motData->steerActual_str,msg_type,10);
    steerBuf += advance + 1;
  }
  else return(-2);
  // third val is raw pwm out
  success=sscanf(steerBuf,"%s%d%n",msg_type,&msg_data,&advance);
  if (success ==2) {
    motData->steerPWM=msg_data;
    strncpy(motData->steerPWM_str,msg_type,10);
    steerBuf += advance + 1;
  }
  else return(-3);
//dummy return
  return 0;
}

/* Parse steering motor data returned from daughterboard motor controller. */
/* WARNING THIS IS NOT ROBUST: DEPENDS ON SPECIFIC ORDER OF DATA VALS */
/* IF YOU CHANGE THE ORDER YOU WILL BREAK THIS ROUTINE */
/* parse return to $QD* query drive message */
int parseDriveMsg(char *driveBuf, struct swarmMotorData *motData)
{
  char msg_type[10];		// temp storage for scanned string
  int msg_data=0;		// temp storage for scanned int data
  int success=0;		// how many fields have we read from sscanf
  int advance=0; 		// advance this many chars after each sscanf

  // first value is drive target speed
  success=sscanf(driveBuf,"%s%d%n",msg_type,&msg_data,&advance);
  if (success ==2) {
    motData->driveTarget=msg_data;
    strncpy(motData->driveTarget_str,msg_type,10);
    driveBuf += advance + 1;
  }
  else return(-1);

  // second value is drive actual speed
  success=sscanf(driveBuf,"%s%d%n",msg_type,&msg_data,&advance);
  if (success ==2) {
    motData->driveActual=msg_data;
    motData->speedRPS = countsToRPS(motData->driveActual);
    strncpy(motData->driveActual_str,msg_type,10);

    driveBuf += advance + 1;
  }
  else return(-2);

  // third val is raw pwm out
  success=sscanf(driveBuf,"%s%d%n",msg_type,&msg_data,&advance);
  if (success ==2) {
    motData->drivePWM=msg_data;
    strncpy(motData->drivePWM_str,msg_type,10);
    driveBuf += advance + 1;
  }
  else return(-3);

  // fourth val is odometer
  success=sscanf(driveBuf,"%s%d%n",msg_type,&msg_data,&advance);
  if (success ==2) {
    motData->odometer=msg_data;
    strncpy(motData->odometer_str,msg_type,10);
    driveBuf += advance + 1;
  }
  else return(-4);

  // fifth val is raw current sense ADC value
  success=sscanf(driveBuf,"%s%d%n",msg_type,&msg_data,&advance);
  if (success ==2) {
    motData->rawCurrent=msg_data;
    strncpy(motData->rawCurrent_str,msg_type,10);
    driveBuf += advance + 1;
  }
  else return(-5);
  //dummy return to please compiler
  return 0;
}

// calculate speed in radians per second given encoder counts
//  we have a 500 count per revolution on the motor
// we measure the counts every 1/10 of a second
// there is a 9/23 gear ratio between motor and shell
//(shell does nine revolutions for every 23 motor revs
// there are 2 pi radians per revolution
// so the conversion factor given N counts in the last interval is:
// 2*pi*(rads/rev)*(1 rev/500 cnts) * N cnts/.1 sec * 9/23 = 0.0491* N rads/s

double countsToRPS(int counts) {

  return((double)counts * 0.0491);
}

// print out motor struct for debug & logging
void dumpMotorData(struct swarmMotorData *motData) {

  printf("driveTarget: %s%d\n",
	 motData->driveTarget_str,
	 motData->driveTarget);
  printf("driveActual: %s%d\n",
	 motData->driveActual_str,
	 motData->driveActual);
  printf("speed in Rad/S: %f\n",
	 motData->speedRPS);

  printf("drivePWM: %s%d\n",
	 motData->drivePWM_str,
	 motData->drivePWM);
  printf("odometer: %s%d\n",
	 motData->odometer_str,
	 motData->odometer);
}


int parseQueryMsg(char *queryBuf, struct swarmMotorData *motData, struct swarmImuData *imuData )
{
  char msg_type[10];		// temp storage for scanned string
  int msg_data=0;		// temp storage for scanned int data
  int success=0;		// how many fields have we read from sscanf
  int advance=0; 		// advance this many chars after each sscanf

  // first value is drive target speed
  success=sscanf(queryBuf,"%s%d%n",msg_type,&msg_data,&advance);
  if (success ==2) {
    motData->driveTarget=msg_data;
    strncpy(motData->driveTarget_str,msg_type,10);
    queryBuf += advance + 1;
  }
  else return(-1);

  // second value is drive actual speed
  success=sscanf(queryBuf,"%s%d%n",msg_type,&msg_data,&advance);
  if (success ==2) {
    motData->driveActual=msg_data;
    motData->speedRPS = countsToRPS(motData->driveActual);
    strncpy(motData->driveActual_str,msg_type,10);

    queryBuf += advance + 1;
  }
  else return(-2);

  // third val is raw pwm out
  success=sscanf(queryBuf,"%s%d%n",msg_type,&msg_data,&advance);
  if (success ==2) {
    motData->drivePWM=msg_data;
    strncpy(motData->drivePWM_str,msg_type,10);
    queryBuf += advance + 1;
  }
  else return(-3);

  // fourth val is odometer
  success=sscanf(queryBuf,"%s%d%n",msg_type,&msg_data,&advance);
  if (success ==2) {
    motData->odometer=msg_data;
    strncpy(motData->odometer_str,msg_type,10);
    queryBuf += advance + 1;
  }
  else return(-4);

  // fifth val is raw current sense ADC value
  success=sscanf(queryBuf,"%s%d%n",msg_type,&msg_data,&advance);
  if (success ==2) {
    motData->rawCurrent=msg_data;
    strncpy(motData->rawCurrent_str,msg_type,10);
    queryBuf += advance + 1;
  }
  else return(-5);

  // first value is steer target
  success=sscanf(queryBuf,"%s%d%n",msg_type,&msg_data,&advance);
  if (success ==2) {
    motData->steerTarget=msg_data;
    strncpy(motData->steerTarget_str,msg_type,10);
    queryBuf += advance + 1;
  }
  else return(-6);

  // second value is steer actual
  success=sscanf(queryBuf,"%s%d%n",msg_type,&msg_data,&advance);
  if (success ==2) {
    motData->steerActual=msg_data;
    strncpy(motData->steerActual_str,msg_type,10);
    queryBuf += advance + 1;
  }
  else return(-7);

  // third val is raw pwm out
  success=sscanf(queryBuf,"%s%d%n",msg_type,&msg_data,&advance);
  if (success ==2) {
    motData->steerPWM=msg_data;
    strncpy(motData->steerPWM_str,msg_type,10);
    queryBuf += advance + 1;
  }
  else return(-8);

  // FIRST Value is ADC0
  success=sscanf(queryBuf,"%s%d%n",msg_type,&msg_data,&advance);
  if (success ==2) {
    imuData->int_adc0=msg_data;
    strncpy(imuData->adc0_str,msg_type,10);
    queryBuf += advance + 1;
  }
  else return(-9);


  // Second Value is ADC1
  success=sscanf(queryBuf,"%s%d%n",msg_type,&msg_data,&advance);
  if (success ==2) {
    imuData->int_adc1=msg_data;
    strncpy(imuData->adc1_str,msg_type,10);
    queryBuf += advance + 1;
  }
  else return(-10);

  // Third Value is RateX
  success=sscanf(queryBuf,"%s%d%n",msg_type,&msg_data,&advance);
  if (success ==2) {
    imuData->int_ratex=msg_data;
    strncpy(imuData->ratex_str,msg_type,10);
    queryBuf += advance + 1;
  }
  else return(-11);

  // Fourth Value is ratez
  success=sscanf(queryBuf,"%s%d%n",msg_type,&msg_data,&advance);
  if (success ==2) {
    imuData->int_ratez=msg_data;
    strncpy(imuData->ratez_str,msg_type,10);
    queryBuf += advance + 1;
  }
  else return(-12);

  /* Fifth value is AccZ */
  success=sscanf(queryBuf,"%s%d%n",msg_type,&msg_data,&advance);
  if (success ==2) {
    imuData->int_accz=msg_data;
    strncpy(imuData->accz_str,msg_type,10);
    queryBuf += advance + 1;
  }
  else return(-13);

  // Sixth value is AccX
  success=sscanf(queryBuf,"%s%d%n",msg_type,&msg_data,&advance);
  if (success ==2) {
    imuData->int_accx=msg_data;
    strncpy(imuData->accx_str,msg_type,10);
    queryBuf += advance + 1;
  }
  else return(-14);

  // Seventh value is vref (SPARE)
  success=sscanf(queryBuf,"%s%d%n",msg_type,&msg_data,&advance);
  if (success ==2) {
    imuData->int_vref=msg_data;
    strncpy(imuData->vref_str,msg_type,10);
    queryBuf += advance + 1;
  }
  else return(-15);

  // last (eighth) value is AccY
  success=sscanf(queryBuf,"%s%d",msg_type,&msg_data);
  if (success ==2) {
    imuData->int_accy=msg_data;
    strncpy(imuData->accy_str,msg_type,10);
  }
  else return(-16);

  calculateImuBias(imuData);

  imuIntToSI(imuData);
  //dummy return to please compiler
  return 0;
}

void initYawSensor(void)
{

	int rawYaw = 0;
	unsigned char c;
	char debugMsg[1024];
	int myIdx;

	int spiFd = open("/dev/mem", O_RDWR|O_SYNC);
	dioptr = (unsigned int *)mmap(0, getpagesize(),
			PROT_READ|PROT_WRITE, MAP_SHARED, spiFd, DIOBASE);
	//RW_REG(dioptr) &= ~(1 << CS_PIN); // make sure yaw sensor not chip selected

	RW_REG(dioptr) ^= (1 << CS_PIN); // yaw sensor chip select-set high
	init_spi(); // call init_spi before selecting an SPI device

	RW_REG(dioptr) &= ~(1 << CS_PIN); // yaw sensor chip select - set low

	c=spi8(0x97);
	sprintf(debugMsg, "\n Yaw: init 1 write=%02X", c);
	logit(eMcuLog, eLogDebug, debugMsg);
	c=spi8(0x08);
	sprintf(debugMsg, "\n Yaw: init 2 write=%02X", c);
	logit(eMcuLog, eLogDebug, debugMsg);

	RW_REG(dioptr) ^= (1 << CS_PIN); // yaw sensor chip select - set high

	for (myIdx=0; myIdx < 1000; myIdx++);

	RW_REG(dioptr) &= ~(1 << CS_PIN); // yaw sensor chip select - set low

	c = spi8(0x96);
	sprintf(debugMsg, "\n Yaw: init 3 read=%02X", c);
	logit(eMcuLog, eLogDebug, debugMsg);
	rawYaw = (c & 0x3f) << 10;

	c = spi8(0x01);
	sprintf(debugMsg, "\n Yaw: init 4 read=%02X", c);
	logit(eMcuLog, eLogDebug, debugMsg);
	rawYaw |= c << 2;
	RW_REG(dioptr) ^= (1 << CS_PIN); // yaw sensor chip select - set high


}

int getYawRate(void)
{
	int rawYaw = 0;
	unsigned char c;
	char debugMsg[1024];
	int myIdx;

	RW_REG(dioptr) &= ~(1 << CS_PIN); // yaw sensor chip select - set low

	c=spi8(0x00);
	sprintf(debugMsg, "\n Yaw: first byte from SPI write=%02X", c);
	logit(eMcuLog, eLogDebug, debugMsg);
	c=spi8(0x00);
	sprintf(debugMsg, "\n Yaw: second byte from SPI write=%02X", c);
	logit(eMcuLog, eLogDebug, debugMsg);

	RW_REG(dioptr) ^= (1 << CS_PIN); // yaw sensor chip select - set high

	for (myIdx=0; myIdx < 1000; myIdx++);

	RW_REG(dioptr) &= ~(1 << CS_PIN); // yaw sensor chip select - set low

	//c = spi8(0x17); // GYRO_SCALE
	//c = spi8(0x3d);   // STATUS
	//c = spi8(0x35);   // MSC_CRTL
	//c = spi8(0x05);   // GYRO_DATA
	c = spi8(0x39);    // SENS/AVG
	sprintf(debugMsg, "\n Yaw: first byte from SPI read=%02X", c);
	logit(eMcuLog, eLogDebug, debugMsg);
	rawYaw = (c & 0x3f) << 10;

	c = spi8(0x39);
	sprintf(debugMsg, "\n Yaw: second byte from SPI read=%02X", c);
	logit(eMcuLog, eLogDebug, debugMsg);
	rawYaw |= c << 2;
	RW_REG(dioptr) ^= (1 << CS_PIN); // yaw sensor chip select - set high
	return(rawYaw);
}
