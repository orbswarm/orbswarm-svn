/************************************************
Burning Man Swarm-Orb Maker Faire Transmitter
http://www.orbswarm.com
Written by Petey the Programmer '07
Created: 11-May-07
Version 1.0

-- main.c --

Project: ORB Portable Transmitter using ATMega8L chip
	
************************************************/

#include <avr/io.h>
#include <stdlib.h>
#include <avr/signal.h>
#include <avr/interrupt.h>

#include "eprom.h"
#include "UART.h"
#include "putstr.h"
#include "timer.h"
#include "a2d.h"

#define ON	1
#define OFF 0

// Use port B:0 on hand wired proto-board
// Use port C:5 on Olimex board

#define LED_PORT	PORTC
#define LED_PIN		5

// --------------------------------------------------------------------------------------------------------------------------------------------------------------------------
// Prototypes

void Init_Chip(void);
void save_eeprom_settings(void);
void read_eeprom_settings(void);

unsigned char build_up_command_string(unsigned char c);
void process_command_string(void);
short command_data(unsigned char firstChr);

void turn_LED(unsigned char LED_Num, unsigned char On_Off);
void check_heart_beat(unsigned char *state);
void pause(void);
void num_to_Str( short v, char *str);

// --------------------------------------------------------------------------------------------------------------------------------------------------------------------------
// Static variable definitions

#define CMD_STR_SIZE 12
static unsigned char Command_String[CMD_STR_SIZE];
static unsigned char CmdStrLen = 0;

static short maxLeft, maxRight, centerPos, maxPWM;
extern volatile unsigned short Timer0_ticks;
extern volatile unsigned char Timer0_10hz_Flag;

volatile uint8_t Debug_Output;

// --------------------------------------------------------------------------------------------------------------------------------------------------------------------------
// Init hardware
// This stuff is very chip dependant
// Setup for ATMega8 chip

void Init_Chip(void)
{
						/* Initialize port dir bits -- a '1' bit indicates an output pin*/
  DDRB = 0xFF;			/* PortB1:2 is PWM output */
  DDRC = 0xF0;			/* PortC - A/D inputs on pins 0:1; LED output on pin 4:5 b 1111 0000 */
  DDRD = 0xF2;			/* D0 is RS-232 Rx, D1 is Tx, D4:7 are Dir Pins for Motors -- 0b 1111 0010 */
  
//  OSCCAL = 0xBE;		// 8.0 Mhz calibration number 

  UART_Init(12);		// 25 = 19.2k, 12 = 38.4k when system clock is 8Mhz (ATMega8) 
						// 51 = 9600 - XBee default baud rate
						
  A2D_Init();			/* Init A/D converters */
  
  Timer0_Init();		/* Init Tick Timer */
    
  sei();				/* Enable interrupts */

// ---

	putstr("\r\n--- Orb Xmit v2.0 ---\r\n");
//	pause();
	
//	turn_LED(4,OFF);		// ports come up set to zero, LEDs are inverted.
	turn_LED(LED_PIN,OFF);	// Have to explicitly turn them OFF.
	Debug_Output = OFF;	
}

// --------------------------------------------------------------------------------------------------------------------------------------------------------------------------
// Loop forever doing the various tasks.
// Nothing requires wait states - everything cycles freely.
// At 8.0 Mhz, the main loop cycles 16,800+ times every 100ms (10hz loop) = 168,000 cps

