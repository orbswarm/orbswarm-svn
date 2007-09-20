 /* scanner for a toy Pascal-like language */

%option noyywrap 

%{
/* need this for the call to atof() below */
#include <math.h>
%}

%x spucommand

DIGIT [0-9]
STRING [a-z][a-z0-9]*


%%



"\{" BEGIN(spucommand);

<spucommand>[0-9][0-9]  printf( "A SPU addr: %s\n", yytext );
<spucommand>[ \t\n]+ /* eat up whitespace */
<spucommand>\$[a-z0-9]+\*"\}"  {
printf( "A MCU command payload %s\n", yytext );
BEGIN(INITIAL);
}
<spucommand><[a-z0-9]+>"\}"  {
printf( "A LED command payload %s\n", yytext );
BEGIN(INITIAL);
}



. printf( "Unrecognized character: %s\n", yytext );


%%


main( argc, argv )
int argc;
char **argv;
{
++argv, --argc; /* skip over program name */
if ( argc > 0 )
yyin = fopen( argv[0], "r" );
else
yyin = stdin;

yylex();
}
