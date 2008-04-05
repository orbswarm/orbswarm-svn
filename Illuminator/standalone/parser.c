// -----------------------------------------------------------------------
// 
//	File: parser.c
//	parser file for SWARM Orb LED Illumination Unit http://www.orbswarm.com
//      which is a custom circuit board by rick L using an Atmel AVR atmega-8
//      build code using WinAVR toolchain: see makefile
//
//	Written by Jonathan Foote (Head Rotor at rotorbrain.com)
//      based on original code by Petey the Programmer
// -----------------------------------------------------------------------

#include <avr/io.h>
#include <ctype.h> 
#include "illuminator.h"  /* contains illuminatorStruct definition */
#include "parser.h"	
#include "putstr.h"	
#include "UART.h"


void hue2rgb(short inthue, unsigned char charval, unsigned char *red, unsigned char *grn, unsigned char *blu);

extern illuminatorStruct illum;
extern char debug_out;

/* for indexed color */
extern unsigned char maxIndex;
extern unsigned char cir[];
extern unsigned char cig[];
extern unsigned char cib[];

unsigned char commandStr[MAX_COMMAND_LENGTH];
unsigned char commandLen = 0;


// --------------------------------------------------------------------
// parse the command string
// 

void parseCommand(){
  unsigned short intData=0;     /* holds numerical value of parsed data */
  unsigned char charPos=1;	/* start with first char past the "<" */
  unsigned char c;		/* next char to parse */

  /* if command does not start with L then they ain't talking to us */
  if(commandStr[charPos++] != 'L') {
    if(debug_out) putstr("\r\n no L command");
    return;
  }  

  /* is the next char an address digit? */
  c = commandStr[charPos];
  if(isdigit(c)) { 
    /* if not our address then they ain't talking to us */
    /* should use parseInt to get multi-byte addr, but assume 0-9 for now */
    if ((c - '0') != illum.Addr) {
      if(debug_out) {
	putstr("\r\nwrong address:  ");
	UART_send_byte(c);
	putstr(" != ");
	putS16( (short) illum.Addr );
      }
      return; 			/* skip rest of command */
    }
    // it was a digit, so move ahead
    charPos++;
  } 
  
  /* OK, they are talking to us: get the rest of the command */
  /* if there's a number in the command, it follows the next (command) byte*/
  /* grab it now*/
  if(isdigit(commandStr[charPos+1]))  // check for end of string 
    intData = parseInteger(charPos+1);
  else // short or malformed command
    intData=0;
  
  c = commandStr[charPos];
  switch (c) {

  case 'F':	
    if(debug_out) putstr("\r\nGot fade command");
    /* dispatch fade command here */
    doFade(&illum);
    break;


  // everything from here down is data parsing; nothing executable

  case 'R':	// set red value
    illum.tR = (unsigned char) intData;
    if(debug_out) {
      putstr("\r\nGot red: ");
      putS16( (short) illum.tR );
    }
    break;

  case 'G':	// set raw green value
    if(debug_out) {
      putstr("\r\nGot grn: ");
      putS16( intData );
    }
    illum.tG = (unsigned char) intData;
    break;

  case 'B':	// set raw blue value
    if(debug_out) {
      putstr("\r\nGot blue: ");
      putS16( intData );
    }
    illum.tB = (unsigned char) intData;
    break;

  case 'H':	// set the hue
    if(debug_out) {
      putstr("\r\nGot hue: ");
      putS16( intData );
    }

    illum.H = (short) intData;
    hue2rgb((short)illum.H, (unsigned char) illum.V, &(illum.tR), &(illum.tG),&(illum.tB));
    break;
    
  case 'V':	// set the value
    if(debug_out) {
      putstr("\r\nGot val: ");
      putS16( intData );
    }		
    illum.V = (unsigned char) intData;
    hue2rgb((short)illum.H, (unsigned char) illum.V, &(illum.tR), &(illum.tG),&(illum.tB));
    break;
      
  case 'T':	// set the time
    if(debug_out) {
      putstr("\r\nGot time: ");
      putS16( intData );
    }
    illum.Time = (unsigned short) intData;
    break;
    
  case 'C':	//  the check mode
    if(debug_out){
      putstr("\r\nCheck mode: ");
      putS16( intData );
      putstr("\r\nAddr: ");
      putS16( (short) illum.Addr );
    }
    /* do check (self-test) mode: blink address number of times */
    illum.check = intData;
    break;
    

  case 'K':	//  the blink mode: blink this many times
    if(debug_out){
      putstr("\r\nBlink val: ");
      putS16( intData );
    }
    illum.blink = intData;
    illum.blinkCounter = intData;
    break;

  case 'P':	//  the blink period
    if(debug_out){
      putstr("\r\nBlink val: ");
      putS16( intData );
    }
    /* do check (self-test) mode: blink address number of times */
    illum.blink = intData;
    illum.blinkCounter = intData;
    break;

  case 'W':	//  write alternate color for blink mode
    if(debug_out){
      putstr("\r\nW alt val: ");
      putS16( intData );
    }
    /* do check (self-test) mode: blink address number of times */
    illum.bR = illum.tR;
    illum.bG = illum.tG;
    illum.bB = illum.tB;
    break;

  case 'X':	//  write color to index 
    if(debug_out){
      putstr("\r\nwrite color index ");
      putS16( intData );
    }
    /* do check (self-test) mode: blink address number of times */
    if(intData > maxIndex) intData = maxIndex;
    if(intData < 0) intData = 0;
    cir[intData] = illum.tR;
    cig[intData] = illum.tG;
    cib[intData] = illum.tB;
    break;
    
  case 'I':	//  read color from index
    if(debug_out){
      putstr("\r\reading index: ");
      putS16( intData );
    }
    /* do check (self-test) mode: blink address number of times */
    if(intData > maxIndex) intData = maxIndex;
    if(intData < 0) intData = 0;
    illum.tR = cir[intData]; 
    illum.tG = cig[intData]; 
    illum.tB = cib[intData]; 
    break;
    

  case 'A':	// set the address
  case 'a':	// set the address
    if(debug_out) {
      putstr("\r\nGot address: ");
      putS16( intData );
    }
    illum.Addr = (unsigned char) intData;
    writeAddressEEPROM(illum.Addr);
    break;
    
  default: 			/* poorly formed command string, ignore */
    if(debug_out){
      putstr("\r\nunrecog command char:\n ");
      UART_send_byte(c);
    }
    break;
  }
}


