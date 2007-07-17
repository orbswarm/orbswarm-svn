// motor.h

#define FORWARD 1
#define REVERSE -1

void Motor_PWM_Init(void);

void Set_Motor1_PWM(unsigned char pwm, signed char direction);
void Set_Motor2_PWM(unsigned char pwm, signed char direction);

void Set_Motor1_Power(unsigned char power, signed char direction);
void Set_Drive_Speed(unsigned char t, signed char direction);

//char Motor_Read_Drive_Direction(void);
//char Motor_Read_Drive_PWM(void);

void Set_Drive_Deadband(short s);
void Motor_set_Kp(char c);
void Motor_set_Ki(char c);
void Motor_set_Kd(char c);

void Motor_dump_data(void);
void Motor_save_PID_settings(void);
void Motor_read_PID_settings(void);

void Drive_Servo_Task(void);


void Get_Drive_Status(void);
short limit( short *v, short minVal, short maxVal);

