<<<<<<< .mine
/* IMU.c */

// Version 14.4
// Date: 16-May-2007
// Petey the Programmer

#include <avr/io.h>
#include "UART.h"
#include "a2d.h"
#include "putstr.h"
#include "encoder.h"
#include "steering.h"
#include "motor.h"
#include "IMU.h"   

extern volatile short encoder1_speed;

void calc_check_sum( char *checkSum, char *str);
void check_sum_to_HexStr( char checkSum, char *str);
void num_to_Str( short v, char *str);


// associate signal names with adc channels
//static char *sigName[] = {"ADC0",  /* ADC0, PC0, pin 23,  */
//			  "ADC1",  /* ADC1, PC1, pin 24,  */
//			  "RATEX", /* ADC2, PC2, pin 25, IMU header 13  gyro X*/
//			  "RATEY", /* ADC3, PC3, pin 26, IMU header 11  gyro Y*/
//			  "ACCZ",  /* ADC4, PC4, pin 27, IMU header 9   accel Z */
//			  "ACCX",  /* ADC5, PC5, pin 28, IMU header 7   accel X */
//			  "SPARE", /* ADC6, TQFP pin 19, IMU header 5   unused */
//			  "ACCY"}; /* ADC7, TQFP pin 22, IMU header 3,  accel Y*/


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
// Ballast_Swing_Angle = atan2(xAccel-512, zAccel-512)
//

// New, easier-to-parse version by JTF. No checksum, etc. 
void Get_IMU_Data(void)
{
//  unsigned char n;
//  short v;
  
  //putstr("ADC\n");
  
/*  for (n = 0; n <= 7; n++){
    v = A2D_read_channel(n);
    putstr(sigName[n]);
    putstr("=");
    putS16(v);
    putstr("\r\n");
  }*/
    
	// RATEX = -(IMU RATEY) = 1023-ADC6
    	putstr("RATEX=");
    	putS16(1023-(short)A2D_read_channel(6));
    	putstr("\r\n");

	// RATEZ = -(IMU RATEX) = 1023-ADC7
    	putstr("RATEZ=");
    	putS16(1023-(short)A2D_read_channel(7));
    	putstr("\r\n");	

	// ACCX = -(IMU ACCY) = 1023-ADC3
    	putstr("ACCX=");
    	putS16(1023-(short)A2D_read_channel(3));
    	putstr("\r\n");	

	// ACCY = +(IMU ACCZ) = ADC4
    	putstr("ACCY=");
    	putS16((short)A2D_read_channel(4));
    	putstr("\r\n");	

	// ACCZ = +(IMU ACCX) = ADC2
    	putstr("ACCZ=");
    	putS16((short)A2D_read_channel(2));
    	putstr("\r\n");
	
	// ENCODER SPEED
    	putstr("ENC_SPEED=");
	putS16(encoder1_speed);
    	putstr("\r\n");

}

void IMU_output_data_string(void)
{
	char checkSum = 0;
	char theStr[] = "$IMU\0\0\0\0";		// must be at least 7 chrs long: ",-1023\0"
	uint8_t n;
	short v;
	
	calc_check_sum( &checkSum, theStr );
	putstr(theStr);
	
	// v = Encoder_read_speed(MOTOR1_SHAFT_ENCODER);
	// num_to_Str(v,theStr);
	// calc_check_sum( &checkSum, theStr );
	// putstr(theStr);
	
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



// --------------------------------------------------------------------------

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

=======
/* IMU.c */

// Version 14.4
// Date: 16-May-2007
// Petey the Programmer

#include <avr/io.h>
#include "UART.h"
#include "a2d.h"
#include "putstr.h"
#include "encoder.h"
#include "steering.h"
#include "motor.h"
#include "IMU.h"   

extern volatile short encoder1_speed;

void calc_check_sum( char *checkSum, char *str);
void check_sum_to_HexStr( char checkSum, char *str);
void num_to_Str( short v, char *str);


// associate signal names with adc channels
//static char *sigName[] = {"ADC0",  /* ADC0, PC0, pin 23,  */
//			  "ADC1",  /* ADC1, PC1, pin 24,  */
//			  "RATEX", /* ADC2, PC2, pin 25, IMU header 13  gyro X*/
//			  "RATEY", /* ADC3, PC3, pin 26, IMU header 11  gyro Y*/
//			  "ACCZ",  /* ADC4, PC4, pin 27, IMU header 9   accel Z */
//			  "ACCX",  /* ADC5, PC5, pin 28, IMU header 7   accel X */
//			  "SPARE", /* ADC6, TQFP pin 19, IMU header 5   unused */
//			  "ACCY"}; /* ADC7, TQFP pin 22, IMU header 3,  accel Y*/


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
// Ballast_Swing_Angle = atan2(xAccel-512, zAccel-512)
//

// New, easier-to-parse version by JTF. No checksum, etc. 
void Get_IMU_Data(void)
{
//  unsigned char n;
//  short v;
  
  //putstr("ADC\n");
  
/*  for (n = 0; n <= 7; n++){
    v = A2D_read_channel(n);
    putstr(sigName[n]);
    putstr("=");
    putS16(v);
    putstr("\r\n");
  }*/
    
	// RATEX = -(IMU RATEY) = 1023-ADC6
    	putstr("RATEX=");
    	putS16(1023-(short)A2D_read_channel(6));
    	putstr("\r\n");

	// RATEZ = -(IMU RATEX) = 1023-ADC7
    	putstr("RATEZ=");
    	putS16(1023-(short)A2D_read_channel(7));
    	putstr("\r\n");	

	// ACCX = (IMU RATEY) = ADC3
    	putstr("ACCX=");
    	putS16((short)A2D_read_channel(3));
    	putstr("\r\n");	

	// ACCY = (IMU RATEZ) = ADC4
    	putstr("ACCY=");
    	putS16((short)A2D_read_channel(4));
    	putstr("\r\n");	

	// ACCZ = +(IMU RATEX) = ADC2
    	putstr("ACCZ=");
    	putS16((short)A2D_read_channel(2));
    	putstr("\r\n");
	
	// ENCODER SPEED
    	putstr("ENC_SPEED=");
	putS16(encoder1_speed);
    	putstr("\r\n");

}

void IMU_output_data_string(void)
{
	char checkSum = 0;
	char theStr[] = "$IMU\0\0\0\0";		// must be at least 7 chrs long: ",-1023\0"
	uint8_t n;
	short v;
	
	calc_check_sum( &checkSum, theStr );
	putstr(theStr);
	
	// v = Encoder_read_speed(MOTOR1_SHAFT_ENCODER);
	// num_to_Str(v,theStr);
	// calc_check_sum( &checkSum, theStr );
	// putstr(theStr);
	
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



// --------------------------------------------------------------------------

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

>>>>>>> .r779
