// ---------------------------------------------------------------------
// 
//	File: testharness.c
//      SWARM Orb SPU code http://www.orbswarm.com
//	
//      test harness code for LEMON-generated parser.
//      Takes stdin and tries to parse it, prints results to stdout
//      
//
//	Written by Jonathan Foote (Head Rotor at rotorbrain.com)
// -----------------------------------------------------------------------

#include <stdio.h>
#include <stdlib.h>
#include <ctype.h>  /* for isupper, etc. */
#include "scan.h"  /* created by lemon */
#include "scanner.h"

int parseDebug = 1;

/* Parser calls this when there is a complete MCU command */
void dispatchMCUCmd(int spuAddr, cmdStruct *c){
  printf("Orb %d Got MCU command: \"%s\"\n",spuAddr, c->cmd);
}

/* Parser calls this when there is a complete LED command */
void dispatchLEDCmd(int spuAddr, cmdStruct *c){
  printf("Orb %d Got LED command: \"%s\"\n",spuAddr, c->cmd);
}

/* Parser calls this when there is a complete SPU command */
void dispatchSPUCmd(int spuAddr, cmdStruct *c){
  printf("Orb %d Got SPU command: \"%s\"\n",spuAddr, c->cmd);
}


int main()
{
  void* pParser = ParseAlloc (malloc);
  int nextchar = 1;

  printf("At main\n");


  while (nextchar > 0) {

    nextchar = (int)getc(stdin);
    //printf("Got char \"%c\"\n",(char)nextchar);

    doScanner(pParser, nextchar);
    
  }    
  ParseFree(pParser, free );
  return(1);
}


