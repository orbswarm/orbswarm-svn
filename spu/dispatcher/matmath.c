/*  matmath.c

    This is a roughly self-contained code module for matrix
    math.

    This code comes from a mixture of sources, and at this time, may not have clear rights.
    Redistribute at your own risk.

*/

#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include "matmath.h"

#define NR_END 1
#define FREE_ARG char*

/**********************************************************

     Vector Math Functions

*/

void vecCopy( uFloat *src, uFloat *dst, int n )
{
  int  i;

  for( i = 1; i <= n; i++ )
    dst[ i ] = src[ i ];
}

void vecAdd( uFloat *a, uFloat *b, uFloat *c, int n )
{
  int i;
  uFloat  *a_ptr, *b_ptr, *c_ptr;

  a_ptr = a + 1;
  b_ptr = b + 1;
  c_ptr = c + 1;

  for( i = 1; i <= n; i++)
    *c_ptr++ = *a_ptr++ + *b_ptr++;
}

/* vecSub()
   This function computes C = A - B, for vectors of size n  */

void vecSub( uFloat *a, uFloat *b, uFloat *c, int n )
{
  int i;
  uFloat  *a_ptr, *b_ptr, *c_ptr;

  a_ptr = a + 1;
  b_ptr = b + 1;
  c_ptr = c + 1;

  for( i = 1; i <= n; i++)
    *c_ptr++ = *a_ptr++ - *b_ptr++;
}

/*  vector C = vector A * scalar B , for A of size n   */
extern void vecScalarMult( uFloat *a, uFloat b, uFloat *c, int n )
{
  int i;
  uFloat  *a_ptr, *c_ptr;

  a_ptr = a + 1;
  c_ptr = c + 1;

  for( i = 1; i <= n; i++)
    *c_ptr++ = *a_ptr++ * b;
}

/**********************************************************

  Matrix math functions

*/

void matAdd( uFloat **a, uFloat **b, uFloat **c, int m, int n )
{
  int i,j;
  uFloat  *a_ptr, *b_ptr, *c_ptr;

  for( j = 1; j <= m; j++)
    {
      a_ptr = &a[j][1];
      b_ptr = &b[j][1];
      c_ptr = &c[j][1];

      for( i = 1; i <= n; i++)
	*c_ptr++ = *a_ptr++ + *b_ptr++;
    }
}


/* matSub()
   This function computes C = A - B, for matrices of size m x n  */

void matSub( uFloat **a, uFloat **b, uFloat **c, int m, int n )
{
  int i,j;
  uFloat  *a_ptr, *b_ptr, *c_ptr;

  for( j = 1; j <= m; j++)
    {
      a_ptr = &a[j][1];
      b_ptr = &b[j][1];
      c_ptr = &c[j][1];

      for( i = 1; i <= n; i++)
	*c_ptr++ = *a_ptr++ - *b_ptr++;
    }
}

/*  matMult
    This function performs a matrix multiplication.
*/
void matMult( uFloat **a, uFloat **b, uFloat **c,
	      int a_rows, int a_cols, int b_cols )
{
  int i, j, k;
  uFloat  *a_ptr;
  uFloat  temp;

  for( i = 1; i <= a_rows; i++)
    {
      a_ptr = &a[i][0];
      for( j = 1; j <= b_cols; j++ )
	{
	  temp = 0.0;
	  for( k = 1; k <= a_cols; k++ )
	  {
	    if ((a_ptr[k] != 0.0) && (b[k][j] != 0.0))
	    	temp = temp + (a_ptr[k] * b[k][j]);
	  }
	  c[i][j] = temp;
	}
    }
}


/*  matMultVector
    This function performs a matrix x vector multiplication.
*/
void matMultVector( uFloat **a, uFloat *b, uFloat *c,
	      int a_rows, int a_cols )
{
  int i, k;
  uFloat  *a_ptr, *b_ptr;
  uFloat  temp;

  for( i = 1; i <= a_rows; i++)
    {
      a_ptr = &a[i][0];
      b_ptr = &b[1];
      temp = 0.0;

      for( k = 1; k <= a_cols; k++ )
      {
        if ((a_ptr[ k ] != 0.0) && (*b_ptr != 0.0))
	   temp += a_ptr[ k ] * *b_ptr++;
	else
           b_ptr++;
      }
      c[i] = temp;
    }
}

