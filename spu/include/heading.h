// heading.h

void heading_Init(void);

void headingSetTargetDistance(double v);
double headingServoTask(double distance, int debugFileDescriptor)
short heading_Read_Position(void);

void heading_dump_data(void);
void heading_set_dead_band(short db);

void heading_set_Kp(short v);
void heading_set_Ki(short v);
void heading_set_Kd(short v);

void heading_set_integrator(short v);

void heading_set_min(short v);
void heading_set_max(short v);
void heading_set_accel(short v);

void Get_heading_Status(void);
void heading_Servo_Task(void);

void headingSetDebugOutput(double v);
