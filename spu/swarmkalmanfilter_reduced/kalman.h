/*  kalman.h
    This is the include file for the kalman filtering functions defined
    in kalman.c.

*/

#undef PRINT_DEBUG

#include "matmath.h"    /*  uFloat decl. and matrix utilities */



/***********   Extended Kalman Filtering   **************/

extern void extended_kalman_init( uFloat **P, uFloat *x );

extern void extended_kalman_step( uFloat *z_in );

extern uFloat *kalman_get_state( void );  


/*********    Application Specific Functions  ***********/

extern void generate_measurement_transfer( uFloat *state, uFloat **H );

extern void apply_measurement( uFloat *newState, uFloat *est_measurement );
extern void systemF( uFloat *stateDot, uFloat *state );
extern void systemJacobian( uFloat *state, uFloat **jacobian );

extern void covarianceSet( uFloat **GQGt, uFloat **R );

/*  and a select set of global variables to communicate with them.  */

