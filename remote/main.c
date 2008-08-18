/************************************************
SWARM Orb dedicated Zigbee remote control transmitter code
http://www.orbswarm.com

Adapted by Jonathan Foote (Head Rotor at rotorbrain.com)
in 2008 from code orginally written by Petey the Programmer '07

Version 1.2 (Coachella)

* added deadband
* changed from RGB to HSV

Version 1.3 (BM 2008)

* Increased power levels and deadband (40->60, 60->100 (turbo)
* Added HALT and MODE SPU commands
* new soundinterface

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
//#include "soundlist.h"
#include "smallsound.h"


short steer_max=100;
short drive_max=70;		/* was 40 */
short drive_turbo=100;		/* was 60 */

short negmax = 500;
short posmax = 500;

#define MINRDIFF ((short) 5) /* minimum diff for right (power) joyst */
#define MINLDIFF ((short) 10) /* deadband for left  (illum) joyst */

#define NUM_HAPPYSOUNDS 16
#define NUM_ANGRYSOUNDS 16
#define NUM_SONGSOUNDS  8
#define NUM_ORBS 6

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

/* increasing voltage means smaller VREF value */
#define _2V58 992 		/* voltage ref values */
#define _2V70 984 		/* voltage ref values */
#define _2V80 976 		/* voltage ref values */
#define _2V90 968 		/* voltage ref values */
#define _3V00 960 		/* voltage ref values */


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
#define VIB_MIN 0x04
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
void send_sound(char start_addr, char end_addr, short soundnum, char *prefix);
void send_song(short soundnum, char *prefix);
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

short deadband = 3; /* send zero if +/- deadband away from zero */

short thishue = 0; /* current hue */
short thisval = 0; /* current value */

/* previous values for calculating delta */
short oldsteer = -100;
short olddrive = -100;
short oldhue = -100;
short oldval = -100;
unsigned char oldkeys  = 0xFF;

/* mode bits */
char color_hold=0; 		/* 1 to set color */
char identify=0;		/* 1 if stop button hit; flash ID colors */
char low_batt=0;		/* set if battery low condition) */


