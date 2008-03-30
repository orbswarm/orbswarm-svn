/************************************************
SWARM Orb dedicated Zigbee remote control transmitter code
http://www.orbswarm.com

2Adapted by Jonathan Foote (Head Rotor at rotorbrain.com)
in 2008 from code orginally written by Petey the Programmer '07

Version 1.0

-- main.c --

Project: ORB Portable Transmitter using ATMega8L chip
	
************************************************/

#include <avr/io.h>
#include <stdlib.h>
#include <avr/interrupt.h>

#include "global.h"
#include "eprom.h"
#include "UART.h"
#include "putstr.h"
#include "timer.h"
#include "a2d.h"

#define LED_ON 0
#define LED_OFF 1






/* define port and pin assignments for buttons, joysticks, and LEDs */

/* LED outputs are on PORTD*/
#define LED_PORT PORTD
#define BATT_LED 3
#define STAT_LED 2

/* ADC inputs on PORTC  */
#define VREF  3
#define JOYRY 4
#define JOYRX 5
#define JOYLX 6
#define JOYLY 7

/* PORTB needs pullups set */
/* Trigger buttons on PORTB */

#define TRIGR1 0
#define TRIGR2 1
#define TRIGL1 2
#define TRIGL2 3

/* Joystick buttons on PORTB */
#define JOYBR 4
#define JOYBL 5





// Prototypes

void Init_Chip(void);
void save_eeprom_settings(void);
void read_eeprom_settings(void);

unsigned char build_up_command_string(unsigned char c);
void process_command_string(void);
short command_data(unsigned char firstChr);

void setLED(unsigned char LED_Num, unsigned char On_Off);
void check_heart_beat(unsigned char *state);
short linearize(short input,short scale);
void pauseMS(unsigned short mS);
void num_to_Str( short v, char *str);



// Static variable definitions

#define CMD_STR_SIZE 12
static unsigned char Command_String[CMD_STR_SIZE];
static unsigned char CmdStrLen = 0;

static short maxLeft, maxRight, centerPos, maxPWM;
extern volatile unsigned short Timer0_ticks;
extern volatile unsigned char Timer0_10hz_Flag;

volatile uint8_t debug_out;

static unsigned char addr;

static unsigned short zero[8];


// Misc. numerical constants

short steer_max=100;
short drive_max=40;
short drive_turbo=60;

/* joysticks are nonlinear. Have more extension in neg direction */
short negmax = 530;
short posmax = 300;


short mindiff = 5; 		/* minumum delta. Don't send if changes less than this */

/* previous values for calculating delta */
int oldsteer = -100;
int olddrive = -100;


// Init hardware
// This stuff is very chip dependant
// Setup for ATMega8 chip

void Init_Chip(void)
{
  char theStr[10];
  unsigned char i;

  /* Initialize port dir bits -- a '1' bit indicates an output pin*/
  DDRB = 0xC0;			/* All bits are button inputs except PB6, PB7 */
  PORTB = 0x3F;			/* All input bits have pullups enabled */


  DDRC = 0x00;			/* PortC - A/D inputs on pins 0:7 */

  DDRD = 0x0E;			/* D0 is RS-232 Rx, D1 is Tx, D2, D3 are LED outputs, D4:7 are address */
  PORTD = 0xF0;			/* High order bits have pullups enabled */


  
  UART_Init(UART_384000);	/* defined in UART.h and global.h */
  
  A2D_Init();			/* Init A/D converters */
  
  Timer0_Init();		/* Init Tick Timer */
  
  sei();			/* Enable interrupts */

  putstr("\r\n--- remote v1.0 ---\r\n");

  /* first read address from upper 4 bits of PORTD (ROTDIP) */
  /* complement for active low, then shift right by 4 to get actual value) */
  addr = (~PIND & 0xF0) >> 4;
  addr = addr & 0x0F;
  putstr("Got addr: " );
  putU8(addr);
  putstr("\r\n");

  /* and blink status LED that many times */
  for (i=0; i<addr; i++) {
    setLED(BATT_LED, LED_ON);
    pauseMS(120);  
    setLED(BATT_LED, LED_OFF);
    pauseMS(120);  
  }


  setLED(BATT_LED,LED_OFF);	// Have to explicitly turn them OFF.
  setLED(STAT_LED,LED_OFF);	// Have to explicitly turn them OFF.
}


