#include <stdio.h>
#include <stdlib.h>
#include <ctype.h>  /* for isupper, etc. */

#include "scan.h"


/* prototype parser functions */
void *ParseAlloc(void *(*mallocProc)(size_t));
void ParseFree(
  void *p,                    /* The parser to be deleted */
  void (*freeProc)(void*)     /* Function used to reclaim memory */
  );
void Parse(
  void *yyp,                   /* The parser */
  int yymajor,                 /* The major token code number */
  int yyminor       /* The value for the token */
  );
void resetAddress(void);


/* we have a complete LED command so handle it */
void dispatchMCUCmd(int spuAddr, char *mcu_cmd){
  printf("Orb %d Got MCU command: \"%s\"\n",spuAddr, mcu_cmd);
}

/* we have a complete LED command so handle it */
void dispatchLEDCmd(int spuAddr, char *led_cmd){
  printf("Orb %d Got LED command: \"%s\"\n",spuAddr, led_cmd);
}
/* we have a complete SPU command so handle it */
void dispatchSPUCmd(int spuAddr, char *spu_cmd){
  printf("Orb %d Got SPU command: \"%s\"\n",spuAddr, spu_cmd);
}


/* this will eventually have a main() that calls routines from scanner.c */
int main()
{
  void* pParser = ParseAlloc (malloc);
  int nextchar = 1;

  printf("At main\n");
  while (nextchar > 0) {

    nextchar = (int)getc(stdin);
    //printf("Got char \"%c\"\n",(char)nextchar);
    

    /* lexical analyzer for parser */
    if (islower(nextchar)  || isupper(nextchar)) {
      Parse (pParser, CHAR, nextchar); /* get chars [a-zA-Z] */
    }
    else if(isdigit(nextchar)) { /* get digits [0-9] */
      Parse (pParser, DIGIT, nextchar);
    }
    else if (isblank(nextchar)) { /* get whitespace */
      Parse (pParser, WS, nextchar);
    }
    else { /* get special chars */
      switch(nextchar) {
      case '{':
	Parse (pParser, CMD_START, nextchar);
	break;
      case '}':
	Parse (pParser, CMD_END, nextchar);
	Parse (pParser, 0, 0);
	resetAddress();
	break;
      case '$':
	Parse (pParser, MCU_START, nextchar);
	break;
      case '*':
	Parse (pParser, MCU_END, nextchar);
	break;
      case '[':
	Parse (pParser, SPU_START, nextchar);
	break;
      case ']':
	Parse (pParser, SPU_END, nextchar);
	break;
      case '<':
	Parse (pParser, LED_START, nextchar);
	break;
      case '>':
	Parse (pParser, LED_END, nextchar);
	break;
      case '.':
	Parse (pParser, CHAR, nextchar);
	break;
	
      default:
	break;
      }
    }
  }

  ParseFree(pParser, free );
  return(1);
}

