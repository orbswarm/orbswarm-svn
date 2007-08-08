/*  kalman.h
    This is the include file for the kalman filtering functions defined
    in kalman.c.

    J. Watlington, 11/16/95
*/

#undef PRINT_DEBUG

#include "matmath.h"    /*  m_elem decl. and matrix utilities */



/***********   Extended Kalman Filtering   **************/

extern void extended_kalman_init( m_elem **P, m_elem *x );

extern void extended_kalman_step( m_elem *z_in );

extern m_elem *kalman_get_state( void );  


/*********    Application Specific Functions  ***********/

extern void generate_measurement_transfer( m_elem *state, m_elem **H );

extern void apply_measurement( m_elem *newState, m_elem *est_measurement );
extern void systemF( m_elem *stateDot, m_elem *state );
extern void systemJacobian( m_elem *state, m_elem **jacobian );

extern void covarianceSet( m_elem **GQGt, m_elem **R );

/*  and a select set of global variables to communicate with them.  */

extern int     state_size;
extern int     measurement_size;
extern int     global_step;  /* the current step number (k) */
