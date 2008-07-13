// encoder.h

#define MOTOR1_SHAFT_ENCODER	1
#define STEERING_ENCODER 2

void Encoder_Init(void);
void Encoder_Sample(void);
void Encoder_sample_speed(unsigned char channelNum);
unsigned short EncoderReadSpeed();
