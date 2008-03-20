// a2d.h

#define SPEED_CONTROL_CHANNEL	1
#define STEERING_CONTROL_CHANNEL	0


void A2D_Init(void);
void A2D_poll_adc(void);
short A2D_read_channel(uint8_t chanNum);
