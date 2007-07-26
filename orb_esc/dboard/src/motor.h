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
void Drive_set_current(short s);
void Drive_set_Kp(char c);
void Drive_set_Ki(char c);
void Drive_set_Kd(char c);


void Motor_dump_data(void);
void Motor_save_PID_settings(void);
void Motor_read_PID_settings(void);

void Drive_Servo_Task(void);
void Get_Drive_Status(void);

short limit( short *v, short minVal, short maxVal);

// ----------------------------------------------------------------------
// Motor Control Block for keeping track of Motor PID function variables

typedef struct {
  char	Kp;			/* proportional PID term */
  char	Ki;			/* integral PID term */
  char	Kd;			/* derivative PID term */
  short targetSpeed;		/* the speed we want to go */
  short currentSpeed;		/* the speed we are going now */
  char currentDirection;	/* the direction we are going now */
  unsigned char	currentPWM;	/* the pwer we are giving it now */
  short	dead_band;	/* close enough to desired speed if within */
  short lastSpeedError;		/* the error we had last iteration */
  unsigned char lastPWM;	/* the power we gave it last iteration */
  unsigned char deltaAccel;	/* biggest change in accel per inter */
  short intLimit;  		/* integrator limit */
  unsigned char minPWM;
  unsigned char maxPWM;
  short maxCurrent;		/* current where we start limiting power */
} motor_control_block;
