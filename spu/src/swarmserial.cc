#include "../include/swarmserial.h"

/********************************************
 * Opens a serial port specified by port at 
 * a baud rate specified by baud. The port is
 * set to 8N1 parity.
 */
int initSerialPort(const char* port, int baud)
{
  int fd; /* File descriptor for the port */
  struct termios options;
  speed_t brate = baud; /* let you override switch below if needed */

  fd = open(port, O_RDWR | O_NOCTTY | O_NDELAY);
  if(fd == -1){
    fprintf(stderr,"failed to open serial port%s\n",port);
  }

  /*
   * Get the current options for the port...
   */
  fprintf(stderr,"\n Getting current options\n");
  tcgetattr(fd, &options);
  /*
   * Set the baud rates to 115200...
   */
    switch(baud) 
    {
      case 4800:   brate=B4800;   break;
      case 9600:   brate=B9600;   break;
  // if you want these speeds, uncomment these and set #defines if Linux
  //#ifndef OSNAME_LINUX
  //    case 14400:  brate=B14400;  break;
  //#endif
      case 19200:  brate=B19200;  break;
  //#ifndef OSNAME_LINUX
  //    case 28800:  brate=B28800;  break;
  //#endif
      case 38400:  brate=B38400;  break;
      case 57600:  brate=B57600;  break;
      case 115200: brate=B115200; break;
    }

  fprintf(stderr,"\n Setting Baud rate to %d\n",baud);
    cfsetispeed(&options, brate);
    cfsetospeed(&options, brate);

  /*
   * Set port to 8N1 pairity
   */
  options.c_cflag &= ~PARENB;
  options.c_cflag &= ~CSTOPB;
  options.c_cflag &= ~CSIZE;
  options.c_cflag |= CS8;

  /*
   * Setup port in raw data mode.
   */
  fprintf(stderr,"\n Setting port to raw data mode\n");
  options.c_lflag &= ~(ICANON | ECHO | ECHOE | ISIG);

  /*
   * Enable the receiver and set local mode...
   */
  options.c_cflag |= (CLOCAL | CREAD);

  /*
   * Set the new options for the port...
   */
  fprintf(stderr,"\n Setting Config options to port\n");
  tcsetattr(fd, TCSANOW, &options);
  fprintf(stderr,"\n Done Setting Config options to port\n");

  return fd;
}

void readCharsFromSerialPort(int port_fd, char* buff, 
                            int* numBytesRead, int maxBufSz)
{
   ioctl(port_fd, FIONREAD, numBytesRead);
   if(*numBytesRead > maxBufSz)
   {
     *numBytesRead = maxBufSz;
   } 
   read(port_fd,buff,*numBytesRead);
}
int writeCharsToSerialPort(int port_fd, char* buff,
                                        int numBytesToWrite)
{
   int status = SWARM_SUCCESS;
   int bytesWritten = 0;
   bytesWritten = write(port_fd, buff, numBytesToWrite);
   if (bytesWritten < 0)
   {
     fprintf(stderr,"write() of %d bytes failed!\n", numBytesToWrite);
     status = SWARM_SERIAL_WRITE_ERR;
   }
   //tcflush(port_fd, TCOFLUSH);
   //tcdrain(port_fd);
   return status;
}
