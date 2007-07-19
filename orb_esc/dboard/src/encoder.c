// ---------------------------------------------------------------------
// 
//	File: encoder.c
//      SWARM Orb http://www.orbswarm.com
//	Sense speed from Geartooth encoders for SWARM Orb Motor Control Unit
//
//	Refactored by Jonathan (Head Rotor at rotorbrain.com)
//      Original Version by Petey the Programmer  7-May-2007
// -----------------------------------------------------------------------


// how this works: Timer0 interrupts at 100hz rate, incrementing var
// Timer2_ticks. Geartooth sensor interrupts, triggering SIG_INTERRUPT0
// routine. This saves the current value of Timer2_ticks in encoder1_count, 
// and resets Timer2_ticks. Thus encoder1_count has the length of the last
// geartooth pulse in 10 ms increments.


#include <avr/interrupt.h>
#include <avr/io.h>
#include "encoder.h"   
#include "timer.h"   

/* Variables for encoder interrupt routines */

volatile unsigned short encoder1_count;
volatile unsigned short encoder2_count;

static short encoder2_speed;

#define MAX_SAMPLES	10
static unsigned char prevSample[MAX_SAMPLES];

extern volatile unsigned short Timer2_ticks;


void Encoder_reset(unsigned char channelNum);

// ----------------------------------------------------------------------
// Interrupt routines to handle Shaft Encoder Inputs


SIGNAL(SIG_INTERRUPT0)
{
//  encoder1_count++;
  encoder1_count = Timer2_ticks;
  Timer2_reset();
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
  unsigned char n;
  
  for (n=0;n<MAX_SAMPLES;n++)
    prevSample[n] = 0;
  
  encoder1_count = 0;
  encoder2_count = 0;
  
  //MCUCR |= ((1<<ISC10) | (1<<ISC00)); // Any logical change interrupts
  // GICR |= ((1<<INT0) | (1<<INT1)); // Enable  Interrupts on Pins IN0 & IN1

  MCUCR |= ((1<<ISC00) | (1<<ISC01)); // Rising edge generates interrupt
  GICR |= (1<<INT0); // Enable interrupt on pin PD2(INT0)

}

// ------------------------------------------------------------------------

void Encoder_reset(unsigned char channelNum)
{
	if (channelNum == MOTOR1_SHAFT_ENCODER)
		{
		GICR &= ~(1<<INT0);		// Disable Enternal Interrupts on Pins IN0
		encoder1_count = 0;
		GICR |= (1<<INT0);		// Enable Enternal Interrupts on Pins IN0
		}
	else
		{
		GICR &= ~(1<<INT1);
		encoder2_count = 0;
		GICR |= (1<<INT1);
		}
}


// -------------------------------------------------------------------------
// obsolete procedure to average counts: may need to resurrect

void Encoder_sample_speed(unsigned char channelNum)
{
	unsigned char n;
	unsigned short theCount = 0;
	
	//theCount = Encoder_read_count(channelNum);
	Encoder_reset(channelNum);

	if (channelNum == MOTOR1_SHAFT_ENCODER) {
		for (n=0;n<(MAX_SAMPLES-1);n++)
			prevSample[n] = prevSample[n+1];
		prevSample[MAX_SAMPLES-1] = theCount;
		}
	else
		encoder2_speed = theCount;
}

#define SPROCKET_TEETH 23 // this number of teef on the sprocket
// -------------------------------------------------------------------------
// convert encoder pulse width (measured in 100hz ticks) to RPM

unsigned short EncoderReadSpeed(){ 

  uint32_t RPM = 0;
  uint32_t count = 0;

  GICR &= ~(1<<INT0);   // Disable Enternal Interrupts on Pins IN0
  count = (uint32_t)encoder1_count;
  GICR |= (1<<INT0);     // Enable Enternal Interrupts on Pins IN0


  // if we get X 100hz counts in a pulse, that means
  // we get 100/X counts per second. To get revolutions per
  // second, multiply by SPROCKET_TEETH (counts per revolution)
  // to get RPM, multiply by 60 seconds/minute
  // 60 * 23 * 100 = 138000 to save multiplications
  // add another factor of 10 for more resolution, so rounds per 10 min

  if (count == 0) {
    return 600;		/* max speed to avoid divide-by-zero */
  }
  if (count > 5000)  // essentially stopped
    return 0;
  else {
    RPM = ((uint32_t)138000)/count;
    return (unsigned short)RPM;
  }

}

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


