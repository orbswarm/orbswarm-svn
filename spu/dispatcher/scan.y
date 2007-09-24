/* ---------------------------------------------------------------------
// 
//	File: scan.y
//      SWARM Orb SPU code http://www.orbswarm.com
//	
//      language description for LEMON-generated parser.
//      run LEMON on this, then concatenate with scanfrontend.c
//      to generate parser code. See http://www.hwaci.com/sw/lemon/
//
// Something like this: 
//	./lemon scan.y  # (makes scan.c)
//	cat scan.c scanfrontend.c  > scanner.c
//	$(CC) -c -o scanner.o $(CFLAGS) scanner.c
//       See Makefile.
//
//	Written by Jonathan Foote (Head Rotor at rotorbrain.com)
// ----------------------------------------------------------------------- */

%token_type {int}  
   
%include {   
#include <stdio.h>
#include <stdlib.h>
#include <ctype.h>   /* for isupper(), etc. */
#include <string.h>  /* for memset()  */
#include "scan.h"    /* generated by lemon */
#include "scanner.h" /* NOT generated by lemon: func protos */

/* these hold incoming command strings */
extern int parseDebug;

cmdStruct mcuCmd;
cmdStruct spuCmd;
cmdStruct ledCmd;
int spuAddr = 0;

}  
   
%syntax_error {  
  if(parseDebug)
    printf("Lemon syntax error\n");
}   

%parse_failure {
  if(parseDebug)
    printf("LEMON parser failure\n");
}
%stack_overflow {
  if(parseDebug)
    printf("LEMON parser stack overflow\n");
}

%right DIGIT.

expr ::= cmd_start spuAddr ws mcu_cmd CMD_END.   { 
  //printf("Got MCU command\n");
}
expr ::= cmd_start spuAddr ws spu_cmd CMD_END.   { 
  //printf("Got SPU command\n");
}
expr ::= cmd_start spuAddr ws led_cmd CMD_END.   { 
  //printf("Got led command\n");
}

/* ignore any whitespace between commands */
cmd_start ::= CMD_START.
cmd_start ::= ws CMD_START.

/* accumulate one or more whitespace chars */
ws ::= WS. 
ws ::= ws WS.

/* spu address is one or more digits. Accumulate'em */
spuAddr ::= DIGIT(A).          {accumAddrDigit(A); } 
spuAddr ::= spuAddr DIGIT(A).  {accumAddrDigit(A); } 

/* Motor control string is one or more chars and numerals */
mcu_str ::= MCU_START(A).      { accumCmd(&mcuCmd,A,1); } 
mcu_str ::= mcu_str CHAR(A) .  { accumCmd(&mcuCmd,A,0); }
mcu_str ::= mcu_str DIGIT(A) . { accumCmd(&mcuCmd,A,0); }
mcu_cmd ::= mcu_str MCU_END(A).{ 
  accumCmd(&mcuCmd,A,0);
  dispatchMCUCmd(spuAddr,&mcuCmd);
} 

/* Sound/LED control string is like mcu plus whitespace*/
led_str ::= LED_START(A).      { accumCmd(&ledCmd,A,1); } 
led_str ::= led_str CHAR(A).   { accumCmd(&ledCmd,A,0); } 
led_str ::= led_str DIGIT(A).  { accumCmd(&ledCmd,A,0); } 
led_str ::= led_str WS(A).     { accumCmd(&ledCmd,A,0); }  
led_cmd ::= led_str LED_END(A).   { 
  accumCmd(&ledCmd,A,0);
  dispatchLEDCmd(spuAddr,&ledCmd);
} 

/* SPU control str is like LED with different delimiter*/
spu_str ::= SPU_START(A).      { accumCmd(&spuCmd,A,1); } 
spu_str ::= spu_str CHAR(A).   { accumCmd(&spuCmd,A,0); } 
spu_str ::= spu_str DIGIT(A).  { accumCmd(&spuCmd,A,0); } 
spu_str ::= spu_str WS(A).     { accumCmd(&spuCmd,A,0); }  
spu_cmd ::= spu_str SPU_END(A).   { 
  accumCmd(&spuCmd,A,0);
  dispatchSPUCmd(spuAddr,&spuCmd);
} 



