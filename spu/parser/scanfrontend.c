
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


