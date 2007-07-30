// steering.h

void Steering_Init(void);

void Steering_Set_Target_Pos(short targetPos);
void Steering_do_Servo_Task(void);
short Steering_Read_Position(void);

void Steering_dump_data(void);
void Steering_set_dead_band(short db);

void Steering_set_Kp(short v);
void Steering_set_Ki(short v);
void Steering_set_Kd(short v);

void Steering_set_min(short v);
void Steering_set_max(short v);
void Steering_set_accel(short v);

void Steering_save_PID_settings(void);
void Steering_read_PID_settings(void);