/*  matMultScalar
    This function performs a matrix multiplication.
*/
void matMultScalar( uFloat **a, uFloat b, uFloat **c,
	      int a_rows, int a_cols )
{
  int i, j;



  for( i = 1; i <= a_rows; i++)
    {
      for( j = 1; j <= a_cols; j++ )
	{
	  c[i][j] = a[i][j] * b;
	}
    }
}



/*  matMultTranspose
    This function performs a matrix multiplication of A x transpose B.

Is this right?  it's implemented differently than matMult... - MAP
*/
void matMultTranspose( uFloat **a, uFloat **b, uFloat **c,
	      int a_rows, int a_cols, int b_cols )
{
  int i, j, k;
  uFloat  *a_ptr, *b_ptr;
  uFloat  temp;

  for( i = 1; i <= a_rows; i++)
    {
      a_ptr = &a[i][0];
      for( j = 1; j <= b_cols; j++ )
	{
	  b_ptr = &b[j][1];

	  temp = (uFloat)0;

	  for( k = 1; k <= a_cols; k++ )
	  {
	    if ((a_ptr[k] != 0.0) && (*b_ptr != 0.0))
	    	temp += a_ptr[ k ] * *b_ptr++;
	    else b_ptr++;
	  }
	  c[i][j] = temp;
	}
    }
}

/*  matTransposeMult
    This function performs a matrix multiplication of transpose A x B.
    a_rows refers to the transposed A, is a_cols in actual A storage
    a_cols is same, is a_rows in actual A storage
*/
void matTransposeMult( uFloat **A, uFloat **B, uFloat **C,
	      int a_rows, int a_cols, int b_cols )
{
  int i, j, k;
  uFloat  temp;

  for( i = 1; i <= a_rows; i++)
    {
      for( j = 1; j <= b_cols; j++ )
	{
	  temp = 0.0;

	  for( k = 1; k <= a_cols; k++ )
	    temp += A[ k ][ i ] * B[ k ][ j ];

	  C[ i ][ j ] = temp;
	}
    }
}





void matCopy( uFloat **src, uFloat **dst, int num_rows, int num_cols )
{
  int  i, j;

  for( i = 1; i <= num_rows; i++ )
    for( j = 1; j <= num_cols; j++ )
      dst[ i ][ j ] = src[ i ][ j ];
}


/* gjInverse()
   This function performs gauss-jordan elimination to solve a set
   of linear equations, at the same time generating the inverse of
   the A matrix.

*/

#define SWAP(a,b) {temp=(a);(a)=(b);(b)=temp;}

void gjInverse(uFloat **a, int n, uFloat **b, int m)
{
  int *indxc,*indxr,*ipiv;
  int i,icol=0,irow=0,j,k,l,ll;
  uFloat big,dum,pivinv,temp;

  indxc=ivector(1,n);
  indxr=ivector(1,n);
  ipiv=ivector(1,n);
  for (j=1;j<=n;j++) ipiv[j]=0;
  for (i=1;i<=n;i++) {
    big=0.0;
    for (j=1;j<=n;j++)
      if (ipiv[j] != 1)
	for (k=1;k<=n;k++) {
	  if (ipiv[k] == 0) {
	    if (fabs(a[j][k]) >= big) {
	      big=fabs(a[j][k]);
	      irow=j;
	      icol=k;
	    }
	  } else if (ipiv[k] > 1) fprintf(stderr,"gjInverse: Singular Matrix\n");
	}
    ++(ipiv[icol]);
    if (irow != icol) {
      for (l=1;l<=n;l++) SWAP(a[irow][l],a[icol][l])
	for (l=1;l<=m;l++) SWAP(b[irow][l],b[icol][l])
    }
    indxr[i]=irow;
    indxc[i]=icol;
    if (a[icol][icol] == 0.0) fprintf(stderr,"gjInverse: Singular Matrix\n");
    pivinv=1.0/a[icol][icol];
    a[icol][icol]=1.0;
    for (l=1;l<=n;l++)
	if (a[icol][l] != 0)
		a[icol][l] *= pivinv;
    for (l=1;l<=m;l++)
	if (b[icol][l] != 0)
		b[icol][l] *= pivinv;
    for (ll=1;ll<=n;ll++)
      if (ll != icol) {
	dum=a[ll][icol];
	a[ll][icol]=0.0;
	for (l=1;l<=n;l++) a[ll][l] -= a[icol][l]*dum;
	for (l=1;l<=m;l++) b[ll][l] -= b[icol][l]*dum;
      }
  }
  for (l=n;l>=1;l--) {
    if (indxr[l] != indxc[l])
      for (k=1;k<=n;k++)
	SWAP(a[k][indxr[l]],a[k][indxc[l]]);
  }

  freeIVector(ipiv,1,n);
  freeIVector(indxr,1,n);
  freeIVector(indxc,1,n);
}
#undef SWAP



