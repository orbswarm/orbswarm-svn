// steering.h

void Steering_Init(void);

void Steering_Set_Target_Pos(short targetPos);
void Steering_do_Servo_Task(void);
short Steering_Read_Position(void);
