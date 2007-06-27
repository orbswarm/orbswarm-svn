// ---------------------------------------------------------------------------
//
//	File: encoder.c
//
//	Wheel/Shaft Encoders for Orb Motor Control Unit
//
//	Version 16.0
//	Changed encoder 2 interrupt to killswitch interrupt
// ---------------------------------------------------------------------------

#include <avr/interrupt.h>
#include <avr/io.h>
#include "encoder.h"   
#include "putstr.h"

/* Variables for encoder interrupt routines */

volatile unsigned short encoder1_count=0;
volatile unsigned char  killsw_active=0;

#define MAX_SAMPLES	10
static unsigned char prevSample[MAX_SAMPLES];

void Encoder_reset(unsigned char channelNum);

// ---------------------------------------------------------------------------
// Interrupt routines to handle Shaft Encoder Inputs

ISR(SIG_INTERRUPT0)
{
  encoder1_count++;
}

ISR(SIG_INTERRUPT1)
{
  char saveSREG;
  //save status reg
  //GICR &= ~(1<<INT1);		// disable further interrupts until clear
  //putstr("MCU: Killswitch!\n"); 
  killsw_active = 1;

  //SREG = saveSREG;
}

// --------------------------------------------------------------------------
// Setup inputs for Interrupt on Change
// Pins IN0 & IN1 - PortD 2:3


// INO is geartooth sensor
// IN1 is killswitch

void Encoder_Init(void)
{
  unsigned char n;
  
  for (n=0;n<MAX_SAMPLES;n++)
	prevSample[n] = 0;
	
  encoder1_count = 0;
  MCUCR |=  (1<<ISC00);    // Any logical change on INO generates interrupt
  GICR |=  (1<<INT0); // Enable Enternal Interrupts on Pins IN0 
  

}
void Killswitch_Init(void) {
  MCUCR |= (1<<ISC11);	   // falling edge of int1 generates INI int
  GICR |=  (1<<INT1);	   // Enable Enternal Interrupt on  IN1
  killsw_active = 0;
}

int Killswitch_Query(void) {
  return(killsw_active);
}

void Killswitch_Reset(void)
{
  GICR |=  (1<<INT1);	   // Enable Enternal Interrupt on  IN1
  killsw_active = 0;
}

// --------------------------------------------------------------------------

void Encoder_reset(unsigned char channelNum)
{
	if (channelNum == MOTOR1_SHAFT_ENCODER)
		{
		GICR &= ~(1<<INT0);		// Disable Enternal Interrupts on Pins IN0
		encoder1_count = 0;
		GICR |= (1<<INT0);		// Enable Enternal Interrupts on Pins IN0
		}
}





unsigned short Encoder_read_count(unsigned char channelNum)
{
  unsigned short encoder_count=0;
  
  if (channelNum == MOTOR1_SHAFT_ENCODER){
    GICR &= ~(1<<INT0);		// Disable Enternal Interrupts on Pins IN0
    encoder_count = encoder1_count;
    GICR |= (1<<INT0);		// Enable Enternal Interrupts on Pins IN0
  }
  return encoder_count;
}

// ----------------------------------------------------------------------------
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
  
  if (channelNum == MOTOR1_SHAFT_ENCODER) 
    {
      for (n=0;n<(MAX_SAMPLES-1);n++)
	prevSample[n] = prevSample[n+1];
      prevSample[MAX_SAMPLES-1] = theCount;
    }
}

// ----------------------------------------------------------------------------
// return the sum of the prev 5 or 10 samples.
// this increases our resolution, but introduces a time lag in the feedback.

unsigned short Encoder_read_speed(unsigned char channelNum)
{
  unsigned char n;
  unsigned short theCount = 0;
  
  if (channelNum == MOTOR1_SHAFT_ENCODER) 
    {
      for (n=0;n<(MAX_SAMPLES-1);n++)
	theCount += prevSample[n];
      
      return theCount;
    }
  else
    return 0;
}

