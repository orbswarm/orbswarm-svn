#!/usr/bin/python
# makegamma.py
# Python program to generate gamma lookup table to
# correct LED brightness from linear PWM.
# Written by Jonathan Foote (Head Rotor at rotorbrain.com) for
# the SWARM project http://www.orbswarm.com

# calculates a function of the form out = in^gamma, rounded to nearest int

# generates an output file "gamma.h"


from math import floor

# length of table
tablength = 255
# maximum value in table
tabmax = 255
# the actual gamma exponent
gamma = 1.8
# output file name
outfilename = "gamma.h"

# scale factor to get output between 0 and maxn
scale = float(tabmax)/pow(tablength,gamma)

#write table to this file
outfile = open(outfilename, "w")

# print header
outfile.write("/* gamma.h autogenerated by makeGamma.py */\n")
outfile.write("/* open source from  www.orbswarm.com */\n")
outfile.write("/* gamma for this table is %f */\n" % gamma)
outfile.write(""" /*
To use: 'python makegamma.py'
Then insert in your code

#include gamma.h  

unsigned char linPWMval, gammaPWMval;

(code)

gammaPWMval = gtab[linPWMval];

*/
""")
outfile.write("#define TABLENGTH %d\n" % tablength)
outfile.write("static unsigned char gtab[TABLENGTH] = { \n")

#generate exponential table
for i in range(tablength):
    outfile.write("%d, /* %d */\n" %
                  (int(floor(scale*pow(i,gamma) + 0.5)), i))

# print footer
outfile.write("%d}; /* %d */\n" % (tabmax,tablength))
print 'created gamma table file "%s" with %d entries' % (outfilename,i+1)
outfile.close()
