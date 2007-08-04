#include <avr/io.h>
#include <ctype.h> 
#include "illuminator.h"  /* contains illuminatorStruct definition */
#include "parser.h"	
#include "putstr.h"	

extern illuminatorStruct illum;
unsigned char commandStr[MAX_COMMAND_LENGTH];
unsigned char commandLen = 0;


// --------------------------------------------------------------------
// parse the command string
// 

void parseCommand(){
  unsigned short intData=0; // holds numerical value of input data
  unsigned char charPos = 1;	// start with first char past the "<"
  
  /* if command does not start with L then they ain't talking to us */
  if(commandStr[charPos++] != 'L') {
    putstr("No L in cmd\r\n ");
    return;
  }  

  /* is the next char an address digit? */
  if(isdigit(commandStr[charPos++])) { 
    /* if not our address then they ain't talking to us */
    /* should use parseInt to get multi-byte addr, but assume 0-9 for now */
    if ((commandStr[charPos++] - '0') != illum.Addr) 
      putstr("wrong address\r\n ");
      return;
  }
  
  /* OK, they are talking to us: get the rest of the command */
  
  /* if there's a number in the command, it follows the next (command) byte*/
  /* grab it now*/
  if(isdigit(commandStr[charPos+1]))  // check for end of string 
    intData = parseInteger(charPos+1);
  else // short or malformed command
    intData=0;
  
  switch (commandStr[charPos]) {
    
  case 'F':	
    putstr("Got fade command \r\n");
    /* dispatch fade command here */
    //doFade(illum);
    break;

  case 'P': 
    putstr("Got pulse command \r\n");
    /* dispatch pulse command here */
    //doPulse(illum);
    break;

  case 'Z':
    putstr("Got sawtooth command \r\n");
    /* dispatch saw command here */
    //doSawtooth(illum);
    break;

  // everything from here down is data parsing; nothing executable
  case 'H':	// set the hue
    putstr("Got hue: ");
    putS16( intData );
    putstr("\r\n");			
    illum.tHue = (unsigned char) intData;
    break;
    
  case 'S':	// set the hue
    putstr("Got sat: ");
    putS16( intData );
    putstr("\r\n");			
    illum.tSat = (unsigned char) intData;
    break;
    
  case 'V':	// set the hue
    putstr("Got val: ");
    putS16( intData );
    putstr("\r\n");			
    illum.tVal = (unsigned char) intData;
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
    break;
    
  default: 			/* poorly formed command string, ignore */
    putstr("syntax error\n ");
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
 
