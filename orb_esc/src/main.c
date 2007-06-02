/************************************************
Burning Man Swarm-Orb Motor Control Unit
http://www.orbswarm.com
Written by Petey the Programmer '07
Created: 6-Apr-07
Last Modified: 7-May-07
Version 14.3

-- main.c --

Project: ORB Motor Control Unit for ATMega8 chip

	Features: 
		Interrupt driven Hardware UART for receiving commands via RS-232 port.
		2 channels of hardware PWM for Drive Motor & Steering Motor control.
		8 channels of A/D converters for Motor Current, Steering Feedback, and 5DOF IMU.
		2 channels of interrupt on change inputs for shaft or Quadrature encoders.
		API supports tuning of PIDs via serial input - can use XBee for remote tuning.
		
----------------------

Change Log

	v1 : Interrupt driven Background timer.
	v2 : polled USART routines.
	v3 : Interrupt driven USART routines.
	v4 : Now handles processing Command Strings.
	v6 : PWM working.
	v7 : Move to ATMega8 chip - get A2D functions working.
	v8 : Added Timer0 .. Re-name A2D files.
	v9 : Hardware H-Bridge connected.  Works.  XBee connected.  Works.
		 Added Wheel Encoder interrupts.
		 Renamed some files and functions, clean up a little.
   v10 : Add PID function framework - need real motors before tuning can begin.
		 Framework is now pretty much done.  Need specific API for Commands.
   v11 : Get Steering Servo functions working.
   v12 : Change to Open Source Speed Controller (OSSC) H-Bridge Hardware.
   v13 : Save PID params to eeprom.  Rework A2D code - setup for IMU & oversampling.
		 Change encoder from interrupts to polling for Quadrature encoder.
		 and changed back again to interrupts for single dir sprocket encoder.
		 Added Fail Safe cmd.  1 second w/o com data, Orb will stop.
 v13.4 : Added iTerm to Steering PID function.
		 Added Velocity / Torque PID control.
 v14.1 : Added 5DOF IMU output.
		 IMU, Orb Speed, and Steering data is output 10 times per second to Linux Brain
		 
----------------------

	Port Pin Assignments:
	
	USART	PortD 0:1	Rx/Tx Rs-232
	Shaft	PortD 2:3	Interrupt on change shaft encoder inputs
	ESC		PortD 4:5	Motor2 Dir/Disable Pins - outputs
	ESC		PortD 6:7	Motor1 Dir/Disable Pins - outputs
	PWM		PortB 1:2	Motor 1&2 PWM Outputs
	ADC		PortC 0:7	A/D converter inputs 0..+5volts	
	LED		PortC 5		Heartbeat LED on Olimex Board
	
************************************************/

#include <avr/io.h>
#include <avr/signal.h>
#include <avr/interrupt.h>

#include "eprom.h"
#include "UART.h"
#include "putstr.h"
#include "motor.h"
#include "timer.h"
#include "encoder.h"
#include "a2d.h"
#include "steering.h"
#include "IMU.h"

#define ON	1
#define OFF 0

// --------------------------------------------------------------------------------------------------------------------------------------------------------------------------
// Prototypes

void Init_Chip(void);

unsigned char build_up_command_string(unsigned char c);
void process_command_string(void);
short command_data(unsigned char firstChr);

void turn_LED(unsigned char LED_Num, unsigned char On_Off);
void check_heart_beat(unsigned char *state);
void pause(void);

// --------------------------------------------------------------------------------------------------------------------------------------------------------------------------
// Static variable definitions

#define CMD_STR_SIZE 12
static unsigned char Command_String[CMD_STR_SIZE];
static unsigned char CmdStrLen = 0;
static unsigned char doing_Speed_control = 0;
static unsigned char using_iTerm_PID = 1;
static unsigned char Fail_Safe_Counter = 0;
static unsigned char Fail_Safe = 1;

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
  
  UART_Init(12);		// 25 = 19.2k, 12 = 38.4k when system clock is 8Mhz (ATMega8) 
						// 51 = 9600 - XBee default baud rate
						
  Motor_PWM_Init();		/* Setup PWM using PortB1:2 on ATMega8 for output */
  
  A2D_Init();			/* Init A/D converters */
  
  Timer0_Init();		/* Init Tick Timer */
  
  Steering_Init();		/* Steering Servo */
  
  Encoder_Init();		/* Wheel / Shaft Encoders */
  
  sei();				/* Enable interrupts */

// ---

	putstr("\r\n--- Orb MCU v14.3 ---\r\n");
	pause();
	
//	turn_LED(4,OFF);	// ports come up set to zero, LEDs are inverted.
	turn_LED(5,OFF);	// Have to explicitly turn them OFF.
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
  
  Init_Chip();

  Motor_read_PID_settings( &using_iTerm_PID );	// read saved PID gain factors from EEPROM
  Steering_read_PID_settings();

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
		Fail_Safe_Counter++;
		
		Encoder_sample_speed(MOTOR1_SHAFT_ENCODER);	// save count, restart from zero
		
		if (doing_Speed_control)
			Motor_do_motor_control(using_iTerm_PID);
		
		Steering_do_Servo_Task();

