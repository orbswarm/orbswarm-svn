#include  <stdio.h>    /* Standard input/output definitions */
#include  <unistd.h>
#include  <sys/ioctl.h>
#include  <sys/types.h>
#include  <sys/time.h>
#include  <sys/select.h>
#include "../include/swarmserial.h"
#include "../include/swarmdefines.h"

int main(int argc, char *argv[]) 
{
  int com1; /* File descriptor for the port */
  int com2; /* File descriptor for the port */
  int n;
  char buff[MAX_BUFF_SZ + 1];
  int bytes;
  char buff2[MAX_BUFF_SZ + 1];
  int bytes2 = 0;
  int             max_fd;
  fd_set          input;

  com1 = initSerialPort("/dev/ttyAM0", 38400);
  com2 = initSerialPort("/dev/ttyAM1", 38400);

  max_fd = (com2 > com1 ? com2 : com1) + 1;

  while(1)
  {
     /* Initialize the input set */
     FD_ZERO(&input);
     FD_SET(com1, &input);
     FD_SET(com2, &input);

     /* Do the select */
     n = select(max_fd, &input, NULL, NULL, NULL);

     /* See if there was an error */
     if (n < 0)
       fprintf(stderr,"select failed");
     else
     {
      /* We have input */
      if (FD_ISSET(com1, &input))
      {
        //Read data from com1
        readCharsFromSerialPort(com1, buff2, &bytes2,MAX_BUFF_SZ); 
        buff2[bytes2+1] = '\0';
        printf("\n Read the data:%s from serial port com1\n",buff2);
  
        //write data to com2 
        printf("\nWriting '''%s''' data back to com 2 \n",buff2);
        writeCharsToSerialPort(com2, buff2, bytes2);
      }
      if (FD_ISSET(com2, &input))
      {
        //Read data from com2
        readCharsFromSerialPort(com2, buff, &bytes ,MAX_BUFF_SZ); 
        buff[bytes+1] = '\0';
        printf("\n Read the data:%s from serial port com2\n",buff);
    
        //write data to com1 
        printf("\nWriting '''%s''' data back to com 1 \n",buff);
        writeCharsToSerialPort(com1, buff, bytes);
      }
    }
  }
} //END main() 

    
