/* IMU.c */

// Version 16.0
// Date: June-2007
// JTF from original from Petey the Programmer

#include <avr/io.h>
#include "UART.h"
#include "a2d.h"
#include "putstr.h"
#include "encoder.h"
#include "steering.h"
#include "motor.h"
#include "IMU.h"   

void calc_check_sum( char *checkSum, char *str);
void check_sum_to_HexStr( char checkSum, char *str);
void num_to_Str( short v, char *str);

// ------------------------------------------------------------------------
// This routine is called 10 times per second from the main loop.
// Send out the data from the 5DOF IMU, plus the steering and motor data.
// Format the data just like that comming from the GPS units.
//
// Output String: "$IMU,0,512,985,23,635,1023,435*C5"
//
// Sequence is: Speed, xGyro, yGyro, xAccel, yAccel, zAccel, vRef * checksum
// Checksum is XOR of everything between $ and * (not including $ or *)
//
// 5DOF unit puts out 5 signals, 1 for each measurement, plus a reference voltage.
// Unit uses 3.3v max output.  A2D is setup using 3.3v as its AD_vRef for full scale input.
// Each measurement is a 10 bit number [0..1023] centered at 512
// vRef is 1.23v reference signal.
//
// Steering Axle tilt and Ballast box swing angle can be calculated using the
// raw accelerometer data via the arctan function:
// Steering_Axle_tilt = atan2(yAccel-512, zAccel-512)
// Ballast_Swing_Angle = atan2(xAccel-512, zAccel-512)11
//

// associate signal names with adc channels
static char *sigName[] = {"ADC0",  /* ADC0, pin 23 */
			  "ADC1",  /* ADC1, pin 24 */
			  "RATEX", /* ADC2, pin 25, header 13  gyro X*/
			  "RATEY", /* ADC3, pin 26, header 11  gyro Y*/
			  "ACCZ",  /* ADC4, pin 27, header 9   accel Z */
			  "ACCX",  /* ADC5, pin 28, header 7   accel X */
			  "NC",    /* ADC6, pin 19, header 5   */
			  "ACCY"}; /* ADC7, pin 22, header 3,  accel y*/


void IMU_output_data_string(void)
{
  char checkSum = 0;
  char theStr[] = "$IMU\0\0\0\0";		// must be at least 7 chrs long: ",-1023\0"
  uint8_t n;
  short v;
  
  calc_check_sum( &checkSum, theStr );
  putstr(theStr);
  
  v = Encoder_read_speed(MOTOR1_SHAFT_ENCODER);
  num_to_Str(v,theStr);
  calc_check_sum( &checkSum, theStr );
  putstr(theStr);
  
  for (n = IMU_FIRST_CHANNEL; n <= IMU_LAST_CHANNEL; n++){
    v = A2D_read_channel(n);
    num_to_Str(v,theStr);
    calc_check_sum( &checkSum, theStr );
    putstr(theStr);
  }
  
  check_sum_to_HexStr( checkSum, theStr );	
  putstr("*");
  putstr(theStr);
  putstr("\r\n");
}


// New, easier-to-parse version by JTF. No checksum, etc. 
void IMU_output_data(void)
{
  unsigned char n;
  short v;
  
  putstr("ADC\n");
  
  for (n = 0; n <= 7; n++){
    v = A2D_read_channel(n);
    putstr(sigName[n]);
    putstr(": ");
    putS16(v);
    putstr("\n");
  }
}
// -----------------------------------------------------------------------------------

void calc_check_sum( char *checkSum, char *str)
{
	char ch;
	
	while((ch=*str)!= '\0')
		{
		*checkSum = *checkSum ^ ch;
		str++;
	}
}

// --------------------------------------------------------------------------------------------------------------------------------------------------------------------------

void check_sum_to_HexStr( char checkSum, char *str)
{
	char HexStr[] = "0123456789ABCDEF";
	uint8_t v;
	
	v = (checkSum >> 4) & 0x0F;
	str[0] = HexStr[v];
	
	v = checkSum & 0x0F;
	str[1] = HexStr[v];
	
	str[2] = 0;
}

// --------------------------------------------------------------------------------------------------------------------------------------------------------------------------
// special version - output 10 bit number with leading comma

void num_to_Str( short v, char *str)
{
	uint8_t n = 0;
	short mx, maxVal = 0;
	
	str[n++] = ',';	
	if (v < 0) 
		{
		str[n++] = '-';
		v = -v;
		}
	if (v > 1-24) v = 1024;
	
	if (v > 9) maxVal = 10;
	if (v > 99) maxVal = 100;
	if (v > 999) maxVal = 1000;
	
	if (maxVal)
	for (mx = maxVal; mx > 1; mx = mx / 10)
		{
		str[n] = 0;
		while((v - mx)>=0) {
			v -= mx;
			str[n]++;
			}
		str[n++] += '0';
		}

	str[n++] = v + '0';
	str[n] = 0;
}

// --------------------------------------------------------------------------------------------------------------------------------------------------------------------------
// End of File