// parse the command string  at the given position
// convert Ascii signed number to short word. (16-Bit)

unsigned short parseInteger(unsigned char startChr)
{
  unsigned short accum = 0; // accumulate the integer data
  //unsigned char sign = 0;
  unsigned char cPos = startChr;
  
  /* if you want negative values (better change var to signed too) */
  //if (commandStr[startChr] == '-') {
  //  sign = 1;
  //  cPos++;
  //}
  
  /* while we see digits, convert them to integer */
  /* should probably check for integer overflow   */
  /* (string end is handled by isdigit('\0')=0) */
  while (isdigit(commandStr[cPos])) {
    accum = (accum * 10) + (commandStr[cPos++] - '0');
  } 
  
  //if (sign)
  //  accum = -accum;

  return accum;
}

// --------------------------------------------------------------------------
// process incoming chars - commands start with '<' and end with '>'
// return 1 if command string is complete - else return zero
 
unsigned char accumulateCommandString(unsigned char c){
  /* catch beginning of this string */
  if (c == '<') { // this will catch re-starts and stalls as well as valid commands.
     commandLen = 0;
     commandStr[commandLen++] = c;
     return 0;
  }
  
  if (commandLen != 0){	 // string in progress, accumulate next char
    
    if (commandLen < MAX_COMMAND_LENGTH) 
      commandStr[commandLen++] = c;
    return (c == '>');
  }
  return 0;
}
 
