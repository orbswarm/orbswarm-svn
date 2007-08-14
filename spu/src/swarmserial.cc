// ---------------------------------------------------------------------
// 
//	File: swarmserial.c
//      SWARM Orb SPU code http://www.orbswarm.com
//	serial com routines for TS-7260
//
//
// -----------------------------------------------------------------------

#include "../include/swarmserial.h"


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
    default:	 
      fprintf(stderr,"\n unsupported bit rate %d\n",baud);
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
  /* set raw input, 1 second timeout */
  options.c_cflag     |= (CLOCAL | CREAD);
  options.c_oflag     &= ~OPOST;
  options.c_cc[VMIN]  = 0;
  options.c_cc[VTIME] = 0;

  /*
   * Set the new options for the port...
   */
  fprintf(stderr,"\n Setting Config options to port\n");
  tcsetattr(fd, TCSANOW, &options);
  fprintf(stderr,"\n Done Setting Config options to port\n");

  return fd;
}
/* this is the OLD version -- had some undebuggable problems */
int OLDinitSerialPort(const char* port, int baud)
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
    default:	 
      fprintf(stderr,"\n unsupported bit rate %d\n",baud);
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
   //ioctl(port_fd, FIONREAD, numBytesRead);
   ioctl(port_fd, TIOCINQ , numBytesRead);
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
   // I took these out because they caused data loss (!) -- jtf
   //tcflush(port_fd, TCOFLUSH);
   //tcdrain(port_fd);
   return status;
}

//Reads data from serial port, port_fd, until either the ack character is 
//reached or the number of read attempts on the port exceeds maxTrys.
int readCharsFromSerialPortUntilAck(int port_fd, char* buff, int* numBytesRead, int maxBufSz, int maxTrys, char ackChar)
{
  int status = SWARM_SUCCESS;

  readCharsFromSerialPort(port_fd, buff, numBytesRead, maxBufSz); 
  if(numBytesRead > 0)
    fprintf(stderr,"\n LAST CHAR READ: %c AT POS %d\n",buff[*numBytesRead -1],*numBytesRead-1);

     //if(rindex(buff, ackChar) == NULL)
     if(buff[*numBytesRead -1] != ackChar)
     { 
      //We didn't get all of the data so we keep reading until we do
       int total_bytes = 0;
       int numTrys = 0;
       total_bytes = *numBytesRead;
       while(1)  
       {
         printf("\n NUM TRYS:%d\n",numTrys);
         if(numTrys > maxTrys){
            printf("Breaking out of read loop on try #%d\n",numTrys); 
            break; 
         }
         printf("CALLING READ A SECOND TIME\n"); 
         readCharsFromSerialPort(port_fd, &buff[total_bytes], numBytesRead,maxBufSz - total_bytes); 
         total_bytes += *numBytesRead;
         //printf("NUM BYTES READ: %d\n",total_bytes); 

         //if(rindex(buff, ackChar) != NULL)
         if(total_bytes > 0)
         {
         fprintf(stderr,"\n LAST CHAR READ 2: %c AT POS %d\n",buff[*numBytesRead -1],*numBytesRead-1);
         }
         if(buff[total_bytes -1] == ackChar)
           break; //we got it all so get out of the read loop 

         numTrys++;
       }
       *numBytesRead = total_bytes; 
     } 
    buff[*numBytesRead+1] = '\0';
  return status;
}

// treats porFd as a serial port fd. set portFd to -1 to avoid writing to 
// serial port useful for debugging.

int packetizeAndSendMotherShipData(int portFd, char* buffToWrite, int buffSz)
{
  int status = SWARM_SUCCESS;
  char aggPacket[MAX_AGG_PACKET_SZ];
  char aggPacketPayload[MAX_AGG_PACKET_PAYLOAD_SZ];
  int msgByteIdx = 0;
  int msgBytesLeft = buffSz;
   
  fprintf(stderr, "\n START packetizeAndSendMotherShipData BUFF SZ:%d",buffSz); 
  while(msgBytesLeft > 0)
  { 
    fprintf(stderr, "\n BUILD FULL PACKET INDEX : %d",msgByteIdx); 
    strncpy(aggPacketPayload,&buffToWrite[msgByteIdx],MAX_AGG_PACKET_PAYLOAD_SZ);
    msgByteIdx += strlen(aggPacketPayload);
    msgBytesLeft -= MAX_AGG_PACKET_PAYLOAD_SZ;
    fprintf(stderr, "\n BUILD FULL PACKET BYTES LEFT : %d",msgBytesLeft); 
 
    //compose final packet with header and footer  
    sprintf(aggPacket,"%s%s%s",AGGR_ZIGBEE_STREAM_WRITE_HEADER,aggPacketPayload,AGGR_ZIGBEE_STREAM_WRITE_END); 
    if(portFd < 0)
    {
      fprintf(stderr,"\nFULL PACKET---%s--- SIZE:%d\n",aggPacket,strlen(aggPacket));
    }
    else
    {
      writeCharsToSerialPort(portFd, aggPacket, strlen(aggPacket));
    }
    aggPacketPayload[0] = '\0';
    aggPacket[0] = '\0';
  }
  fprintf(stderr, "\n END packetizeAndSendMotherShipData"); 
  return status;
}
