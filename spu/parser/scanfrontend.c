char mcu_cmd[BUFLENGTH];
char led_cmd[BUFLENGTH];
char spu_cmd[BUFLENGTH];
int spuAddr = 0;


/* add next char to command string. If start==1, then start new string */
void accumMCUCmd(int token, int start){
  static int cmd_len = 0;

  if (start)
    cmd_len = 0;
  
  if (cmd_len >= BUFLENGTH) {
    fprintf(stderr,"Buffer overflow in accumMCUCmd()\n");
    return;
  }
  mcu_cmd[cmd_len++] = (char)token;
  mcu_cmd[cmd_len] = '\0';
  //printf("MCU cmd: %s ",mcu_cmd);
}


/* we have a complete LED command so handle it */
void dispatchMCUCmd(){
  printf("Orb %d Got MCU command: \"%s\"\n",spuAddr, mcu_cmd);
}

/* add next char to command string. If start==1, then start new string */
void accumLEDCmd(int token, int start){
  static int cmd_len = 0;

  if (start){
    cmd_len = 0;
  }
  if (cmd_len >= BUFLENGTH) {
    fprintf(stderr,"Buffer overflow in accumLEDCmd()\n");
    return;
  }
  led_cmd[cmd_len++] = (char)token;
  led_cmd[cmd_len] = 0;
  //printf("LED cmd: %s ",led_cmd);
}

/* we have a complete LED command so handle it */
void dispatchLEDCmd(){
  printf("Orb %d Got LED command: \"%s\"\n",spuAddr, led_cmd);
}
/* add next char to command string. If start==1, then start new string */
void accumSPUCmd(int token, int start){
  static int cmd_len = 0;

  if (start){
    cmd_len = 0;
  }
  if (cmd_len >= BUFLENGTH) {
    fprintf(stderr,"Buffer overflow in accumSPUCmd()\n");
    return;
  }
  spu_cmd[cmd_len++] = (char)token;
  spu_cmd[cmd_len] = 0;
  //printf("SPU cmd: %s ",spu_cmd);
}

/* we have a complete SPU command so handle it */
void dispatchSPUCmd(){
  printf("Orb %d Got SPU command: \"%s\"\n",spuAddr, spu_cmd);
}

/* got the next digit in the spu address, 
   calculate ongoing numerical value */
void accumAddrDigit(int x) {
  spuAddr = 10*spuAddr + (x - '0');
  //printf("Got addr digit %c, addr=%d\n",(char)x,spuAddr); 
}
/* set spu address to zero before next command */
void resetAddress() {
  spuAddr = 0;
}

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