/**********************************************************

  Vector and Matrix allocation and deallocation routines

*/

uFloat *vector(long nl, long nh)
/* allocate a vector with subscript range v[nl..nh] */
{
	uFloat *v;

	v=(uFloat *)malloc((size_t) ((nh-nl+1+NR_END) *
					   sizeof(uFloat)));
	if (!v) fprintf(stderr,"Allocation failure in vector()\n");
	return v-nl+NR_END;
}

int *ivector(long nl, long nh)
/* allocate an int vector with subscript range v[nl..nh] */
{
	int *v;

	v=(int *)malloc((size_t) ((nh-nl+1+NR_END)*sizeof(int)));
	if (!v) fprintf(stderr,"Allocation failure in ivector()\n");
	return v-nl+NR_END;
}

uFloat **matrix(long nrl, long nrh, long ncl, long nch)
/* allocate a uFloat matrix with subscript range m[nrl..nrh][ncl..nch] */
{
	long i, nrow=nrh-nrl+1,ncol=nch-ncl+1;
	uFloat **m;

	/* allocate pointers to rows */
	m=(uFloat **)malloc((size_t)((nrow+NR_END)
					    *sizeof(uFloat *)));
	if (!m) fprintf(stderr,"Allocation failure in matrix()\n");
	m += NR_END;
	m -= nrl;

	/* allocate rows and set pointers to them */
	m[nrl]=(uFloat *)malloc((size_t)((nrow*ncol+NR_END)
					       *sizeof(uFloat)));
	if (!m[nrl]) fprintf(stderr,"Allocation failure in matrix()\n");
	m[nrl] += NR_END;
	m[nrl] -= ncl;

	for(i=nrl+1;i<=nrh;i++) m[i]=m[i-1]+ncol;

	/* return pointer to array of pointers to rows */
	return m;
}

void freeVector(uFloat *v, long nl, long nh)
/* free a vector allocated with vector() */
{
	free((FREE_ARG) (v+nl-NR_END));
}

void freeIVector(int *v, long nl, long nh)
/* free an int vector allocated with ivector() */
{
	free((FREE_ARG) (v+nl-NR_END));
}


void freeMatrix(uFloat  **m, long nrl, long nrh, long ncl, long nch)
/* free a matrix allocated by matrix() */
{
	free((FREE_ARG) (m[nrl]+ncl-NR_END));
	free((FREE_ARG) (m+nrl-NR_END));
}





/***************************************************

  Matrix and Vector Printing routines

*/


void printVector( char *str, uFloat *x, int n )
{
  int     i;

  printf( "%s:\n", str );
  for( i = 1; i <= n; i++ )
    {
      if( (x[ i ] > 1) && (x[i] < 999) )
	printf( " %3.1lf", x[ i ] );
      else
	printf( " %2.2lg", x[ i ] );
    }
  printf( "\n" );
}


void printMatrix( char *str, uFloat **A, int m, int n )
{
  int     i, j;

  printf( "%s:  (%d x %d)\n", str, m, n );
  for( i = 1; i <= m; i++ )
    {
      for( j = 1; j <= n; j++ )
	{
	  printf( " %3.3lg", A[ i ][ j ] );
	}
      printf( ";\n" );
    }
}
