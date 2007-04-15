
// a2d.c - A2D functions

#include <avr/io.h>
#include "a2d.h"
 
// Static variables holding the last value from the ADC

static unsigned short REF1_value;
static unsigned short REF2_value;

// ----------------------------------------------------------------------------------------------------------------------------------------------------------------
// Initialize Analogue to Digital converters.
// Use single ended conversion - one channel at a time, 
// sample channels only when triggered. (not free running)

void A2D_Init(void)
{
	// setup which pins to be used for AD - PortC0:1
	
//    ADCSRA = (1<<ADPS0) | (1<<ADPS1) | (1<<ADPS2); // Prescale 128

    ADCSRA = (1<<ADPS0) | (1<<ADPS1);		// Prescale 8
    ADMUX = (1<<REFS0);						// AVCC voltage ref, ch 0.

//	ADCSRA |= (1<<ADIE);	// Enable ADC conversion complete interrupt
    ADCSRA |= (1<<ADEN);	// Enable the ADC
      
// Start conversions
	ADCSRA |= (1<<ADSC);
}

// ----------------------------------------------------------------------------------------------------------------------------------------------------------------
// poll a2d conversion complete bit - read ADC, start next conversion.
// This avoids waiting or having to turn off interupts to read/write values.

void A2D_poll_adc(void)
{
	if (bit_is_clear(ADCSRA, ADSC))	// if clear, conversion is done.
		{
		if ((ADMUX & 0x03) == 0)	// Check which channel was just measured
			{
			REF1_value = ADC;		// Save measured value
			ADMUX |= 1;				// Set next channel to read to 1
			}
		else // Channel 1 is done
			{
			REF2_value = ADC;		// Save measured value
			ADMUX &= ~0x03;			// Set next channel to read to 0
			}
		
		ADCSRA |= (1<<ADSC);		// Start next conversion
		}
}

// ----------------------------------------------------------------------------------------------------------------------------------------------------------------
// Setup and Read an Analogue to Digital Converter channel.
// Returns 10-bit value 0..1023
// Wait for conversion

unsigned short xA2D_read_channel(unsigned char chanNum);
unsigned short xA2D_read_channel(unsigned char chanNum)
{
	unsigned short theData;
	
	ADMUX = (1<<REFS0) | (chanNum & 0x03);		// AVCC voltage ref, set channel num 0..3
	
	ADCSRA |= (1<<ADSC);	// Start AD conversion
	
	loop_until_bit_is_clear(ADCSRA, ADSC);
	
	theData = ADC;	// this is 10-bit data.  Should read low byte, then high
	
	return theData;		
}

// ----------------------------------------------------------------------------------------------------------------------------------------------------------------
// Return most recent value read from A2D converter for requested channel.
// This avoids waiting or having to turn off interupts to read/write values.

unsigned short A2D_read_channel(unsigned char chanNum)
{
	if (chanNum == 0)
		return REF1_value;
	else
		return REF2_value;
}

// ----------------------------------------------------------------------------------------------------------------------------------------------------------------

