// Illuminator struct for illuminator variables

typedef struct {
  unsigned char	Hue;		/* target hue */
  unsigned char	Val;		/* target value */
  unsigned char	Sat;		/* target saturation */
  unsigned short Time;		/* fade or command duration time in ms */
  unsigned char Addr;		/* address of this illuminator board */
} illuminatorStruct;

