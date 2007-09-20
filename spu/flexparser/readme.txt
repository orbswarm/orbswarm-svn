# flexparser/readme.txt by Jonathan Foote (Head Rotor at rotorbrain.com)
# for the SWARM project www.orbswarm.com September 2007

This directory contains source files for another attempt at the SPU parser. This parser
takes incoming commands for each orb robot and parses them into various actions.(HOPEFULLY!)

RTFM here: http://www.delorie.com/gnu/docs/flex/flex.html

This is a very crude first attempt at making a parser from flex.


Read this: 

Files:

test.l : the parser generator source. Run flex on it to get lex.yy.c. See the makefile. 


Compile lex.yy.c to get test executable. Run that; test it by typing or piping input into it (it reads from stdin)



testinput: a text file with some test input. "cat testinput | test" to test


