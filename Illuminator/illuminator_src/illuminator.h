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
  unsigned char	H;		/* current hue */
  unsigned char	S;		/* current saturation */
  unsigned char	V;		/* current value */
  unsigned char	R;		/* current red */
  unsigned char	G;		/* current green */
  unsigned char	B;		/* current blue */
  unsigned char	tR;		/* target hue */
  unsigned char	tG;		/* target value */
  unsigned char	tB;		/* target saturation */
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
  unsigned char HSVset;		/* flag that hue, sat or value has been set */
} illuminatorStruct;

void doFade(illuminatorStruct *illum);
void writeAddressEEPROM(illuminatorStruct *illum);
void readAddressEEPROM(illuminatorStruct *illum);
