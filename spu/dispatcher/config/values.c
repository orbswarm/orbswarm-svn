/* read_config_var - a generic function used to read editable  */
/* files. It looks for a keyword and returns it value.         */
/* e.g. KEYWORD=value,  TEMP_DIR=/usr/tmp, MAX_DOWNLOAD=10     */
/* This file is generic enough to be included as a library call*/
/* **NOTE: The total maximum of one table entry is 80 chars    */


#include <stdio.h>
#include <string.h>

int read_config_var( char *, char * , char [] );

#ifndef GOOD
	#define GOOD 0
#endif
#ifndef BAD
	#define BAD -1
#endif
#ifndef UGLY
	#define UGLY -2
#endif

/* BAD == file error, UGLY == bad user parm                    */



int read_config_var( char *values_file, char *keyword , char value[] )
{
static char str[80];
int len;
FILE * _file; 


	if( keyword == NULL ) return (BAD);

	len = strlen(keyword);

	if( len > 77) return(UGLY);


	if( values_file )
	{ 
		_file = fopen(values_file, "r");
		if (_file == NULL) return(BAD);
	}
	else
		return(UGLY); 

	if( fseek(_file, 0, SEEK_SET) )
	{
		fclose(_file);
		return(BAD);
	}

	for(;;)
	{
		fgets(str, 80, _file);
		if( ferror(_file) || feof(_file) ) return(UGLY);
		len = strlen(str);

		if( strncmp(keyword, str, strlen(keyword)) == 0 )
		{ 
				if (str[len - 1] == '\n') str[--len] = 0;
				sprintf(value, "%s", &str[strlen(keyword)+1] ); break;
		}
	} 

	fclose(_file);

return 0;

}  /* end of read_config_var */
