/* IMU.c */

// Version 14.1
// Date: 1-May-2007
// Petey the Programmer

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

// --------------------------------------------------------------------------------------------------------------------------------------------------------------------------
// This routine is called 10 times per second from the main loop.
// Send out the data from the 5DOF IMU, plus the steering and motor data.
// Format the data just like that comming from the GPS units.
//
// Output String: "$IMU,0,500,512,985,23,635,1023,435*C5"
//
// Sequence is: Speed, Steer, xGyro, yGyro, xAccel, yAccel, zAccel, vRef * checksum
// Checksum is XOR of everything between $ and * (not including $ or *)
//
// 5DOF unit puts out 5 signals, 1 for each measurement, plus a reference voltage.
// Unit uses 3.3v max output.  A2D is setup using 3.3v as its AD_vRef for full scale input.
// Each measurement is a 10 bit number [0..1023] centered at 512
// vRef is 1.23v reference signal.


void IMU_output_data_string(void)
{
	char checkSum = 0;
	char theStr[] = "$IMU\0\0";		// must be at least 6 chrs long
	uint8_t n;
	short v;
	
	calc_check_sum( &checkSum, theStr );
	putstr(theStr);
	
	v = Encoder_read_speed(MOTOR1_SHAFT_ENCODER);
	num_to_Str(v,theStr);
	calc_check_sum( &checkSum, theStr );
	putstr(theStr);
	
	v = Steering_Read_Position();
	num_to_Str(v,theStr);
	calc_check_sum( &checkSum, theStr );
	putstr(theStr);
	
	for (n = IMU_FIRST_CHANNEL; n <= IMU_LAST_CHANNEL; n++)
		{
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

// --------------------------------------------------------------------------------------------------------------------------------------------------------------------------

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
	uint8_t n = 1;
	short mx, maxVal = 0;
	
	str[0] = ',';
	
	if (v > 10) maxVal = 10;
	if (v > 100) maxVal = 100;
	if (v > 1000) maxVal = 1000;
	
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
