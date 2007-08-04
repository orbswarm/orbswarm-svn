// Illuminator struct for illuminator variables

typedef struct {
  unsigned char	H;		/* current hue */
  unsigned char	S;		/* current saturation */
  unsigned char	V;		/* current value */
  unsigned char	tHue;		/* target hue */
  unsigned char	tVal;		/* target value */
  unsigned char	tSat;		/* target saturation */
  unsigned short Time;		/* fade or command duration time in ms */
  unsigned short Now;		/* how far we are into the command in ms */
  unsigned char Addr;		/* address of this illuminator board */
} illuminatorStruct;

