

#include "../dispatcher/swarmdefines.h"

#define RADIUS 0.38
#define GRAVITY 9.81

#define PERIOD  0.10

#define STATE_SIZE         	14


/*  These are the components of the state vector   */

#define STATE_vdot             	1
#define STATE_v                	2
#define STATE_phidot           	3
#define STATE_phi              	4
#define STATE_theta            	5
#define STATE_psi              	6
#define STATE_x    		7
#define STATE_y    		8
#define STATE_xab    		9
#define STATE_yab   		10
#define STATE_zab    		11
#define STATE_xrb   		12
#define STATE_zrb   		13
#define STATE_yawb   		14

#define MEAS_SIZE         	11

/*  These are the components of the measurement vector   */

#define MEAS_xa           	1
#define MEAS_ya             2
#define MEAS_za           	3
#define MEAS_xr             4
#define MEAS_zr           	5
#define MEAS_xg             6
#define MEAS_yg    			7
#define MEAS_psig    		8
#define MEAS_vg    			9
#define MEAS_omega   		10
#define MEAS_yaw   			11

/*  Function Prototypes for kalmanSwarm */

void kalmanInitialBias( struct swarmGpsDataStruct * gpsData, struct swarmImuData * imuData,
						struct swarmStateEstimate * stateEstimate );

int kalmanInit( struct swarmStateEstimate * stateEstimate );

int kalmanProcess( struct swarmGpsDataStruct * gpsData, struct swarmImuData * imuData, struct swarmStateEstimate * stateEstimate);

void zeroStateEstimates( struct swarmStateEstimate * stateEstimate );

void initStateEstimates(struct swarmGpsDataStruct * gpsData, struct swarmImuData * imuData,
		struct swarmStateEstimate * stateEstimate );

