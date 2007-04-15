/************************************************
Burning Man Swarm-Orb
Petey the Programmer '07
Created: 6-Apr-07
Last Modified: 10-Apr-07
Version 10.3

-- main.c --

Project: ORB Electronc Speed Control for ATMega8 chip

	Features: 
		Interupt driven Hardware UART for receiving commands via RS-232 port.
		2 channels of hardware PWM for Drive Motor & Steering Motor control.
		2 channels of A/D converts for Current Sense on Motor1 and Steering Feedback pot.
		2 channels of interupt-on-change encoder inputs for wheel/shaft encoders.
		
----------------------

	v1 : Interupt driven Background timer.
	v2 : polled USART routines.
	v3 : Interupt driven USART routines.
	v4 : Now handles processing Command Strings.
	v6 : PWM working.
	v7 : Move to ATMega8 chip - get A2D functions working.
	v8 : Added Timer0 .. Re-named a2d files.
	v9 : Hardware H-Bridge connected.  Works.  XBee connected.  Works.
		 Added Wheel Encoder interupts.
		 Renamed some files and functions, clean up a little.
   v10 : Add PID function framework - need real motors before tuning can begin.
		 Framework is now pretty much done.  Need specific API for Commands.
		 
----------------------

	Port Pin Assignments:
	
	USART	PortD 0:1	Rx/Tx Rs-232
	Shaft	PortD 2:3	Interupt on change shaft encoder inputs
	ESC		PortD 4:5	Motor2 Fwd/Rev Pins - outputs
	ESC		PortD 6:7	Motor1 Fwd/Rev Pins - outputs
	PWM		PortB 1:2	Motor 1&2 PWM Outputs
	LED		PortB 4:5	Connted to LEDs on STK500 Board
	ADC		PortC 0:1	A/D converter inputs 0..+5volts
	
************************************************/

#include <avr/io.h>
#include <avr/signal.h>
#include <avr/interrupt.h>

#include "UART.h"
#include "putstr.h"
#include "motor.h"
#include "timer.h"
#include "encoder.h"
#include "a2d.h"
#include "steering.h"

#define ON	1
#define OFF 0

// --------------------------------------------------------------------------------------------------------------------------------------------------------------------------
// Prototypes

void Init_Chip(void);

unsigned char build_up_command_string(unsigned char c);
void process_command_string(void);
short command_data(unsigned char firstChr);

void turn_LED(unsigned char LED_Num, unsigned char On_Off);

void check_buttons(void);
void check_heart_beat(unsigned char *state);
void check_encoders(void);

// --------------------------------------------------------------------------------------------------------------------------------------------------------------------------
// Static variable definitions

#define CMD_STR_SIZE 12
static unsigned char Command_String[CMD_STR_SIZE];
static unsigned char CmdStrLen = 0;
static unsigned char doing_Speed_control = 0;

extern volatile unsigned short Timer0_ticks;
extern volatile unsigned char Timer0_10hz_Flag;

// --------------------------------------------------------------------------------------------------------------------------------------------------------------------------
// Init hardware
// This stuff is very chip dependant
// It's a pain keeping track of the port bits here -
// It's nicer when each module configs their own port setups.
// Setup for ATMega8 chip

void Init_Chip(void)
{
						/* Initialize port dir bits -- a '1' bit indicates an output pin*/
  DDRB = 0xFF;			/* PortB1:2 is PWM output */
  DDRC = 0x00;			/* PortC - A/D inputs */
  DDRD = 0xF2;			/* D0 is RS-232 Rx, D1 is Tx, D6:7 are Dir Pins for Motor1 -- 0b 1111 0010 */
  
  UART_Init(51);		// 25 = 19.2k, 12 = 38.4k when system clock is 8Mhz (ATMega8) 
						// 51 = 9600 - XBee default
						
  Motor_PWM_Init();		/* Setup PWM using PortB1:2 on ATMega8 for output */
  
  A2D_Init();			/* Init A/D converters */
  
  Timer0_Init();		/* Init Tick Timer */
  
  Steering_Init();
  
  Encoder_Init();		/* Wheel / Shaft Encoders */
  
  sei();				/* Enable interrupts */

// ---

	putstr("\r\n--- Orb ESC Test v10.3 ---\r\n");

	turn_LED(4,OFF);	// ports come up set to zero, LEDs are inverted
	turn_LED(5,OFF);	// Have to explectly turn them OFF.
}