int main (void)
{
  unsigned char theData;
  unsigned char state = 0;
  short n, t, ch1, ch2;
  short prevSpeed, prevSteer;
  short SpeedValue, SteerValue;
  char theStr[10];
  float fSpeed, fSteer;
  
  Init_Chip();
  read_eeprom_settings();
  
  for (t=0; t<50; t++) 
	{	// warm up ADC for 1 second
	  for (n=0; n<8000; n++)
		A2D_poll_adc();
	}

  putstr("\r\n--- READY ---\r\n");
  
  prevSpeed = 0;
  prevSteer = 0;
  
  for (;;) {	// loop forever

	A2D_poll_adc();					// see if A/D conversion done & re-trigger 
	check_heart_beat( &state );		// Heart-beat is fore-ground -- true indication prog is alive.

	
	if (UART_data_in_ring_buf()) {			// check for waiting UART data
		theData = UART_ring_buf_byte();		// pull 1 chr from ring buffer
		if (build_up_command_string(theData)) 
			process_command_string();		// execute commands
		}
	
	if (Timer0_10hz_Flag) {		// do these tasks only 10 times per second
		Timer0_10hz_Flag = 0;
	
		ch1 = A2D_read_channel(SPEED_CONTROL_CHANNEL);
		SpeedValue = (ch1 - 485);					// returns values -140..0..140  (485)

		if (abs(SpeedValue) < 5) SpeedValue = 0;	// dead band - prevent chatter
		fSpeed = SpeedValue / 140.0;
		SpeedValue = (fSpeed * maxPWM);
		
		ch2 = A2D_read_channel(STEERING_CONTROL_CHANNEL);
		SteerValue = (380 - ch2) + (centerPos * 2);		// returns values -140..0..140  (380)

		fSteer = SteerValue / 140.0;
		if (fSteer < 0)
			SteerValue = (fSteer * maxLeft);
		else
			SteerValue = (fSteer * maxRight);
		
		if (SpeedValue != prevSpeed) {
			theStr[0] = '$';
			theStr[1] = 'p';
			num_to_Str( SpeedValue, theStr );
			putstr( theStr );
			putstr("\r\n");
			}
		
		if (SteerValue != prevSteer) {
			theStr[0] = '$';
			theStr[1] = 's';
			num_to_Str( SteerValue, theStr );
			putstr( theStr );
			putstr("\r\n");
			}
		
		prevSpeed = SpeedValue;
		prevSteer = SteerValue;
		}
		
  } // forever loop

	return 0;	// make compiler happy
} 

// --------------------------------------------------------------------------------------------------------------------------------------------------------------------------
// process incoming chars - commands start with '>' and end with '<'
// return 1 if command string is complete - else return zero

#define START_CHAR	'>'
#define END_CHAR	'<'

unsigned char build_up_command_string(unsigned char c)
{
	if (c == START_CHAR) {		// this will catch re-starts and stalls as well as valid commands.
		CmdStrLen = 0;
		Command_String[CmdStrLen++] = c;
		return 0;
		}
	
	if (CmdStrLen != 0)		// string has already started
		{
		if (CmdStrLen < CMD_STR_SIZE) 
			Command_String[CmdStrLen++] = c;
		return (c == END_CHAR);
		}
		
	return 0;
}

// --------------------------------------------------------------------------------------------------------------------------------------------------------------------------
// We have a complete command string - execute the command.

void process_command_string(void)
{
	short theData;
	unsigned char cVal;
	unsigned char dataPos = 2;	// position of data within Command_String
	
	if (Command_String[dataPos] == ' ') dataPos++;	// skip space
	theData = command_data(dataPos);
	
	switch (Command_String[1]) {

		case 'a':	// Set steering max Left value
			maxLeft = theData;
			putstr("Set Steering Left-Max ");
			putS16(theData);
			putstr("\r\n");
			break;

		case 'b':	// Set steering max Right value
			maxRight = theData;
			putstr("Set Steering Right-Max ");
			putS16(theData);
			putstr("\r\n");
			break;

		case 'c':	// Set steering center value
			centerPos = theData;
			putstr("Set Steering Center ");
			putS16(theData);
			putstr("\r\n");
			break;

		case 'd':	// Set PWM max value
			maxPWM = theData;
			putstr("Set Max PWM");
			putS16(theData);
			putstr("\r\n");
			break;

		case 'w':	// write values to eeprom
			putstr("Save to EEPROM\r\n");
			save_eeprom_settings();
			break;
			
		case 'X':	// Set OSCAL value
			cVal = (unsigned char)theData;
			putstr("Set OSCAL ");
			putS16(theData);
			putstr("\r\n");
			OSCCAL = theData;
			break;

		}	
	CmdStrLen = 0;			// clear len, start building next command
}

// --------------------------------------------------------------------------------------------------------------------------------------------------------------------------
// scan the command string just after the command byte, 
// convert Ascii signed number to short word. (16-Bit)

