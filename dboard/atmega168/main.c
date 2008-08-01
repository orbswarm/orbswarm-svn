// ---------------------------------------------------------------------
// 
//	File: main.c
//      SWARM Orb 
//	main file for SWARM Orb Motor Control Unit http://www.orbswarm.com
//
//
//	Jonathan (Head Rotor at rotorbrain.com)
// -----------------------------------------------------------------------

/* SWARM autonomous robot daughterboard code
heavily based on original code by Petey the Programmer '07


-- main.c --

----------------------

Change Log

 V20: new version started July 2008 from V18.0
----------------------


	TQFP Port Pin Assignments:
	
	USART	PortD 0:1	Rx/Tx Rs-232            DIP 3:4
	Shaft	PortD 2	        Geartooth sense INT0    DIP 4
	Sft quad PortD 3
debug is PORTD,3
	ESC	PortD 4:5	Motor1 Dir/Disable Pins - outputs DIP 6:11 
	ESC	PortD 6:7	Motor2 Dir/Disable Pins - outputs 
	PWM	PortB 1:2	Motor 1&2 PWM Outputs  DIP 15:16
	ADC	PortC 0:7	A/D converter inputs 0..+3.3 volts	
	LED	PortB 0		Heartbeat LED 
	

************************************************/

#include <avr/io.h>
#include <avr/interrupt.h>

#include "global.h"
#include "UART.h"
#include "putstr.h"
#include "motor.h"
#include "timer.h"
#include "encoder.h"
#include "a2d.h"
#include "steering.h"
#include "IMU.h"

#define ON  1
#define OFF 0

// set or unset this to enable
#define ENABLE_FAIL_SAFE 0

// blink led on this pin for heartbeat
// #define HB_LED 5 /* for olimex proto board, portC */
#define HB_LED 0 /* for daughterboard, port B*/

#define VERSIONSTR "v20.0"

extern volatile unsigned short encoder1_count;
extern volatile short encoder1_speed;
extern volatile unsigned short encoder1_dir;

// from motor.h, this contains motor drive variables
//extern static motor_control_block drive;
// -----------------------------------------------------------------------
// Prototypes

void Init_Chip(void);

unsigned char build_up_command_string(unsigned char c);
void process_command_string(void);
short command_data(unsigned char firstChr);

void turn_LED(unsigned char LED_Num, unsigned char On_Off);
void check_heart_beat(unsigned char *state);
void pause(void);

// -----------------------------------------------------------------------
// Static variable definitions

#define CMD_STR_SIZE 12
static unsigned char Command_String[CMD_STR_SIZE];
static unsigned char CmdStrLen = 0;
static unsigned char Fail_Safe_Counter = 0; /* ticks since last RC command */
static unsigned char Fail_Safe = 0;  /* set to enable stop if no RC signal */


static unsigned char LED_state = 0; /*  used to toggle LED */
static unsigned char heart_ticks = 0;	/*  incrememented at 100hz rate */
static unsigned char steer_servo_ticks = 0; /*  incrememented at 100hz rate */
static unsigned char drive_servo_ticks = 0; /*  incrememented at 100hz rate */



extern volatile unsigned short Timer0_ticks;
extern volatile unsigned char Timer0_100hz_Flag;


volatile uint8_t Steer_Debug_Output; // output debug info for steering PID
volatile uint8_t Drive_Debug_Output; // output debug info for drive PID


volatile unsigned char doing_Speed_control = 0;

// -------------------------------------------------------------------------
// Init hardware
// Setup for ATMega8 chip

void Init_Chip(void)
{


				/* Initialize port dir bits -- a '1' bit indicates an output pin*/
  DDRB = 0xFF;			/* PortB1:2 is PWM output */
  DDRC = 0x00;			/* PortC - A/D inputs on pins 0:7; */
  DDRD = 0xF2;			/* D0 is RS-232 Rx, D1 is Tx  */ 
				/* D2, D3 are INT0, INT1 inputs */ 
				/* (for shaft encoder) */
				/* D4:7 are Dir Pins for Motors -- 0b 1111 0010 */	 
  PORTD |= _BV(PIND2); 		/* turn pullup on for INT0, INT1 inputs */
  PORTD |= _BV(PIND3); 		/* turn pullup on for INT0, INT1 inputs */


  
  //  UART_Init(UART_38400);	// defines from global.h and uart.h
  UART_Init(UART_115200);	// defines from global.h and uart.h
						


  Motor_PWM_Init();  /* Setup PWM using PortB1:2 on ATMega8 for output */
  
  //A2D_Init();			/* Init A/D converters */
  
  Timer0_Init();		/* Init Tick Timer */

  Steering_Init();		/* Steering Servo */
  
  Encoder_Init();		/* Wheel / Shaft Encoders */
 
  sei();			/* Enable interrupts */

// ---



  putstr("\r\nSWARM  dboard MCU ");
  //putstr(VERSIONSTR);
  //putstr("\r\n");
  //pause();
 
	
  turn_LED(HB_LED,OFF);	// Have to explicitly turn them OFF.

  Steer_Debug_Output = OFF;	
  Drive_Debug_Output = OFF;	
  
  Fail_Safe_Counter = 0;
  Fail_Safe = OFF;
}