// --------------------------------------------------------------------------------------------------------------------------------------------------------------------------
// Loop forever doing the various tasks.
// Nothing requires wait states - everything cycles freely.

int main (void)
{
  unsigned char theData;
  unsigned char state = 0;
  
  Init_Chip();

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
		Encoder_sample_speed(MOTOR1_SHAFT_ENCODER);	// save count, restart from zero
		if (doing_Speed_control == ON)
			Motor_do_motor_control();
		Steering_do_Servo_Task();		
		}

	//	check_buttons();	// check push buttons on STK500 board	
  } // forever loop

	return 0;	// make compiler happy
} 

// --------------------------------------------------------------------------------------------------------------------------------------------------------------------------
// process incoming chars - commands start with '$' and end with '*'
// return 1 if command string is complete - else return zero

unsigned char build_up_command_string(unsigned char c)
{
	if (c == '$') {			// this will catch re-starts and stalls as well as valid commands.
		CmdStrLen = 0;
		Command_String[CmdStrLen++] = c;
		return 0;
		}
	
	if (CmdStrLen != 0)		// string has already started
		{
		if (CmdStrLen < CMD_STR_SIZE) 
			Command_String[CmdStrLen++] = c;
		return (c == '*');
		}
		
	return 0;
}

// --------------------------------------------------------------------------------------------------------------------------------------------------------------------------
// we have a complete command string - execute the command.
// Command_String looks like "$A 25*" or "$s -35*" for now.

void process_command_string(void)
{
	short theData;
	char c;
	unsigned char dataPos = 2;	// position of data within Command_String
	
	if (Command_String[dataPos] == ' ') dataPos++;	// skip space
	
	switch (Command_String[1]) {
			
		case 't':	// set drive torque .. neg torque = braking
			theData = command_data(dataPos);	// zero = coast						
			putstr("Set_Torque: ");
			putS16( theData );
			putstr("\r\n");			
			Set_Motor1_Torque( theData, FORWARD );
			doing_Speed_control = ON;	// use PID for motor control
			break;
		
		case 'p':	// set drive PWM  -100 .. 0 .. 100
			theData = command_data(dataPos);	
			putstr("Set_PWM: ");
			putS16( theData );
			doing_Speed_control = OFF;	// control motor directly, don't use PID

			if (theData < 0) {
				theData = -theData;
				Set_Motor1_Power( theData, REVERSE );
				putstr(" Reverse\r\n");
				}
			else {
				Set_Motor1_Power( theData, FORWARD );
				putstr(" Fwd\r\n");
				}			
			break;
		
		case 's':	// set steering  -100 .. 0 .. 100
			theData = command_data(dataPos);	// dataPos = chr pos of data in Cmd String
			putstr("Steer: ");
			putS16( theData );
			putstr("\r\n");
			break;
		
	// ---
	
		case 'S':	// Stop -> All Stop
			Set_Motor1_PWM( 0, FORWARD );
			doing_Speed_control = OFF;
			putstr("STOP\r\n");
			break;
		case 'F':	// Fwd -> Set Direction to Forward
			Motor_Set_Drive_Direction(FORWARD);
			break;
		case 'R':	// Rev -> Set Direction to Reverse
			Motor_Set_Drive_Direction(REVERSE);
			break;

		case '?':	// Send back motor, speed, & steering info
			Motor_dump_data();
			break;

	// ---
	
		case 'K':	// Set PID Gain values  $Kp 8*
			putstr("Set Kx\r\n");
			dataPos = 3;
			if (Command_String[dataPos] == ' ') dataPos++;	// skip space
			theData = command_data(dataPos);
			
			switch (Command_String[2]) {
				case 'p': Motor_set_Kp(theData); break;
				case 'i': Motor_set_Ki(theData); break;
				case 'd': Motor_set_Kd(theData); break;
				}
							
			break;
		}
	
	CmdStrLen = 0;	// clear len, start building next command
}

// --------------------------------------------------------------------------------------------------------------------------------------------------------------------------
// scan the command string just after the command byte, 
// convert Ascii signed number to short word. (16-Bit)
// Commands look like  "$s -95*"

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
	} while (Command_String[cPos] != '*');

	if (sign)
		accum = -accum;
	
	return accum;
}