int main (void)
{
  unsigned char theData;
  unsigned char state = 0;
  short i, n, t, ch1, ch2;
  char theStr[10];
  
  Init_Chip();
  read_eeprom_settings();
  


  // warm up ADC for 1 second and learn zero points
  for(i =0; i<8;i++) 
    zero[i] = 0;

  for (t=0; t<8; t++) {
    for(i=0; i<8; i++) {
      for (n=0; n<8000; n++)
	A2D_poll_adc();
      zero[i] += A2D_read_channel(i);
    }  
  }
  for(i =0; i<8;i++) {
    zero[i] = zero[i] >> 3;
    putS16(zero[i]);
    putstr(" ");
  }

  putstr("\r\n--- READY ---\r\n");
  
  
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

      ch1 = A2D_read_channel(JOYRX) - zero[JOYRX];
      if(abs(ch1 - oldsteer) > mindiff) {
	putstr("{6");
	UART_send_byte(addr + '0');
	putstr(" $s");
	putS16(linearize(ch1,steer_max)); 
	putstr("*}\n\r ");
	oldsteer = ch1;
      }

      ch1 = A2D_read_channel(JOYRY) - zero[JOYRY];
      if(abs(ch1 - olddrive) > mindiff) {
	putstr("{6");
	UART_send_byte(addr + '0');
	putstr(" $p");
	putS16(linearize(ch1,-drive_max)); 
	putstr("*}\n\r ");
	olddrive = ch1;
      }
      
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

// -----------------------------------------------------------------------------------------------
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


// for debugging - use PortB 4:5 output pins to control LEDs on STK500 board.
//				 - use PortC 5 for LED on Olimex board

void setLED(unsigned char LED_Num, unsigned char On_Off)
{
	if (On_Off == LED_ON) {
		LED_PORT &= ~(1 << LED_Num); // clear pin turns LED on
		}
	else
		{
		LED_PORT |= (1 << LED_Num);	// set pin turns LED off
		}
}


// Blink Heartbeat LED
// let's me know I'm alive - and resets Timer0
// Toggle the LED once per second
// Send Idle Command to orb to keep it alive.

void check_heart_beat(unsigned char *state)
{
  short  ch1, ch2;
  
  //	if (Timer0_ticks > 1023) {	//  1024 tics per second - heart-beat LED
  if (Timer0_ticks > 511) {	//  512 tics per second - heart-beat LED
    Timer0_reset();
    
    if(debug_out) {
      putstr("\r\n JOYRX, JOYRY =");
      ch1 = A2D_read_channel(JOYRX) - zero[JOYRX];
      putS16(linearize(ch1,256)); 
      ch2 = A2D_read_channel(JOYRY)  - zero[JOYRY];
      putS16(linearize(ch2,256));
      putstr("|| JOYLX, JOYLY =");
      ch1 = A2D_read_channel(JOYLX) -   zero[JOYLX];
      putS16(ch1); 
      ch2 = A2D_read_channel(JOYLY)  - zero[JOYLY];
      putS16(ch2);
      putstr(" PINB");
      putU8(PINB);
    
    }
    
    if (*state == 0) {
      setLED(STAT_LED,LED_ON);
      *state = 1;
    }
    else {
      setLED(STAT_LED,LED_OFF);
      *state = 0;
    }
  }
}


/* linearize joystick value */

short linearize(short input, short scale)
{
  float value;
  if(input > posmax) input = posmax;
  if(input < -negmax) value = -negmax;

  value = (float)input;

  if (input > 0)
    value = value / (float)posmax;
  else
    value = (value / (float)negmax);

      
  return((short)(value*scale));
}


// pause for 100 ms second.

void pauseMS(unsigned short mS){
  Timer0_reset();
  while (Timer0_ticks < mS) ;
  Timer0_reset();
}




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
	str[n] = 0;
}


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

