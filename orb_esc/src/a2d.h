// a2d.h

#define CURRENT_SENSE_CHANNEL	0
#define STEERING_FEEDBACK_POT	1

void A2D_Init(void);
void A2D_poll_adc(void);
unsigned short A2D_read_channel(uint8_t chanNum);