/* indexes */
unsigned char color = 0; 	/* points to current color in array */
unsigned char happysoundcount = 0;	/* points to current sound in array */
unsigned char angrysoundcount = 0;	/* points to current sound in array */
unsigned char songsoundcount = 0;

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
  thishue = addr*60 + 20;
  thisval = 127;

  putstr("Remote v1.3\r\n" );

  send_light_cmd(addr, XVAL, 'H', thishue); 
  send_light_cmd(addr, XVAL, 'V', (unsigned char) thisval); 
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
  short abshue, absval;

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

      /* check voltage ref: are we below thresh?*/
      ch1 = A2D_read_channel(VREF);
      if(ch1 > _2V70){ 		/* increasing VREF val means decreasing bat V */
	low_batt = 1;
      }
      else
	low_batt = 0;

      ch1 = A2D_read_channel(JOYRX) - zero[JOYRX];
      diff = ch1 - oldsteer;
      if (diff < 0) diff = -diff; /* abs value */
      
      if(diff > MINRDIFF) {
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
      if(diff > MINRDIFF ) {
	send_addr(addr);
	putstr("$p");
	diff = ch2;
	if (diff < 0) diff = -diff;
	if(diff < deadband) 
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

      if(debug_out) {
	putstr("\n\rD:");
	putS16(ch2);
	putstr(" S:");
	putS16(ch1);
	putstr(" H:");
	putS16(hue);
	putstr(" V:");
	putS16(val);
      }

      abshue = (hue < 0) ? -hue : hue;
      absval = (val < 0) ? -val : val;
      if((abshue > MINLDIFF) | 
	 (absval > MINLDIFF) ){
	do_joy_color(val,hue);
      }
      
      //oldhue = hue;
      //oldval = val;

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
void send_sound(char start_addr, char end_addr, short soundnum, char *prefix){
  char dest;
  
  for(dest=start_addr;dest<end_addr;dest++){
    send_addr(dest);
    putstr("<M ");
    if(prefix == NULL) {
      putstr("VST>}");
    }
    else{
      putstr("VPF ");
      putstr(prefix);
      putS16(soundnum);
      putstr(".mp3>}");
    }
  }
}


/* generate a song command (null soundname sends "stop") */
void send_song(short soundnum, char *prefix){
  
  char dest = 0; 		/* address of destination orb */
  for(dest=0;dest<NUM_ORBS;dest++){
    send_addr(dest);
    putstr("<M ");
    if(prefix==NULL) {
      putstr("VST>}");
    }
    else {
      putstr("VPF ");
      putstr(prefix);
      putS16(soundnum);
      putstr("_");
      UART_send_byte(dest + '0');
      putstr(".mp3>}");
    }
  }
}

void do_keys(unsigned char keys, unsigned char oldkeys){
  unsigned char keydown = 0;
  unsigned char keyup = 0;
  unsigned char start_addr=0;
  unsigned char end_addr=0;


  /* find key down transitions: oldkey bits will be high (pullup) */
  /* new key down bits will be low */
  keydown = oldkeys & ~keys;


  /* find key up transitions new key will be high, old key will be low*/
  keyup =  ~oldkeys & keys;

/*   if(debug_out ){ */
/*     putstr("\n\r kup: "); */
/*     putB8(keyup); */
/*     putstr(" kdwn: "); */
/*     putB8(keydown); */
/*   } */


  if (~PINC & MACRO) { 		/* send to all orbs if macro down */
    start_addr = 0;
    end_addr = NUM_ORBS;
  }
  else {
    start_addr = addr;
    end_addr = addr+1;
  }

  if(keydown & TRIGR1) {      /* R1 trigger button down */
    if(~keys & TRIGL1) {      /* if mode, advance sound count */
      if(++happysoundcount >= NUM_HAPPYSOUNDS) {
	happysoundcount = 0;
      }
    }
    send_sound(start_addr,end_addr, happysoundcount, "h");
  }
  if(keydown & TRIGR2) {      /* R2 trigger button down */
    if(~keys & TRIGL1) {      /* if mode, advance sound count */
      if(++angrysoundcount >= NUM_HAPPYSOUNDS);
      angrysoundcount = 0;
    }
    send_sound(start_addr,end_addr, angrysoundcount, "a");
  }

  /* no response to L1 (MODE) */

  /* turn off lights and sounds with TL2 */
  if(keydown & TRIGL2) {      /* L2 trigger button down */
    if(~keys & TRIGL1) {      /* if mode, turn off lights */
      send_light_cmd(addr, XVAL, 'V', 0); 
      send_light_cmd(addr, XVAL, 'F', XVAL); 
    }
    else if (~PINC & MACRO){ 	/* if macro btn down, send song */
      if(songsoundcount & 0x01) /* if odd count, send all stop */
	send_song(0, NULL);
      else
	send_song((songsoundcount>>1), "song");
      if(++songsoundcount >= (NUM_SONGSOUNDS << 1)) {
	songsoundcount = 0;
      }

    }
    else { /* send stop sound command */
      send_sound(start_addr,end_addr, 0, NULL);
    }
  }

  if(keydown & JOYBL) {		/* the stop button (joyr) was pressed */
    send_addr(addr);
    putstr("$p00*}");
    send_addr(addr);
    putstr("$s00*}");
    send_addr(addr);
    putstr("[HALT 0]}");

  }
  if(keydown & JOYBR) {		/* Do identification flash */
    send_light_cmd(addr, XVAL, 'T', 50); 
    send_light_cmd(addr, XVAL, 'C', XVAL);
    /* send SPU command */
    send_addr(addr);
    putstr("[MODE "); 
    switch(~keys & (TRIGL1 | TRIGL2)){
    case TRIGL1: 
      putstr("1]}"); 
      break;
    case TRIGL2:
      putstr("2]}");
      break;
    case TRIGL1 + TRIGL2:
      putstr("3]}");
      break;
    default:
      putstr("0]}");
    }
  }
}


void do_joy_color(short val, short hue) {

  char dest = 0;
  hue = linearize(hue,20); 
  thishue += hue;

  if(thishue > 360) thishue -= 360;
  if(thishue < 0 ) thishue += 360;

  val = linearize(val,-15); 
  if(debug_out & 0){
    putstr("\n\r linval: ");
    putS16(val);
  }

  thisval += val;

  if((short)thisval >=254) 
    thisval = 254;
  if((short)thisval <=1) 
    thisval = 0;

  if (~PINC & MACRO) {
    for(dest=0;dest<NUM_ORBS;dest++){
      send_hue(dest, XVAL, thishue, (unsigned char)thisval);
    }
  }
  else {
    send_hue(addr, XVAL, thishue, (unsigned char)thisval);
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
  if (Timer0_ticks > ((~PINC&VIB_MAX)?100:250)) {	//  512 tics per second - heart-beat LED
    Timer0_reset();

    
    if (*state == 0) {
      setLED(STAT_LED,LED_ON);
      *state = 1;
      setLED(BATT_LED,LED_OFF); /* always turn off so we don't get stuck */

    }
    else {
      setLED(STAT_LED,LED_OFF);
      if(low_batt)
	setLED(BATT_LED,LED_ON);
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

