// heading.h

void heading_Init(void);

void headingSetTargetDistance(double v);
double headingServoTask(double distance, int debugFileDescriptor)
short heading_Read_Position(void);

void headingSetDeadBand(double v)
void headingSetiLimit(double v)
void headingSetKp(double v)
void headingSetKi(double v)
void headingSetKd(double v)
void headingSetMin(double v)
void headingSetMax(double v)
void headingSetAccel(double v)
void headingSetDebugOutput(int v) {

void Get_heading_Status(void);