// --------------------------------------------------------------------------------------------------------------------------------------------------------------------------
// for debugging - use PortB 4:5 output pins to control LEDs on STK500 board.

void turn_LED(unsigned char LED_Num, unsigned char On_Off)
{
	if (On_Off == ON) {
		PORTB &= ~(1 << LED_Num); // clear pin turns LED on
		}
	else
		{
		PORTB |= (1 << LED_Num);	// set pin turns LED off
		}
}

// --------------------------------------------------------------------------------------------------------------------------------------------------------------------------
// Test the encoder inputs.

void check_encoders(void)
{
	unsigned short theData;
	
	theData = Encoder_read_count(MOTOR1_SHAFT_ENCODER);	// read wheel encoder clicks
	if (theData > 10)	
		{
		Encoder_reset(MOTOR1_SHAFT_ENCODER);	// reset back to zero
		turn_LED(4,OFF);
		}
	else if (theData > 5)
		turn_LED(4,ON);
}

// --------------------------------------------------------------------------------------------------------------------------------------------------------------------------
// Blink Heart-Beat LED
// let's me know I'm alive - and resets Timer0

void check_heart_beat(unsigned char *state)
{
//	short theData;
	
	if (Timer0_ticks > 511) {	//  512 tics per second - heart-beat led
		Timer0_reset();
		
		/*
		theData = Encoder_read_speed(MOTOR1_SHAFT_ENCODER);
		putstr("-->Encoder x5 Count: ");
		putS16(theData);
		putstr("\r\n");
		*/
		
		/*
		theData = Encoder_read_count(MOTOR1_SHAFT_ENCODER);
		Encoder_reset(MOTOR1_SHAFT_ENCODER);
		putstr("-->Encoder Count: ");
		putS16(theData);
		putstr("\r\n");
		*/
		
		if (*state == 0) {
			turn_LED(5,ON);
			*state = 1;
			}
		else {
			turn_LED(5,OFF);
			*state = 0;
			}
		}
}

// --------------------------------------------------------------------------------------------------------------------------------------------------------------------------
// poll the push buttons on the STK500 board to trigger function tests.

void check_buttons(void)
{
//  unsigned char mPower = 0;
  unsigned short A2D_Result;

	if(bit_is_clear(PINC, 4)){		//increase duty cycle if switch 0 is pressed
		turn_LED(4,ON);
		/*
		mPower += 10;
		putstr("Button-0  Power = ");
		putU8( mPower );
		UART_send_byte(0x0D);
		UART_send_byte(0x0A);
		Set_Motor1_PWM(mPower, FORWARD);
		*/
		putstr("Timer OFF\r\n");
		Timer0_OFF();
		loop_until_bit_is_set(PINC, 4);
		}

	if(bit_is_clear(PINC, 5)) {		//decease duty cycle if switch 1 is pressed
		turn_LED(4,OFF);
		/*
		mPower -= 10;
		putstr("Button-1  Power = ");
		putU8( mPower );
		UART_send_byte(0x0D);
		UART_send_byte(0x0A);
		Set_Motor1_PWM(mPower, FORWARD);
		*/
		putstr("Timer ON\r\n");
		loop_until_bit_is_set(PINC, 5);
		Timer0_ON();
		}
		
	if(bit_is_clear(PINC, 2)) {		//do a2d converstion - switch #2
		turn_LED(4,ON);		

		/*
		putstr("Button-2. A2D = ");
		A2D_Result = A2D_read_channel(0);
		putS16( A2D_Result );
		putstr("  ");
		A2D_Result = A2D_read_channel(1);
		putS16( A2D_Result );

		UART_send_byte(0x0D);
		UART_send_byte(0x0A);
		*/
		
		loop_until_bit_is_set(PINC, 2);
		turn_LED(4,OFF);
		}

	if(bit_is_clear(PINC, 3)) {		//do timer read
		turn_LED(4,ON);		
		putstr("-- Timer = ");

		A2D_Result = Timer0_ticks;
		putS16( A2D_Result );

		UART_send_byte(0x0D);
		UART_send_byte(0x0A);
		loop_until_bit_is_set(PINC, 3);
		turn_LED(4,OFF);
		}
}

// --------------------------------------------------------------------------------------------------------------------------------------------------------------------------
// End of File
