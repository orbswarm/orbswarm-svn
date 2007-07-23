// motor.h

#define FORWARD 1
#define REVERSE -1

void Motor_PWM_Init(void);

void Set_Motor1_PWM(unsigned char pwm, signed char direction);
void Set_Motor2_PWM(unsigned char pwm, signed char direction);

void Set_Drive_Speed(short t);

void Drive_set_integrator(short s);
void Drive_set_min(unsigned char c);
void Drive_set_max(unsigned char c);
void Drive_set_dead_band(short s);
void Drive_set_integrator(short s);
void Drive_set_intLimit(short s);
void Drive_set_Kp(char c);
void Drive_set_Ki(char c);
void Drive_set_Kd(char c);


void Motor_dump_data(void);
void Motor_save_PID_settings(void);
void Motor_read_PID_settings(void);

void Drive_Servo_Task(void);
void Get_Drive_Status(void);

short limit( short *v, short minVal, short maxVal);

