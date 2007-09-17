/* 

  Simple lemon parser  example.

  
    $ ./lemon example1.y                          

  The above statement will create example1.c.


*/

%token_type {int}  
   
   
%include {   
#include <stdio.h>
#include <stdlib.h>
#include "test.h"
}  
   
%syntax_error {  
  printf("Syntax error!\n");
}   

%right DIGIT.

program ::= expr(A).   { printf("Done %c\n",(char)A); }  
expr ::= SPU_START number(B) SPU_END.   { 
  printf("Got SPU command %d\n",B);
}
expr ::= SPU_START number(B) mcu_cmd SPU_END.   { 
  printf("Got MCU command %d\n",B);
}
expr ::= SPU_START number(B) chars SPU_END.   { 
  printf("Got chars command %d\n",B);
}
number ::= DIGIT(A). { 
  printf("Got first digit %c\n",(char)A); 
} 
number ::= DIGIT(A) number. { 
  printf("Got additional digit %c\n",(char)A); 
} 
chars ::= CHAR(A). { 
  printf("Got first char %c\n",(char)A); 
} 
chars ::= chars CHAR(A). { 
  printf("Got additional char %c\n",(char)A); 
} 
chars ::= chars DIGIT(A). { 
  printf("Got additional char %c\n",(char)A); 
} 
mcu_cmd ::= MCU_START  chars MCU_END. { 
  printf("Got MCU cmd \n"); 
} 
