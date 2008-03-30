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

extern illuminatorStruct illum;
extern char debug_out;
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
    putstr("\r\n no L command\r\n ");
    return;
  }  

  /* is the next char an address digit? */
  c = commandStr[charPos];
  if(isdigit(c)) { 
    /* if not our address then they ain't talking to us */
    /* should use parseInt to get multi-byte addr, but assume 0-9 for now */
    if ((c - '0') != illum.Addr) {
      putstr("\r\nwrong address:  ");
      UART_send_byte(c);
      putstr(" != ");
      putS16( (short) illum.Addr );
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
    putstr("Got fade command \r\n");
    /* dispatch fade command here */
    doFade(&illum);
    break;


  // everything from here down is data parsing; nothing executable

  case 'R':	// set red value
    putstr("Got red: ");
    putS16( intData );
    putstr("\r\n");			
    illum.tR = (unsigned char) intData;
    break;

  case 'G':	// set raw green value
    putstr("Got grn: ");
    putS16( intData );
    putstr("\r\n");			
    illum.tG = (unsigned char) intData;
    break;

  case 'B':	// set raw blue value
    putstr("Got blue: ");
    putS16( intData );
    putstr("\r\n");			
    illum.tB = (unsigned char) intData;
    break;

  case 'H':	// set the hue
    putstr("Got hue: ");
    putS16( intData );
    putstr("\r\n");			
    illum.H = (unsigned char) intData;
    break;
    
  case 'S':	// set the hue
    putstr("Got sat: ");
    putS16( intData );
    putstr("\r\n");			
    illum.S = (unsigned char) intData;
    break;
    
  case 'V':	// set the hue
    putstr("Got val: ");
    putS16( intData );
    putstr("\r\n");			
    illum.V = (unsigned char) intData;
      break;
      
  case 'T':	// set the time
    putstr("Got time: ");
    putS16( intData );
    putstr("\r\n");			
    illum.Time = (unsigned short) intData;
    break;
    
  case 'A':	// set the address
    putstr("Got address: ");
    putS16( intData );
    putstr("\r\n");			
    illum.Addr = (unsigned char) intData;
    writeAddressEEPROM(illum.Addr);
    break;
    
  default: 			/* poorly formed command string, ignore */
    putstr("unrecog char:\n ");
    UART_send_byte(c);
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
 