short command_data(unsigned char firstChr)
{
	short accum = 0;
	unsigned char sign = 0;
	unsigned char cPos = firstChr;

	if (Command_String[firstChr] == '-') {
		sign = 1;
		cPos++;
		}
	
	do {
		accum = (accum * 10) + (Command_String[cPos++] - '0');
	} while (Command_String[cPos] != END_CHAR);

	if (sign)
		accum = -accum;
	
	return accum;
}

// --------------------------------------------------------------------------------------------------------------------------------------------------------------------------
// for debugging - use PortB 4:5 output pins to control LEDs on STK500 board.
//				 - use PortC 5 for LED on Olimex board

void turn_LED(unsigned char LED_Num, unsigned char On_Off)
{
	if (On_Off == ON) {
		LED_PORT &= ~(1 << LED_Num); // clear pin turns LED on
		}
	else
		{
		LED_PORT |= (1 << LED_Num);	// set pin turns LED off
		}
}

// --------------------------------------------------------------------------------------------------------------------------------------------------------------------------
// Blink Heart-Beat LED
// let's me know I'm alive - and resets Timer0
// Toggle the LED once per second
// Send Idle Command to orb to keep it alive.

void check_heart_beat(unsigned char *state)
{
//	short theData;
	
//	if (Timer0_ticks > 1023) {	//  1024 tics per second - heart-beat LED
	if (Timer0_ticks > 511) {	//  512 tics per second - heart-beat LED
		Timer0_reset();
		
		putstr( "$i*\r\n" );	// send idle command - 
//		putstr("...Testing 1234\r\n...");
		
		
		if (*state == 0) {
			turn_LED(LED_PIN,ON);
			*state = 1;
			}
		else {
			turn_LED(LED_PIN,OFF);
			*state = 0;
			}
		
		}
}

// --------------------------------------------------------------------------------------------------------------------------------------------------------------------------
// pause for 1 second.

void pause(void)
{
	Timer0_reset();
	while (Timer0_ticks < 512) ;
	Timer0_reset();
}

// --------------------------------------------------------------------------------------------------------------------------------------------------------------------------
// special version - output 10 bit number into formatted string
// string enters with pre-amble: "$s..."
// add trailing "*" chr to end of string: $s-100*

void num_to_Str( short v, char *str)
{
	uint8_t n = 2;
	short mx, maxVal = 0;
	
	if (v < 0) 
		{
		str[n++] = '-';
		v = -v;
		}
	if (v > 1024) v = 1024;
	
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
	str[n++] = '*';
	str[n] = 0;
}

// ------------------------------------------------------------------------------------------------------------------------------------------------------
// These read and write 8-bit values from eeprom.

void save_eeprom_settings(void)
{
	uint8_t checksum;
	checksum = 0 - (maxLeft + maxRight + centerPos + maxPWM);
	
	eeprom_Write( EEPROM_START, maxLeft );
	eeprom_Write( EEPROM_START+1, maxRight );
	eeprom_Write( EEPROM_START+2, centerPos );
	eeprom_Write( EEPROM_START+3, maxPWM );
	eeprom_Write( EEPROM_START+4, checksum );
}

// ------------------------------------------------------------------------------------------------------------------------------------------------------

void read_eeprom_settings(void)
{
	uint8_t v1,v2,v3,v4,checksum;

	v1 = eeprom_Read( EEPROM_START );
	v2 = eeprom_Read( EEPROM_START+1 );
	v3 = eeprom_Read( EEPROM_START+2 );
	v4 = eeprom_Read( EEPROM_START+3 );
	checksum = eeprom_Read( EEPROM_START+4 );

	if (!((v1 + v2 + v3 + v4 + checksum) & 0xFF))
		{	// checksum is OK - load values into motor control block
		maxLeft = v1;
		maxRight = v2;
		centerPos = v3;
		maxPWM = v4;
		}
	else
		{
		putstr("Initialize EEPROM Values\r\n");
		maxLeft = 120;
		maxRight = 120;
		centerPos = 0;
		maxPWM = 60;		
		}
}

// --------------------------------------------------------------------------------------------------------------------------------------------------------------------------
// End of File
