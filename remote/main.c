/************************************************
SWARM Orb dedicated Zigbee remote control transmitter code
http://www.orbswarm.com

Adapted by Jonathan Foote (Head Rotor at rotorbrain.com)
in 2008 from code orginally written by Petey the Programmer '07

Version 1.2

* added deadband
* changed from RGB to HSV

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

/* list of available sound files */
#include "soundlist.h"

#define LED_ON 0
#define LED_OFF 1

#define ABS(x) ((x<0)?(-x):(x))

/* special byte value flag */
#define XVAL ((unsigned char)0xFF)

/* define port and pin assignments for buttons, joysticks, and LEDs */

/* LED outputs are on PORTD*/
#define LED_PORT PORTD
#define BATT_LED 3
#define STAT_LED 2

/* ADC inputs on PORTC  */
#define VREF  3
#define JOYRY 4
#define JOYRX 5
#define JOYLY 6
#define JOYLX 7

/* PORTB needs pullups set */
/* Trigger buttons on PINB */

#define TRIGR2 0x01		/* pin 0 */
#define TRIGR1 0x02		/* pin 1 */
#define TRIGL1 0x04		/* pin 2 */
#define TRIGL2 0x08		/* pin 3 */

/* Joystick buttons on PINB */
#define JOYBR 0x10		/* pin 4 */
#define JOYBL 0x20		/* pin 5 */

/* switches on PINC */
#define VIB_MIN 0x03
#define VIB_MAX 0x02
#define MACRO   0x01

// Prototypes

void Init_Chip(void);
void save_eeprom_settings(void);
void read_eeprom_settings(void);

unsigned char build_up_command_string(unsigned char c);
void process_command_string(void);
short command_data(unsigned char firstChr);

void setLED(unsigned char LED_Num, unsigned char On_Off);
void do_heartbeat(unsigned char *state);
short linearize(short input,short scale);
void pauseMS(unsigned short mS);
#define SEND_ADDR(x) putstr("{6");UART_send_byte((unsigned char)(x) + '0');
void send_addr(unsigned char addr);
void send_hue(char addr, unsigned char pod, short hue, unsigned char val);
void send_index_color(char addr, unsigned char pod, unsigned char index);
void send_light_cmd(char addr, unsigned char pod, char cmd, unsigned char val);
void send_sound(char addr, char* soundname);
void do_keys(unsigned char keys, unsigned char oldkeys);
void do_joy_color(short joyx, short joyy);

// Static variable definitions

extern volatile unsigned short Timer0_ticks;
extern volatile unsigned char Timer0_10hz_Flag;

#ifdef DEBUG
volatile uint8_t debug_out = 1;
#warning DEBUG MODE SET
#else
volatile uint8_t debug_out = 0;
#endif


static char addr;
static unsigned short zero[8];


/* characteristic color table */
/* red, green, blue, magenta, yellow, cyan, white,black */
char ccr[] = {0xFE,0x00,0x00,0xFE, 0xFE,0x00,0xFE,0x00};
char ccg[] = {0x00,0xFE,0x00,0x00, 0xFE,0xFE,0xFE,0x00};
char ccb[] = {0x00,0x00,0xFE,0xFE, 0x00,0xFE,0xFE,0x00};


// Misc. numerical constants

short steer_max=100;
short drive_max=40;
short drive_turbo=50;

/* joysticks are nonlinear. Have more extension in neg direction */
short negmax = 530;
short posmax = 300;

short mindiff  = 0; /* minumum delta. Don't send if changes less than this */
short deadband = 3; /* send zero if +/- deadband away from zero */

short charhue = 0; /* characteristic hue of this orb, derived from addr */


/* previous values for calculating delta */
short oldsteer = -100;
short olddrive = -100;
short oldhue = -100;
short oldval = -100;
unsigned char oldkeys  = 0xFF;