// ------------------------------------------------------------------------
// Loop forever doing the various tasks.
// Nothing requires wait states - everything cycles freely.


int main (void)
{
  unsigned char theData;
  unsigned char state = 0;
  
  Init_Chip();

  
  drive_servo_ticks = 0;
  steer_servo_ticks = 0;
  for (;;) {	// loop forever

    A2D_poll_adc();	   // see if A/D conversion done & re-trigger 

    
    if (UART_data_in_ring_buf()) { // check for waiting UART data
      theData = UART_ring_buf_byte(); // pull 1 chr from ring buffer
      if (build_up_command_string(theData)) 
	process_command_string(); // execute commands
    }
    
    if (Timer0_100hz_Flag) { // do these tasks at a 100hz rate 
      Timer0_reset();
      check_heart_beat( &state ); // Heart-beat is foreground -- true indication prog is alive.
      Timer0_100hz_Flag = 0;

      //      putS16(encoder1_speed);
      //putS16(encoder1_dir);
      //putS16(PIND &0x08);
      //putstr("\r\n");
      Fail_Safe_Counter++;
      

      ++drive_servo_ticks;
      if(drive_servo_ticks >= 10){ // 100/10 = 10 hz
	// update speed control servo for drive motor
	if (doing_Speed_control)
	  Drive_Servo_Task();
	drive_servo_ticks = 0;
      }

      ++steer_servo_ticks;
      if(steer_servo_ticks >= 5){ // 100/5 = 20 hz
	// update speed control servo for drive motor
	// always do steering servo
	Steering_Servo_Task();
	steer_servo_ticks=0;
      }
    }// end 100 hz loop

  } // forever loop

  return 0;	// make compiler happy
} 

// --------------------------------------------------------------------------
// process incoming chars - commands start with '$' and end with '*'
// return 1 if command string is complete - else return zero

