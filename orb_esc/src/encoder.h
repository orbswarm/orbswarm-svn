// encoder.h

#define MOTOR1_SHAFT_ENCODER	1
#define STEERING_ENCODER 2

void Encoder_Init(void);
unsigned short Encoder_read_count(unsigned char channelNum);
void Encoder_sample_speed(unsigned char channelNum);
unsigned short Encoder_read_speed(unsigned char channelNum);
