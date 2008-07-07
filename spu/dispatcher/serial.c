// ---------------------------------------------------------------------
// 
//	File: swarmserial.c
//      SWARM Orb SPU code http://www.orbswarm.com
//	serial com routines for TS-7260
//
//
// -----------------------------------------------------------------------

#include "serial.h"


int initSerialPort(const char* port, int baud)
{

#ifdef LOCAL
#warning "compiling serial.h for LOCAL use (not SPU)"
 
 int fd; /* File descriptor for the file */
  fd = open(port, O_RDWR |  O_CREAT ,0x777 );
  fprintf(stderr,"Creating I/O file %s\n",port);
  return fd;
#else

  int fd; /* File descriptor for the port */
  struct termios options;
  speed_t brate = baud; /* let you override switch below if needed */

  fd = open(port, O_RDWR | O_NOCTTY | O_NDELAY);
  //fd = open(port, O_RDWR | O_NOCTTY);
  if(fd == -1){
    fprintf(stderr,"failed to open serial port%s\n",port);
  }

  /*
   * Get the current options for the port...
   */
  //fprintf(stderr,"\n Getting current options\n");
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
    default:	 
      fprintf(stderr,"\n unsupported bit rate %d\n",baud);
    }

  //fprintf(stderr,"\n Setting Baud rate to %d\n",baud);
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
  //fprintf(stderr,"\n Setting port to raw data mode\n");
  options.c_lflag &= ~(ICANON | ECHO | ECHOE | ISIG);
  /*
   * Enable the receiver and set local mode...
   */
  options.c_cflag |= (CLOCAL | CREAD);
  /* set raw input, 1 second timeout */
  options.c_cflag     |= (CLOCAL | CREAD);
  options.c_oflag     &= ~OPOST;
  options.c_cc[VMIN]  = 0;
  options.c_cc[VTIME] = 0;

  /*
   * Set the new options for the port...
   */
  //fprintf(stderr,"\n Setting Config options to port\n");
  tcsetattr(fd, TCSANOW, &options);
  //fprintf(stderr,"\n Done Setting Config options to port\n");
  return fd;
#endif
}

int readCharsFromSerialPort(int port_fd, char* buff, int maxBufSz){
  int numBytesAvail;
  //ioctl(port_fd, FIONREAD, numBytesRead);
#ifdef LOCAL
  numBytesAvail = 10; 		/* read the next 10 bytes */
#else
    ioctl(port_fd, TIOCINQ , &numBytesAvail);
#endif
    if(numBytesAvail > maxBufSz) {
      numBytesAvail = maxBufSz;
  } 
  /* read() returns number of bytes read; pass that upstairs */
  return(read(port_fd,buff,numBytesAvail));
}


int readCharsFromSerialPortBlkd(int port_fd, char* buff, int maxBufSz){
  return(read(port_fd,buff,maxBufSz));
}

int writeCharsToSerialPort(int port_fd, char* buff, int numBytesToWrite)
{
   int status = 1;
   int bytesWritten = 0;
   bytesWritten = write(port_fd, buff, numBytesToWrite);
   if (bytesWritten < 0){
     fprintf(stderr,"write() of %d bytes failed!\n", numBytesToWrite);
     status = 0;
   }
   // I took these out because they caused data loss (!) -- jtf
   //tcflush(port_fd, TCOFLUSH);
   //tcdrain(port_fd);
   return bytesWritten;
}

void flushSerialPort(int port_fd)
{
#ifndef LOCAL
   tcflush(port_fd, TCIFLUSH);
   tcdrain(port_fd);
#endif
}

int initSerialPortBlocking(const char* port, int baud)
{

#ifdef LOCAL
#warning "compiling serial.h for LOCAL use (not SPU)"
 
 int fd; /* File descriptor for the file */
  fd = open(port, O_RDWR |  O_CREAT ,0x777 );
  fprintf(stderr,"Creating I/O file %s\n",port);
  return fd;
#else

  int fd; /* File descriptor for the port */
  struct termios options;
  speed_t brate = baud; /* let you override switch below if needed */

  fd = open(port, O_RDWR | O_NOCTTY);
  //fd = open(port, O_RDWR | O_NOCTTY);
  if(fd == -1){
    fprintf(stderr,"failed to open serial port%s\n",port);
  }

  /*
   * Get the current options for the port...
   */
  //fprintf(stderr,"\n Getting current options\n");
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
    default:	 
      fprintf(stderr,"\n unsupported bit rate %d\n",baud);
    }

  //fprintf(stderr,"\n Setting Baud rate to %d\n",baud);
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
  //fprintf(stderr,"\n Setting port to raw data mode\n");
  options.c_lflag &= ~(ICANON | ECHO | ECHOE | ISIG);
  /*
   * Enable the receiver and set local mode...
   */
  options.c_cflag |= (CLOCAL | CREAD);
  /* set raw input, 1 second timeout */
  options.c_cflag     |= (CLOCAL | CREAD);
  options.c_oflag     &= ~OPOST;
  options.c_cc[VMIN]  = 0;
  options.c_cc[VTIME] = 0;

  /*
   * Set the new options for the port...
   */
  //fprintf(stderr,"\n Setting Config options to port\n");
  tcsetattr(fd, TCSANOW, &options);
  //fprintf(stderr,"\n Done Setting Config options to port\n");
  return fd;
#endif
}