unsigned char build_up_command_string(unsigned char c)
{
  if (c == '$') { // this will catch re-starts and stalls as well as valid commands.
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

// --------------------------------------------------------------------
// We have a complete command string - execute the command.
// While testing / tuning, we're sending out feedback about the commands.  
// This might change. (brain should know what it sent - no need to clog data stream)

void process_command_string(void)
{
  short theData;
  unsigned char dataPos = 2;	// position of data within Command_String
  
  if (Command_String[dataPos] == ' ') dataPos++;	// skip space
  theData = command_data(dataPos);
  
  switch (Command_String[1]) {
    
  case 't':	// set drive speed (PID version) 
    //putstr("Set_Torque: ");
    //putS16( theData );
    //putstr("\r\n");			
    Drive_set_integrator(0);
    Set_Drive_Speed(theData );
    doing_Speed_control = ON;	// use PID for motor control
    break;
    
  case 'p':	// set drive motor PWM directly
    //putstr("Set_PWM: ");
    //putS16( theData );
    doing_Speed_control = OFF;	// control motor directly, don't use PID
    
    if (theData < 0) {
      theData = -theData;
      Set_Motor1_PWM((unsigned char)theData, REVERSE );
      //putstr(" Reverse\r\n");
    }
    else {
      Set_Motor1_PWM( (unsigned char)theData, FORWARD );
      //putstr(" Fwd\r\n");
    }			
    break;

  case 'r':	// set steer  motor PWM directly ONLY FOR TEST
    if (theData < 0) {
      theData = -theData;
      Set_Motor2_PWM((unsigned char)theData, REVERSE );
    }
    else {
      Set_Motor2_PWM( (unsigned char)theData, FORWARD );
    }			
    break;
    
  case 's':	// set steering  -512 .. 0 .. 512 --- change to 0-100% ???
    Steering_set_integrator(0);
    Steering_Set_Target_Pos(theData);
    break;
    

  case '!':	// Stop -> All Stop
    Set_Motor1_PWM( 0, FORWARD );
    doing_Speed_control = OFF;
    //putstr("STOP\r\n");
    break;
    
  case '?':	// Send back motor, speed, & steering info
    Motor_dump_data();
    Steering_dump_data();
    break;
    
  case 'Q':	// Send back motor, speed, & steering info
    switch (Command_String[2]) {
    case 'D': 
      Get_Drive_Status();
      break;
    case 'S': 
      Get_Steering_Status();
      break;
    case 'I': 
      Get_IMU_Data();
      break;
    case '\0': 
    case '*': 
      Get_Drive_Status();
      Get_Steering_Status();
      Get_IMU_Data();
      break;
    }
    break;
    
    
  case 'K':	// Set Drive control PID Gain values  $Kp 8*
    dataPos = 3;
    if (Command_String[dataPos] == ' ') dataPos++;	// skip space
    theData = command_data(dataPos);
    
    switch (Command_String[2]) {
    case 'p': Drive_set_Kp(theData); break;
    case 'i': Drive_set_Ki(theData); break;
    case 'd': Drive_set_Kd(theData); break;
    case 'm': Drive_set_min(theData); break; /* min PWM value */
    case 'x': Drive_set_max(theData); break; /* max PWM value */
    case 'l': Drive_set_intLimit(theData); break; /* max PWM value */
    case 'b': Drive_set_dead_band(theData); break; /* close enuf to target*/
      //    case 'c': drive.maxCurrent = theData; break; /* set current limit*/    
    }
    Motor_dump_data();
    break;
    
  case 'S':	// Set Steering control PID Gain values 
    dataPos = 3;
    if (Command_String[dataPos] == ' ') dataPos++;	// skip space
    theData = command_data(dataPos);
    
    switch (Command_String[2]) {
    case 'p': Steering_set_Kp(theData); break;
    case 'i': Steering_set_Ki(theData); break;
    case 'd': Steering_set_Kd(theData); break;
    case 'a': Steering_set_accel(theData); break;
    case 'm': Steering_set_min(theData); break; /* min PWM value */
    case 'x': Steering_set_max(theData); break; /* max PWM value */
    case 'b': Steering_set_dead_band(theData); break; /* close enuf to target*/
    }
    Steering_dump_data();
    break;
    
  case 'L':	// Turn On / Off Logging Debug data
    /* set bit one to turn on drive debug */
    /* set bit two to turn on steering debug */
    // thus $L3* turns them both on
    Steer_Debug_Output = OFF;
    Drive_Debug_Output = OFF;
    
    if (theData & 0x01) 
      Drive_Debug_Output = ON;
    
    if (theData & 0x02) 
      Steer_Debug_Output = ON;
    break;    
    
  case 'W':	// Write Motor or Steering PID data to EEPROM : WM or WS
    if (Command_String[2] == 'D'){ 
      putstr("EEPROM disabled!");
      //Motor_save_PID_settings();
      Motor_dump_data();
    }      
    if (Command_String[2] == 'S'){
      putstr("EEPROM disabled!");
      //Steering_save_PID_settings();
      Steering_dump_data();
    }
    break;
    
  case 'F':			/* turn failsafe watchdog on or off */
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
    
    Fail_Safe_Counter = 0;   /* we got a command; clear counter */
    CmdStrLen = 0;	     /* clear len, start building next command */
  }
}
// ---------------------------------------------------------------------------
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
  
  // still a bug for non-numerical chars. Should use isnum(char) 
  while (Command_String[cPos] != '*') {
    accum = (accum * 10) + (Command_String[cPos++] - '0');
  } 
  
  if (sign)
    accum = -accum;
  if((cPos - sign - firstChr) > 0) // if we got at least one numeric char
    return accum;
  else // handle null input strings 
    return (short) 0;
}

// ---------------------------------------------------------------------------
// daughterboard uses PORTB, 0

void turn_LED(unsigned char LED_Num, unsigned char On_Off)
{
  if (On_Off == ON) {
    PORTB &= ~(1 << LED_Num); // clear pin turns LED on
  }
  else {
    PORTB |= (1 << LED_Num);	// set pin turns LED off
  }
}


// --------------------------------------------------------------------------
// this is called at a 100hz rate from the main timing loop
// Blink heartbeat LED once per second
// Check Fail Safe counter - if com port looks dead - stop the orb

void check_heart_beat(unsigned char *state)
{
  
  if (heart_ticks > 50) {	//  100 ticks per second
    
    if (LED_state == 0) { // toggle hearbeat LED
      turn_LED(HB_LED,ON);
      LED_state = 1;
    }
    else {
      turn_LED(HB_LED,OFF);
      LED_state = 0;
    }
    heart_ticks = 0;
  }
  else
    heart_ticks++;


  // Fail_Safe_Counter is incremented at 100 hz rate
  if (Fail_Safe && (Fail_Safe_Counter > 150)) {	
    Set_Motor1_PWM( 0, FORWARD );
    doing_Speed_control = OFF;
    //putstr("Fail-Safe-STOP\r\n");
  }
}


// pause for 1/2 second.

void pause(void)
{
	Timer0_reset();
	while (Timer0_ticks < 3600) ;
	Timer0_reset();
}
