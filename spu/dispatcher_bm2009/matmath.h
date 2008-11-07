#ifndef __MATMATH_H__
#define __MATMATH_H__
/*   matmath.h

     This is the declaration file for the matmath functions.

    This code comes from a mixture of sources, and at this time, may not have clear rights.
    Redistribute at your own risk.

*/

/*  This is the datatype used for the math and non-type specific ops.  */

typedef double uFloat;

/*************     Vector math routines    ****************/

/*  vector B = vector A, of size n   */
extern void vecCopy( uFloat *src, uFloat *dst, int n );

/*  vector C = vector A + vector B , both of size n   */
extern void vecAdd( uFloat *A, uFloat *B, uFloat *C, int n );

/*  vector C = vector A - vector B , both of size n   */
extern void vecSub( uFloat *A, uFloat *B, uFloat *C, int n );

/*  vector C = vector A * scalar B , for A of size n   */
extern void vecScalarMult( uFloat *A, uFloat B, uFloat *C, int n );

extern void printVector( char *str, uFloat *x, int n );

/*************   Some matrix math routines  **************/

/*  matrix B = matrix A, of size num_rows x num_cols   */
extern void matCopy( uFloat **A, uFloat **B, int num_rows,
			int num_cols );

/*  matrix C = matrix A + matrix B , both of size m x n   */
extern void matAdd( uFloat **A, uFloat **B, uFloat **C, int m, int n );

/*  matrix C = matrix A - matrix B , all of size m x n  */
extern void matSub( uFloat **A, uFloat **B, uFloat **C, int m, int n );

/*  matrix C = matrix A x matrix B , A(a_rows x a_cols), B(a_cols x b_cols) */
extern void matMult( uFloat **A, uFloat **B, uFloat **C,
		     int a_rows, int a_cols, int b_cols );

/*  matrix C = matrix A x vector B , A(a_rows x a_cols), B(a_cols x 1) */
extern void matMultVector( uFloat **A, uFloat *B, uFloat *C,
			    int a_rows, int a_cols );

/*  C = matrix A x trans( matrix B ), A(a_rows x a_cols), B(b_cols x a_cols) */
extern void matMultTranspose( uFloat **A, uFloat **B, uFloat **C,
			   int a_rows, int a_cols, int b_cols );

/*  matrix C = matrix A x scalar B , A(a_rows x a_cols) */
void matMultScalar( uFloat **a, uFloat b, uFloat **c,
	      int a_rows, int a_cols );


/*  C = trans( matrix A ) x matrix B, A(a_cols x a_rows), B(a_cols x b_cols) */
extern void matTransposeMult( uFloat **A, uFloat **B, uFloat **C,
			   int a_rows, int a_cols, int b_cols );


extern void printMatrix( char *str, uFloat **A, int m, int n );


extern void gjInverse( uFloat **A, int n, uFloat **B, int m );


/*   Vector allocation routines   */

extern uFloat *vector(long nl, long nh);

extern int *ivector(long nl, long nh);

/*   Matrix allocation routines    */

extern uFloat **matrix(long nrl, long nrh, long ncl, long nch);


/*   Deallocation routines   */

extern void freeVector(uFloat *v, long nl, long nh);

extern void freeIVector(int *v, long nl, long nh);

extern void freeMatrix(uFloat **m, long nrl, long nrh, long ncl, long nch);

#endif