// IMU isn't installed yet...
		IMU_output_data_string();		// Send out 5DOF IMU data to Central Linux Brain
		}
		
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
// We have a complete command string - execute the command.
// Command_String looks like "$A 25*" or "$s -35*" for now.
// While testing / tuning, we're sending out feedback about the commands.  
// This might change. (brain should know what it sent - no need to clog data stream)

void process_command_string(void)
{
	short theData;
	unsigned char dataPos = 2;	// position of data within Command_String
	
	if (Command_String[dataPos] == ' ') dataPos++;	// skip space
	theData = command_data(dataPos);
	
	switch (Command_String[1]) {
			
		case 't':	// set drive torque 
			putstr("Set_Torque: ");
			putS16( theData );
			putstr("\r\n");			
			Set_Motor1_Torque( theData, FORWARD );
			doing_Speed_control = ON;	// use PID for motor control
			break;
		
		case 'p':	// set drive PWM  -100 .. 0 .. 100
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
		
		case 's':	// set steering  -512 .. 0 .. 512 --- change to 0-100% ???
			putstr("Steer: ");
			putS16( theData );
			putstr("\r\n");
			Steering_Set_Target_Pos(theData);
			break;
		
	// ---
	
		case 'S':	// Stop -> All Stop
			Set_Motor1_PWM( 0, FORWARD );
			doing_Speed_control = OFF;
			putstr("STOP\r\n");
			break;

		case '?':	// Send back motor, speed, & steering info
			Motor_dump_data();
			Steering_dump_data();
			break;
		
		case 'P':	// Select which PID to use - user should also reset PID gain values.
			using_iTerm_PID = theData;
			if (using_iTerm_PID)
				putstr("Using iTerm PID\r\n");
			else
				putstr("Using Std PID\r\n");
			break;
		
		case 'V':	// Select either Velocity or Torque PID control
			if (theData == 1)
				putstr("Velocity PID");
			else
				putstr("Torque PID");
			Motor_set_PID_feedback(theData);	// 1 = Velocity, 0 = Torque
			break;
			
	// ---
	
		case 'K':	// Set Motor control PID Gain values  $Kp 8*
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

		case 'L':	// Turn On / Off Logging Debug data
			if (theData == 1) 
				Debug_Output = ON;
			else
				Debug_Output = OFF;
			break;

		case 'W':	// Write Motor or Steering PID data to EEPROM : WM or WS
			putstr("Write to EEPROM");
			if (Command_String[2] == 'M') 
				Motor_save_PID_settings(using_iTerm_PID);
			if (Command_String[2] == 'S') 
				Steering_save_PID_settings();
			break;

		case 'F':	// Fail Safe ON / OFF -- default is ON
			if (theData == 1) {
				Fail_Safe = ON;
				putstr("Fail Safe ON\r\n");
				}
			else {
				Fail_Safe = OFF;
				putstr("Fail Safe OFF\r\n");
				}
			break;

	// ---

		case 'a':	// Send steering max accel value
			Steering_set_accel(theData);
			break;

		case 'b':	// Send steering min PWM value
			Steering_set_min(theData);
			break;

		case 'c':	// Send steering max PWM value
			Steering_set_max(theData);
			break;

		case 'd':	// Send steering dead band value
			Steering_set_dead_band(theData);
			break;

		case 'e':	// Send steering Kd value
			Steering_set_Kd(theData);
			break;

		case 'f':	// Send steering Ki value
			Steering_set_Ki(theData);
			break;

		case 'v':	// Send steering Servo Gain factor Kp
			Steering_set_Kp(theData);
			break;

		}
	
	Fail_Safe_Counter = 0;	// com is alive - clear counter
	CmdStrLen = 0;			// clear len, start building next command
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
//				 - use PortC 5 for LED on Olimex board

void turn_LED(unsigned char LED_Num, unsigned char On_Off)
{
	if (On_Off == ON) {
		PORTC &= ~(1 << LED_Num); // clear pin turns LED on
		}
	else
		{
		PORTC |= (1 << LED_Num);	// set pin turns LED off
		}
}

// --------------------------------------------------------------------------------------------------------------------------------------------------------------------------
// Blink Heart-Beat LED
// let's me know I'm alive - and resets Timer0
// Toggle the LED once per second
// Check Fail Safe counter - if com port looks dead - stop the orb

void check_heart_beat(unsigned char *state)
{
//	short theData;
	
	if (Timer0_ticks > TICKS_PER_SECOND) {	//  490 tics per second - heart-beat LED
		Timer0_reset();
				
		if (*state == 0) {
			turn_LED(5,ON);
			*state = 1;
			}
		else {
			turn_LED(5,OFF);
			*state = 0;
			}
		
		// if more than 1.5 seconds goes by w/o cmd - STOP
		if (Fail_Safe && (Fail_Safe_Counter > 15)) {	
			if (Motor_Read_Drive_PWM() != 0) {
				Set_Motor1_PWM( 0, FORWARD );
				doing_Speed_control = OFF;
				putstr("Fail Safe STOP\r\n");
				}
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
// End of File