/* mode bits */
char color_hold=0; 		/* 1 to set color */
char identify=0;		/* 1 if stop button hit; flash ID colors */


/* indexes */
unsigned char color = 0; 	/* points to current color in array */
unsigned char sound = 0;	/* points to current sound in array */


// Init hardware
// This stuff is very chip dependant
// Setup for ATMega8 chip

void Init_Chip(void)
{
  unsigned char i;

  /* Initialize port dir bits -- a '1' bit indicates an output pin*/
  DDRB = 0xC0;			/* All bits are button inputs except PB6, PB7 */
  PORTB = 0x3F;			/* All input bits have pullups enabled */


  DDRC = 0x00;			/* PortC - A/D inputs on pins 4:7 */
  PORTC = 0x07;			/* switches on 0-3 */

  DDRD = 0x0E;			/* D0 is RS-232 Rx, D1 is Tx, D2, D3 are LED outputs, D4:7 are address */
  PORTD = 0xF0;			/* High order bits have pullups enabled */


  
  UART_Init(UART_384000);	/* defined in UART.h and global.h */
  
  A2D_Init();			/* Init A/D converters */
  
  Timer0_Init();		/* Init Tick Timer */
  
  sei();			/* Enable interrupts */



  /* first read address from upper 4 bits of PORTD (ROTDIP) */
  /* complement for active low, then shift right by 4 to get actual value) */
  addr = (~PIND & 0xF0) >> 4;
  addr = addr & (char)0x0F;

  /* characteristic hue of this orb */
  charhue = addr*60 + 20;

  if(debug_out){
    putstr("Remote v1.2 at addr: " );
    putU8(addr);
    putstr("\r\n");
  }
  else { 			/* wake up Zigbee */
    putstr("Remote v1.2\r\n" );
  }

  send_light_cmd(addr, XVAL, 'H', charhue); 
  send_light_cmd(addr, XVAL, 'V', (unsigned char) 253); 
  send_light_cmd(addr, XVAL, 'F',XVAL); 
  


  /* and blink status LED that many times */
  for (i=0; i<addr; i++) {
    setLED(BATT_LED, LED_ON);
    pauseMS(100);  
    setLED(BATT_LED, LED_OFF);
    pauseMS(100);  
  }


  setLED(BATT_LED,LED_OFF);	// Have to explicitly turn them OFF.
  setLED(STAT_LED,LED_OFF);	// Have to explicitly turn them OFF.
}


int main (void)
{
  unsigned char state = 0;
  short i, n, t, ch1, ch2;
  unsigned char keys; 		/* PINB key pad (triggers, etc) */
  unsigned char sws;		/* PINC switces */
  short hue, val, diff;

  Init_Chip();
  

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
    //putS16(zero[i]);
    //putstr(" ");
  }
  
  for (;;) {	// loop forever
    
    A2D_poll_adc();					// see if A/D conversion done & re-trigger 
    do_heartbeat( &state );		// Heart-beat is fore-ground -- true indication prog is alive.
    
    
    if (Timer0_10hz_Flag) {		// do these tasks only 10 times per second
      Timer0_10hz_Flag = 0;

      ch1 = A2D_read_channel(JOYRX) - zero[JOYRX];
      diff = ch1 - oldsteer;
      if (diff < 0) diff = -diff; /* abs value */
      
      if(diff > mindiff) {
	send_addr(addr);
	putstr("$s");
	if(abs(ch1) < deadband) 
	  UART_send_byte('0');
	else 
	  putS16(linearize(ch1,steer_max)); 
	  
	putstr("*}\n\r ");
      }
      oldsteer = ch1;

      ch2 = A2D_read_channel(JOYRY) - zero[JOYRY];
      diff = ch2 - olddrive;
      if (diff < 0) diff = -diff;
      if(debug_out & 0) {
	putstr("\r\n ch2:");
	putS16(ch2);
	putstr(" diff: ");
	putS16(diff);
      }
      if(diff > 1) {
	send_addr(addr);
	putstr("$p");
	if(ABS(ch2) < deadband) 
	  UART_send_byte('0');
	else {
	  if(~PINC & VIB_MAX)
	    putS16(linearize(ch2,-drive_turbo)); 
	  else
	    putS16(linearize(ch2,-drive_max)); 
	}
	putstr("*}\n\r ");
      }
      olddrive = ch2;

      /* left joystick controls color */
      hue = A2D_read_channel(JOYLY) - zero[JOYLY];
      val = A2D_read_channel(JOYLX) - zero[JOYLX];
      if((abs(hue - oldhue) > mindiff) | 
	 (abs(val - oldval) > mindiff) ){
	do_joy_color(val,hue);

      }
      oldhue = hue;
      oldval = val;

      /* detect button presses and do appropriate action */
      keys = (unsigned char) PINB;
      if (keys != oldkeys)
	do_keys(keys,oldkeys);
      oldkeys = keys;

      /* detect switches and do appropriate action */
      sws = (unsigned char) ~PINC & 0x07;
      if(debug_out & 0) {
	putstr("\r\n Switches:");
	putB8(sws);
      }

    }
  } // loop forever 

  return 0;	// make compiler happy
} 



