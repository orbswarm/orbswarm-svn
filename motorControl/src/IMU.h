// IMU.h

#define IMU_Gyro_X_CHANNEL	2
#define IMU_Gyro_Y_CHANNEL	3
#define IMU_VREF_CHANNEL	4
#define IMU_Accel_Z_CHANNEL	5
#define IMU_Accel_Y_CHANNEL	6
#define IMU_Accel_X_CHANNEL	7

#define IMU_FIRST_CHANNEL	2
#define IMU_LAST_CHANNEL	7


void IMU_output_data_string(void);
void IMU_output_data(void);
