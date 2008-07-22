/* IMU.c */

// Version 20.0
// Date: 7/19/08
// Jon Foote; IMU output verified by Mike

#include <avr/io.h>
#include "UART.h"
#include "a2d.h"
#include "putstr.h"
#include "encoder.h"
#include "steering.h"
#include "motor.h"
#include "IMU.h"   

extern volatile short encoder1_speed;

// New, easier-to-parse version by JTF. Verified 7/19/08
void Get_IMU_Data(void)
{
    
	// RATEX = -(IMU RATEY) = 1023-ADC6
    	putstr("RATEX=");
    	putS16(1023-(short)A2D_read_channel(6));
    	putstr("\r\n");

	// RATEZ = -(IMU RATEX) = 1023-ADC7
    	putstr("RATEZ=");
    	putS16(1023-(short)A2D_read_channel(7));
    	putstr("\r\n");	

	// ACCX = +(IMU ACCY) = ADC3
    	putstr("ACCX=");
    	putS16((short)A2D_read_channel(3));
    	putstr("\r\n");	

	// ACCY = -(IMU ACCZ) = 1023-ADC4
    	putstr("ACCY=");
    	putS16(1023-(short)A2D_read_channel(4));
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