/* generate a light control command */
void send_index_color(char addr, unsigned char pod, unsigned char index){
  send_light_cmd(addr, pod, 'T', 50); 
  send_light_cmd(addr, pod, 'R', ccr[(int)index]); 
  send_light_cmd(addr, pod, 'G', ccg[(int)index]); 
  send_light_cmd(addr, pod, 'B', ccb[(int)index]); 
  send_light_cmd(addr, pod, 'F', XVAL); 
}

/* generate a light control command */
void send_hue(char addr, unsigned char pod, short hue, unsigned char val) {
  if (val == XVAL) val = 254;	/* 0xff = XVAL, oops! */
  send_addr(addr);
  putstr("<L");
  if(pod != XVAL) UART_send_byte(pod + '0');
  UART_send_byte('H');
  putS16(hue);
  putstr(">}");
  send_light_cmd(addr, pod, 'V', val); 
  send_light_cmd(addr, pod, 'F', XVAL); 
}


/* "XVAL" pod means supress pod addr (all pods), "XVAL" val means suppress numerical val (for LF commands) */
void send_light_cmd(char addr, unsigned char pod,  char cmd, unsigned char val){
  send_addr(addr);
  putstr("<L");
  if(pod != XVAL) UART_send_byte(pod + '0');
  UART_send_byte(cmd);
  if(val != XVAL ) putU8(val);
  putstr(">}");
}

/* generate a sound command (null soundname sends "stop") */
void send_sound(char addr, char* soundname){
  send_addr(addr);
  putstr("<M ");
  if(soundname != NULL) {
    putstr("VPF ");
    putstr(soundname);
    putstr(".mp3>}");
  }
  else
   putstr("VST>}");
}

