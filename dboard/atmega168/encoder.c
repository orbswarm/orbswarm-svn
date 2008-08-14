// ---------------------------------------------------------------------
// 
//	File: encoder.c
//      SWARM Orb http://www.orbswarm.com
//	Sense speed from Geartooth encoders for SWARM Orb Motor Control Unit
//
//	Refactored by Jonathan (Head Rotor at rotorbrain.com)
//      Original Version by Petey the Programmer  7-May-2007
// -----------------------------------------------------------------------


// how this works: encoder interrupts 500 times per rev, triggering
// int0. This increments encoder_count. At a fixed period, sample encoder_count // to see speed, which will be proportional.


#include <avr/interrupt.h>
#include <avr/io.h>
#include "encoder.h"   
#include "timer.h"   
#include "UART.h"
#include "putstr.h"
/* Variables for encoder interrupt routines */

volatile unsigned short encoder1_count;
volatile short encoder1_speed;
volatile unsigned short encoder1_dir;
volatile uint32_t odometer;

//#define MAX_SAMPLES	10
//static unsigned char prevSample[MAX_SAMPLES];



// ----------------------------------------------------------------------
// Interrupt routines to handle Shaft Encoder Inputs


SIGNAL(INT0_vect)
{
  encoder1_dir =  PIND & _BV(PIND3);
  encoder1_count++;
  odometer++;
}

//SIGNAL(SIG_INTERRUPT1)
//{
//  encoder2_count++;
//}

// ------------------------------------------------------------------------
// Setup inputs for Interrupt on Change
// Pins IN0 & IN1 - PortD 2:3

void Encoder_Init(void)
{
  
  encoder1_count = 0;
  encoder1_speed = 0;
  encoder1_dir = 0;
  odometer = 0;

  //MCUCR |= ((1<<ISC00) | (1<<ISC01)); // Rising edge generates interrupt
  //GICR &= ~(1<<INT1); // disable interrupt on pin PD3(INT1)
  //GICR = 0;
  //GICR |= (1<<INT0); // Enable interrupt on pin PD2(INT0)
  
  EICRA = ((1<<ISC00) | (1<<ISC01)); /* Rising edge generates interrupt */

  EIMSK = 0;
  EIMSK |= (1<<INT0); // Enable interrupt on pin PD2(INT0)

}

// ------------------------------------------------------------------------

// call this at a fixed timer frequency to get encoder speed
//void Encoder_Sample(void)
//{
//  GICR &= ~(1<<INT0);		// Disable Enternal Interrupts on Pins IN0
//  encoder1_speed = encoder1_count;
//  if(encoder1_dir)
//    encoder1_speed = -encoder1_speed;
//  encoder1_count = 0;

//  GICR |= (1<<INT0);		// Enable Enternal Interrupts on Pins IN0
//  putstr("ES\n");
//  //putS16(encoder1_speed);
//  //putstr("\r\n");
//}



#define SPROCKET_TEETH 23 // this number of teef on the sprocket
// -------------------------------------------------------------------------
// convert encoder count (measured every 100 hz) to 10x RPM

//unsigned short EncoderReadSpeed(){ 

//  uint32_t RPM = 0;
//  uint32_t count = 0;

//  GICR &= ~(1<<INT0);   // Disable Enternal Interrupts on Pins IN0
//  count = (uint32_t)encoder1_count;
//  GICR |= (1<<INT0);     // Enable Enternal Interrupts on Pins IN0

//  return(count);
//}

/* unsigned short Encoder_read_speed(unsigned char channelNum) */
/* { */
/* 	unsigned char n; */
/* 	unsigned short theCount = 0; */
/* 	if (channelNum == MOTOR1_SHAFT_ENCODER) { */
/* 		for (n=0;n<(MAX_SAMPLES-1);n++) */
/* 			theCount += prevSample[n]; */
/* 		return theCount; */
/* 		} */
/* 	else */
/* 		return encoder2_speed; */
/* } */


