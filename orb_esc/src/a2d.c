// a2d.c - A2D functions
//
// Version 14.0
// ADC prescaler changed to 64, to give 125kHz AD clock freq.
// With continious sampling, this gives 4,580 samples per second per channel (for 2 channels).

#include <avr/io.h>
#include "a2d.h"
 
// Static variables holding the last value from the ADC

static unsigned short ADValue[8];

// ----------------------------------------------------------------------------------------------------------------------------------------------------------------
// Initialize Analogue to Digital converters.
// Use single ended conversion - one channel at a time, 
// Setup to sample channels only when triggered. (not free running)
// For accurate 10 bit conversion, AD clock must be between 50kHz and 200kHz

void A2D_Init(void)
{
	uint8_t n;
	for (n=0;n<8;n++) ADValue[n] = 0;
		
    ADCSRA = (1<<ADPS1) | (1<<ADPS2);		// Prescale 64 .. @8Mhz ADC clock = 125kHz
	
    ADMUX = (1<<REFS0);						// AVCC voltage ref, ch 0.

    ADCSRA |= (1<<ADEN);	// Enable the ADC
	ADCSRA |= (1<<ADSC);	// Start conversions
}

// ----------------------------------------------------------------------------------------------------------------------------------------------------------------
// poll a2d conversion complete bit - read ADC, start next conversion.
// This avoids waiting or having to turn off interupts to read/write values.
// Sample 8 channels in sequence [0..7] then start over at 0.
// Filter noise using low pass averaging.
// This routine will be called 168,000 times per second. (at 8Mhz)
// Conversions take a couple of cycles to complete.
// It works out to over 9,000 samples per second.

void A2D_poll_adc(void)
{
	uint8_t chanNum;
	
	if (bit_is_clear(ADCSRA, ADSC))	// if clear, conversion is done.
		{
		chanNum = (ADMUX & 0x07);	// Check which channel was just measured
		
	//	ADValue[chanNum++] = ADC;	// Save measured value

		ADValue[chanNum] = ((ADValue[chanNum] * 3) + ADC) / 4;	// Average measured value
		chanNum++;
		
//		ADMUX = _BV(REFS0) | (chanNum & 0x07);	// Use AVCC voltage ref, channel num 0..7				
		ADMUX = _BV(REFS0) | (chanNum & 0x01);	// Use AVCC voltage ref, channel num 0..1				
		ADCSRA |= _BV(ADSC);					// Start next conversion
		}
}

// ----------------------------------------------------------------------------------------------------------------------------------------------------------------
// Return most recent value read from A2D converter for requested channel.
// This avoids waiting or having to turn off interupts to read/write values.

unsigned short A2D_read_channel(uint8_t chanNum)
{
	if (chanNum < 8)
		return ADValue[chanNum];
	else
		return 0;
}

// ----------------------------------------------------------------------------------------------------------------------------------------------------------------
// End of File

