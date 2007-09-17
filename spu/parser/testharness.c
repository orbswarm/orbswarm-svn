
int main()
{
  void* pParser = ParseAlloc (malloc);
  int nextchar = 1;

  printf("At main\n");
  while (nextchar > 0) {

    nextchar = (int)getc(stdin);
    //printf("Got char \"%c\"\n",(char)nextchar);
    
    if(isdigit(nextchar)) {
      Parse (pParser, DIGIT, nextchar);
    }
    else if (islower(nextchar)  || isupper(nextchar)) {
      Parse (pParser, CHAR, nextchar);
    }
    else if (islower(nextchar)  || isupper(nextchar)) {
      Parse (pParser, CHAR, nextchar);
    }
    else {
      switch(nextchar) {
      case '{':
	Parse (pParser, SPU_START, nextchar);
	break;
      case '}':
	Parse (pParser, SPU_END, nextchar);
	Parse (pParser, 0, 0);
	break;
      case '$':
	Parse (pParser, MCU_START, nextchar);
	break;
      case '*':
	Parse (pParser, MCU_END, nextchar);
	break;
      default:
	break;
      }
    }
  }
  



  //ParseFree(pParser, free );

}
