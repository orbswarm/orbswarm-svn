// ------------------------------------------------------------------------------------------------------------------------------------------------------------
//
//	File: encoder.c
//
//	Wheel & Shaft Encoders for Orb Motor & Steering Control
//
//	Version 14.0
//	30-Apr-2007
// ------------------------------------------------------------------------------------------------------------------------------------------------------------

#include <avr/signal.h>  
#include <avr/io.h>
#include "encoder.h"   

/* global variables for encoder interupt routines */

volatile unsigned short encoder1_count;
volatile unsigned short encoder2_count;

static short encoder2_speed;

#define MAX_SAMPLES	10
static unsigned char prevSample[MAX_SAMPLES];

// ------------------------------------------------------------------------------------------------------------------------------------------------------------
// Interupt routines to handle Shaft Encoder Inputs

SIGNAL(SIG_INTERRUPT0)
{
  encoder1_count++;
}

SIGNAL(SIG_INTERRUPT1)
{
  encoder2_count++;
}

// ------------------------------------------------------------------------------------------------------------------------------------------------------------
// Setup inputs for Interupt on Change
// Pins IN0 & IN1 - PortD 2:3

void Encoder_Init(void)
{
  unsigned char n;
  
  for (n=0;n<MAX_SAMPLES;n++)
	prevSample[n] = 0;
	
  encoder1_count = 0;
  encoder2_count = 0;
  
  MCUCR |= ((1<<ISC10) | (1<<ISC00));	// Any logical change generates interupt
  GICR |= ((1<<INT0) | (1<<INT1));		// Enable Enternal Interupts on Pins IN0 & IN1
}

// ------------------------------------------------------------------------------------------------------------------------------------------------------------

void Encoder_reset(unsigned char channelNum)
{
	if (channelNum == MOTOR1_SHAFT_ENCODER)
		{
		GICR &= ~(1<<INT0);		// Disable Enternal Interupts on Pins IN0
		encoder1_count = 0;
		GICR |= (1<<INT0);		// Enable Enternal Interupts on Pins IN0
		}
	else
		{
		GICR &= ~(1<<INT1);
		encoder2_count = 0;
		GICR |= (1<<INT1);
		}
}

// ------------------------------------------------------------------------------------------------------------------------------------------------------------

unsigned short Encoder_read_count(unsigned char channelNum)
{
	unsigned short encoder_count;

	if (channelNum == MOTOR1_SHAFT_ENCODER)
		{
		GICR &= ~(1<<INT0);		// Disable Enternal Interupts on Pins IN0
		encoder_count = encoder1_count;
		GICR |= (1<<INT0);		// Enable Enternal Interupts on Pins IN0
		}
	else
		{
		GICR &= ~(1<<INT1);
		encoder_count = encoder2_count;
		GICR |= (1<<INT1);
		}
	
	return encoder_count;
}

// ------------------------------------------------------------------------------------------------------------------------------------------------------------
// sample the encoders 10 times a second.
// during testing, I'm using a wheel encoder that's only spinning at 60 rpm
// this only gives us a few clicks per 100ms sample -- too few.
// because I'm using a very slow motor, I'm agregating the samples over 1 second.

void Encoder_sample_speed(unsigned char channelNum)
{
	unsigned char n;
	unsigned short theCount;
	
	theCount = Encoder_read_count(channelNum);
	Encoder_reset(channelNum);

	if (channelNum == MOTOR1_SHAFT_ENCODER) {
		for (n=0;n<(MAX_SAMPLES-1);n++)
			prevSample[n] = prevSample[n+1];
		prevSample[MAX_SAMPLES-1] = theCount;
		}
	else
		encoder2_speed = theCount;

}

// ------------------------------------------------------------------------------------------------------------------------------------------------------------
// return the sum of the prev 5 or 10 samples.
// this increases our resolution, but introduces a time lag in the feedback.

unsigned short Encoder_read_speed(unsigned char channelNum)
{
	unsigned char n;
	unsigned short theCount = 0;
	
	if (channelNum == MOTOR1_SHAFT_ENCODER) {
		for (n=0;n<(MAX_SAMPLES-1);n++)
			theCount += prevSample[n];

		return theCount;
		}
	else
		return encoder2_speed;
	
}

// ------------------------------------------------------------------------------------------------------------------------------------------------------------
// End of File
