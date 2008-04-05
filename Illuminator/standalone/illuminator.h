// ---------------------------------------------------------------------
// 
//	File: illuminator.h
//	definition file for SWARM LED Illumination Unit http://www.orbswarm.com
//      which is a custom circuit board by rick L using an Atmel AVR atmega-8
//      code builds using WinAVR toolchain: see makefile
//
//	Written by Jonathan Foote (Head Rotor at rotorbrain.com)
// -----------------------------------------------------------------------

typedef struct {
  unsigned short H;		/* desired hue */
  unsigned char	V;		/* desired value (brightness) */
  unsigned char	S;		/* desired saturation */
  unsigned char	R;		/* current red */
  unsigned char	G;		/* current green */
  unsigned char	B;		/* current blue */
  unsigned char	bR;		/* blink red */
  unsigned char	bG;		/* blink green */
  unsigned char	bB;		/* blink blue */
  unsigned char	tR;		/* target red */
  unsigned char	tG;		/* target grn */
  unsigned char	tB;		/* target blue */
  char	rInc;		/* increment val for red */
  char	gInc;		/* increment val for green */
  char	bInc;		/* increment val for blue */
  unsigned int rCount;		/* wait this many tix before incrementing r */
  unsigned int gCount;
  unsigned int bCount;
  unsigned short Time;		/* fade or command duration time in ms */
  unsigned short Now;		/* how far we are into the command in ms */
  unsigned char Addr;		/* address of this illuminator board */
  unsigned char fading;		/* flag for when we are fading */
  unsigned char blink;	/* set with number of times to blink */
  unsigned char blinkCounter;	/* count of remaining ticks until we blink */
  unsigned char blinkToggle;
  unsigned char check;		/* check mode for debug */
} illuminatorStruct;

void doFade(illuminatorStruct *illum);
void hue2rgb(short inthue, unsigned char charval, unsigned char *red, unsigned char *grn, unsigned char *blu);
void writeAddressEEPROM(unsigned char address);
unsigned char readAddressEEPROM(void);