void do_keys(unsigned char keys, unsigned char oldkeys){
  unsigned char keydown = 0;
  unsigned char keyup = 0;

  /* find key down transitions: oldkey bits will be high (pullup) */
  /* new key down bits will be low */
  keydown = oldkeys & ~keys;


  /* find key up transitions new key will be high, old key will be low*/
  keyup =  ~oldkeys & keys;

  if(debug_out ){
    putstr("\n\r kup: ");
    putB8(keyup);
    putstr(" kdwn: ");
    putB8(keydown);
  }

  if(keydown & TRIGR1) {      /* R1 trigger button down */
    send_sound(addr,soundlist[sound++]);
    if (sound >= nfiles)
      sound = 0;
  }
  if(keydown & TRIGR2) {      /* R2 trigger button down */
    send_sound(addr,soundlist[sound--]);
    if (sound >= nfiles)		/* wrap around */
      sound = nfiles-1;
    identify=0;
  }

  if(keydown & TRIGL1) {      /* L1 trigger button down */
    send_index_color(addr,XVAL,color++);
    if (color >=6)
      color=0;
    identify=0;
  }
  /* turn off lights and sounds with TL2 */
  if(keydown & TRIGL2) {      /* L2 trigger button down */
    send_light_cmd(addr, XVAL, 'V', 0); 
    send_light_cmd(addr, XVAL, 'F', XVAL); 
    send_sound(addr,NULL);	   /* turn off sounds */
  }

  if(keydown & JOYBL) {		/* the stop button (joyr) was pressed */
    send_addr(addr);
    putstr("$p00*}");
    send_addr(addr);
    putstr("$s00*}");
  }
  if(keydown & JOYBR) {		/* Do identification flash */
    send_hue(addr, 0, 0, (unsigned char) 254);
    send_hue(addr, 1, 0, (unsigned char) 254);
    send_hue(addr, 2, 240, (unsigned char) 254);
    send_hue(addr, 3, 240, (unsigned char) 254);
    send_light_cmd(addr, XVAL, 'C', XVAL);
  }
}


void do_joy_color(short val, short hue) {
  unsigned char  a;

  hue = linearize(hue,180) + 180 + charhue; 
  if(hue > 360) hue -= 360;
  val = linearize(val,-127) + 127; 

  if (~PINC & MACRO) {
    for(a=0;a<6;a++){
      send_hue(a, XVAL, hue, (unsigned char)val);
    }
  }
  else {
    
    if(debug_out & 0) {
      putstr("\r\n hue: ");
      putS16(hue); 
      putstr(" val: ");
      putS16(val); 
    }
    send_hue(addr, XVAL, hue, (unsigned char)val);
  }
}


// for debugging - use PortB 4:5 output pins to control LEDs on STK500 board.
//				 - use PortC 5 for LED on Olimex board



void setLED(unsigned char LED_Num, unsigned char On_Off)
{
  if (On_Off == LED_ON) 
    LED_PORT &= ~(1 << LED_Num); // clear pin turns LED on
  else
    LED_PORT |= (1 << LED_Num);	// set pin turns LED off
}


// Blink Heartbeat LED
// let's me know I'm alive - and resets Timer0
// Toggle the LED once per second
// Send Idle Command to orb to keep it alive.

void do_heartbeat(unsigned char *state)
{
  //  short  ch1, ch2;
  
  //	if (Timer0_ticks > 1023) {	//  1024 tics per second - heart-beat LED
  if (Timer0_ticks > 250) {	//  512 tics per second - heart-beat LED
    Timer0_reset();
#ifdef FOO
    if(debug_out & 0) {
      putstr("\r\n JOYRX, JOYRY =");
      ch1 = A2D_read_channel(JOYRX) - zero[JOYRX];
      putS16(linearize(ch1,256)); 
      ch2 = A2D_read_channel(JOYRY) - zero[JOYRY];
      putS16(linearize(ch2,256));
      putstr("|| JOYLX, JOYLY =");
      ch1 = A2D_read_channel(JOYLX) - zero[JOYLX];
      putS16(ch1); 
      ch2 = A2D_read_channel(JOYLY) - zero[JOYLY];
      putS16(ch2);
      putstr(" PINB");
      putB8(PINB);
    
   }
#endif
    
    if (*state == 0) {
      setLED(STAT_LED,LED_ON);
      *state = 1;
      //if(debug_out)putstr("hb 1");
    }
    else {
      setLED(STAT_LED,LED_OFF);
      *state = 0;
      //if(debug_out) putstr("hb 0");
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

void send_addr(unsigned char addr){
  putstr("{6");
  UART_send_byte(addr + '0');
  UART_send_byte(' ');
}

// pause for mS milliseconds

void pauseMS(unsigned short mS){
  Timer0_reset();
  while (Timer0_ticks < mS) ;
  Timer0_reset();
}

