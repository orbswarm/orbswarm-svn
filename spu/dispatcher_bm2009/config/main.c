#include<stdio.h>

int main( int argc, char *argv[] )
{
char * v_file  = "./Secret_Addresses";
char * keyword = "KURU";
char value[80] ;


	if( read_config_var(v_file, keyword, value) == 0 )
	{
		printf("\nValue for %s in %s is %s\n", keyword, v_file, value);
	}
	else
		printf("\nNo value for %s in %s\n", keyword, v_file);

return 0;
}
