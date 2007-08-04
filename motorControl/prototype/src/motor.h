// motor.h

#define FORWARD 1
#define REVERSE -1

void Motor_PWM_Init(void);

void Set_Motor1_PWM(unsigned char pwm, signed char direction);
void Set_Motor2_PWM(unsigned char pwm, signed char direction);

void Set_Motor1_Power(unsigned char power, signed char direction);
void Set_Motor1_Torque(unsigned char t, signed char direction);

char Motor_Read_Drive_Direction(void);
char Motor_Read_Drive_PWM(void);

void Motor_do_motor_control(char use_iTerm_PID);
void Motor_set_PID_feedback(char c);

void Motor_set_Kp(char c);
void Motor_set_Ki(char c);
void Motor_set_Kd(char c);

void Motor_dump_data(void);
void Motor_save_PID_settings(uint8_t iTermFlag);
void Motor_read_PID_settings(uint8_t *iTermFlag);
