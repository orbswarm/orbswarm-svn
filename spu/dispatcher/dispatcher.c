
/*
 *      File: dispatcher.c
 *      SWARM Orb SPU code http://www.orbswarm.com
 *      prototypes and #defs for swarm serial com routines
 *
 *      main loop here. 
 *      read input data from COM2. Parse it and dispatch parsed commands
 *      written by Jonathan Foote (Head Rotor at rotorbrain.com)
 *      based on lots of code by Matt, Dillo, Niladri, Rick,
 * 
 * How it works? - niladri.bora@gmail.com
 * The dispatcher is actually 3 processes -
 * 1. The main dispatcher process : 
 * 			The main process that listens for messages coming on COM2 (via a timed
 * 	  select). Each message is tokenized by the tokenizer in doScanner() which
 * 	  in turn calls the lemon genrated parser with the tokens. The grammar is 
 * 	  defined in scan.y. On sucessful parsing of each message type the corresponding
 * 	  dispatch*()(dispatchMCUCmd(), dispatchLEDCmd(), dispatchSPUCmd(), dispatchGpsVelocityMsg()
 * 	  and dispatchGpsLocationMsg()) function is called from the action rule.
 * 
 * 2. Gps message processor:
 * 			This(()) is a child process that reads messages from the gps data queue.
 * 	  The main dispatcher process writes all gps location and velocity messages 
 *    (see  dispatchGpsVelocityMsg() and dispatchGpsLocationMsg()) into this queue. 
 * 	  The queue is implemented using a circular buffer in shared memory. Additionally 
 * 	  this process and the main dispatcher process synchronize the reading and writing to this 
 * 	  queue via an IPC implemented using pipes. After pushing a message in the queue the 
 *    main dispatcher process writes a character into the pipe. The gps message processor does a blocked read 
 *    on the pipe and whenever it gets something it knows there is a corresponding message in the shared memory
 * 	  queue(see tellChild() and waitParent()). Each GPS location or velocity message 
 * 	  is passed through the parsing routines in gpsutils.c. And the latest velocity and location 
 *    data points are extracted and stored in 'latestGpsCordinates' which is a shared memory 
 *    data structure for the gronkulator to use.
 * 
 * 3. Gronkulator:
 * 			This child process(started by startChildProcessToGronk() in gronkulator.c) 
 *    runs the 10 Hz loop that will eventually call the Kalman Filter to 
 * 	  do the estimated corrections. When the timer overflows every 100 ms(not accurate) or so
 *    it queries the daughterboard on COM5 to get the drive and steering measurements and outputs
 *    that alongwith the most current gps location and velocity values from the variable 'latestGpsCordinates'
 * 	  in shared memory. The gronkulator also recieves MCU commands(sent from the remotes or the 
 * 	  mothership via the aggregator) from the main dispatcher process through a shared memory queue. 
 *    The communication and synchronization for this queue is done in a manner similar to that between
 *    the main dispatcher process and the gps message processor(see gps message processor above). 
 *    All this happens every 10 ms tick of the timer.  
 * */  


#include <stdio.h>              /* Standard input/output definitions */
#include <unistd.h>
#include <sys/ioctl.h>
#include <sys/time.h>
#include <sys/select.h>
#include "spuutils.h"
#include "serial.h"
#include "scanner.h"
#include <getopt.h>
#include "gpsutils.h"
#include "queues.h"
//#include "gronkulator.h"
#include <sys/shm.h>
#include <sys/stat.h>
#include <signal.h>
#include <stdlib.h>
#include <errno.h>
#include <stdarg.h>
#include "imuutils.h"


//#define LOCAL
int parseDebug = eMcuLog;       /*  parser uses this for debug output */
int parseLevel = eLogDebug;

int myOrbId = 60;               /* which orb are we?  */

int com1 = 0;                   /* File descriptor for the port */
int com2 = 0;                   /* File descriptor for the port */
int com3 = 0, com5 = 0;         /* ditto */

Queue *gpsQueuePtr;
Queue *mcuQueuePtr;
swarmGpsData *latestGpsCordinates;

int gpsQueueSegmentId = -1;
struct shmid_ds gpsQueueShmidDs;
int mcuQueueSegmentId = -1;
struct shmid_ds mcuQueueShmidDs;
int latestGpsCordinatesSegmentId = -1;
struct shmid_ds latestGpsCordinatesShmidDs;

int isParent = 1;
enum ECommandPipe
  {
    eGpsCommandPipeId = 1,
    eMcuCommandPipeId
  };
static int pfd1[2] /*Gps */ , pfd2[2] /*mcu */ ;

int
isLogging (int nLogArea, int nLogLevel)
{
  if (eLogError == nLogLevel
      || (nLogLevel >= parseLevel && nLogArea == parseDebug))
    return 1;
  else
    return 0;
}

void
logit (int nLogArea, int nLogLevel, char *strFormattedSring, ...)
{
  va_list fmtargs;
  char buffer[1024];
  va_start (fmtargs, strFormattedSring);
  vsnprintf (buffer, sizeof (buffer) - 1, strFormattedSring, fmtargs);
  va_end (fmtargs);
  if (eLogError == nLogLevel)
    {
      fprintf (stderr, "%s", buffer);
      fprintf (stdout, "%s", buffer);
    }
  else if (nLogLevel >= parseLevel && nLogArea == parseDebug)
    fprintf (stdout, "%s", buffer);
}

/*
 * Sets up the pipes to communicate between parent and children
 */
static void
tellWait (void)
{
  if (pipe (pfd1) < 0 || pipe (pfd2) < 0)
    {
      fprintf (stderr, "pipe error");
      exit (EXIT_FAILURE);
    }
}

/* static void *//* TELL_PARENT() *//* { *//*   if (write(pfd2[1], "c", 1) != 1) *//*     fprintf(stderr, "write error"); *//* } */
static void
waitParent (int nCommandPipeId)
{
  char c;
  if (eGpsCommandPipeId == nCommandPipeId)
    {
      if (read (pfd1[0], &c, 1) != 1)
        fprintf (stderr, "read error");

      if (c != 'g')
        fprintf (stderr, "WAIT_PARENT: incorrect data");
    }
  else if (eMcuCommandPipeId == nCommandPipeId)
    {
      if (read (pfd2[0], &c, 1) != 1)
        fprintf (stderr, "read error");

      if (c != 'm')
        fprintf (stderr, "WAIT_PARENT: incorrect data");
    }
}

static void
tellChild (int nCommandPipeId)
{
  if (eGpsCommandPipeId == nCommandPipeId)
    {
      if (write (pfd1[1], "g", 1) != 1)
        fprintf (stderr, "shm write error");
    }
  else if (eMcuCommandPipeId == nCommandPipeId)
    {
      if (write (pfd2[1], "m", 1) != 1)
        fprintf (stderr, "shm write error");
    }
}

static void
onShutdown (void)
{
  if (isParent && -1 != gpsQueueSegmentId)
    {
      shmctl (gpsQueueSegmentId, IPC_RMID, 0);
      fprintf (stderr, "\n cleaned shared mem for gps message queue");
    }
  if (isParent && -1 != mcuQueueSegmentId)
    {
      shmctl (mcuQueueSegmentId, IPC_RMID, 0);
      fprintf (stderr, "\n cleaned shared mem for mcu message queue");
    }
  if (isParent && -1 != latestGpsCordinatesSegmentId)
    {
      shmctl (latestGpsCordinatesSegmentId, IPC_RMID, 0);
      fprintf (stderr,
               "\n cleaned shared mem for latest gps co-ord data struct");
    }
}
static void
signalHandler (int signo)
{
  if (SIGTERM == signo || SIGINT == signo || SIGQUIT == signo)
    {
      onShutdown ();
      exit (EXIT_SUCCESS);
    }
  else
    onShutdown ();
  exit (EXIT_FAILURE);
}

int
initSharedMem (void)
{
  //Allocate shared memory for GPS struct that represents latest co-ordintaes
  latestGpsCordinatesSegmentId = shmget (IPC_PRIVATE, sizeof (swarmGpsData),
                                         IPC_CREAT | IPC_EXCL | S_IRUSR |
                                         S_IWUSR);
  if (-1 == latestGpsCordinatesSegmentId)
    return 0;
  //Attach
  latestGpsCordinates =
    (swarmGpsData *) shmat (latestGpsCordinatesSegmentId, 0, 0);
  if (-1 == (int) latestGpsCordinates)
    return 0;
  //read shared memory data structure
  if (-1 ==
      shmctl (latestGpsCordinatesSegmentId, IPC_STAT,
              &latestGpsCordinatesShmidDs))
    return 0;
  logit (eDispatcherLog, eLogDebug,
         "\nsegment size for latest gps co-ord data struct=%d",
         gpsQueueShmidDs.shm_segsz);

  //Allocate shared memory for GPS data from the aggregator
  gpsQueueSegmentId = shmget (IPC_PRIVATE, sizeof (Queue),
                              IPC_CREAT | IPC_EXCL | S_IRUSR | S_IWUSR);
  if (-1 == gpsQueueSegmentId)
    return 0;
  //Attach shared memory
  gpsQueuePtr = (Queue *) shmat (gpsQueueSegmentId, 0, 0);
  if (-1 == (int) gpsQueuePtr)
    return 0;
  //read shared memory data structure
  if (-1 == shmctl (gpsQueueSegmentId, IPC_STAT, &gpsQueueShmidDs))
    return 0;
  logit (eDispatcherLog, eLogDebug, "\nsegment size for gps msg queue=%d",
         gpsQueueShmidDs.shm_segsz);

  //Allocate shared memory for mcu commands coming from the aggregator 
  mcuQueueSegmentId = shmget (IPC_PRIVATE, sizeof (Queue),
                              IPC_CREAT | IPC_EXCL | S_IRUSR | S_IWUSR);
  if (-1 == mcuQueueSegmentId)
    return 0;
  //Attach shared memory 
  mcuQueuePtr = (Queue *) shmat (mcuQueueSegmentId, 0, 0);
  if (-1 == (int) mcuQueuePtr)
    return 0;
  //read shared memory data structure
  if (-1 == shmctl (mcuQueueSegmentId, IPC_STAT, &mcuQueueShmidDs))
    return 0;
  logit (eDispatcherLog, eLogDebug, "\nsegment size for spu msg queue=%d",
         mcuQueueShmidDs.shm_segsz);

  return 1;
}

void
doChildProcessToGronk (void)
{
  struct timeval lastGronkTime;
  gettimeofday (&lastGronkTime, NULL);
  struct timeval nowGronkTime;
  struct timeval timeout;
  char buffer[MSG_LENGTH + 1];
  char steer_buffer[MSG_LENGTH + 1];
  char drive_buffer[MSG_LENGTH + 1];
  int i_bytesRead = 0;
  int md_bytesRead = 0;
  fd_set blockSet;
  struct swarmImuData imuData;
  struct swarmMotorData motorData;
  
  int kalmanDataFileFD = open("kalman.data", O_RDWR |  O_CREAT | O_NDELAY ,0x777 );
  if(kalmanDataFileFD < 0)

  while (1)
    {
      gettimeofday (&nowGronkTime, NULL);
      time_t deltaSecs = nowGronkTime.tv_sec - lastGronkTime.tv_sec;
      long deltaMillis =
        (nowGronkTime.tv_usec - lastGronkTime.tv_usec) / 1000;
      if (deltaMillis < 0)
        {
          deltaMillis += 1000;
          deltaSecs--;
        }
      if ((deltaSecs * 1000 + deltaMillis) > GRONKULATOR_FREQ_IN_MILLIS)
        {
          //Time up. Query daughterboard for IMU data, gather GPS data and
          //call the Kalman Filter

          //First get the IMU data and stuff it into a buffer
          writeCharsToSerialPort (com5, "$QI*", 4);
          i_bytesRead = readCharsFromSerialPortBlkd (com5, buffer, 115);
          buffer[i_bytesRead]=0;
          writeCharsToSerialPort (com5, "$QD*", 4);
          
          logit (eMcuLog, eLogInfo, "\n IMU data=%s bytes read=%d",  buffer, i_bytesRead); 
          //parse buffer. This is where the buffer we read off com5 gets stuffed into the imuData struct
          parseImuMsg (buffer, &imuData);
          logImuDataString (&imuData, buffer);

          //Then get the Motor data as soon after the IMU data poll
          //First the drive data
          md_bytesRead =
            readCharsFromSerialPortBlkd (com5, drive_buffer, 84);

          drive_buffer[md_bytesRead] = 0;
          logit (eMcuLog, eLogInfo, "\n Motor data(drive)=%s bytes read=%d", 
          drive_buffer, md_bytesRead);
          
          buffer[i_bytesRead] = 0;
          char parsedAndFormattedGpsCoordinates[96];
          sprintf (parsedAndFormattedGpsCoordinates,
                   "{orb=%d northing=%f easting=%f utmzone=%s}", myOrbId,
                   latestGpsCordinates->UTMNorthing,
                   latestGpsCordinates->UTMEasting,
                   latestGpsCordinates->UTMZone);

          //Parse moto_buffer into the motorData
          drive_buffer[md_bytesRead] = 0;
          parseDriveMsg (drive_buffer, &motorData);
          logDriveDataString (&motorData, drive_buffer);
          logit(eMcuLog, eLogDebug, "\nformatted drive response from db=%s",
          			drive_buffer);

		  char dataFileBuffer[1024];		
          sprintf (dataFileBuffer, "\n%u,%u,%s,%f,%f,%s,%f,%f,%f",
                  (unsigned int) nowGronkTime.tv_sec,
                  (unsigned int) nowGronkTime.tv_usec / 1000, 
                  buffer,/*formatted IMU data*/
                  latestGpsCordinates->UTMNorthing,
                  latestGpsCordinates->UTMEasting,
                  latestGpsCordinates->UTMZone,
                  latestGpsCordinates->nmea_course,
                  latestGpsCordinates->speed, 
                  motorData.speedRPS
			);
 			int bytesWritten = 0;
 			logit(eMcuLog, eLogDebug, "\nwriting to file=%s", dataFileBuffer); 
   			bytesWritten = write(kalmanDataFileFD, dataFileBuffer, 
   				strlen(dataFileBuffer));
   			if (bytesWritten < 0){
     			fprintf(stderr,"write() of %d bytes failed!\n", strlen((const char*)kalmanDataFileFD));
   			}

          //reset timer and start over 
          lastGronkTime = nowGronkTime;
        }
      else
        {
          //Not time to gronk yet. Process mcu commands
          FD_ZERO (&blockSet);
          FD_SET (pfd2[0], &blockSet);
          timeout.tv_sec = 0;
          timeout.tv_usec = 10000;      //10 milli secs is the smallest we can safely set..I think
          int nSelectResult =
            select (pfd2[0] + 1, &blockSet, NULL, NULL, &timeout);
          //int nSelectResult=1;
          if (nSelectResult < 0)
            logit (eMcuLog, eLogError, "\nError in select on mcu pipe");
          else if (0 == nSelectResult)
          ;
//	            logit (eMcuLog, eLogInfo,
//	                   "\nSelect timed out with no data in mcu pipe");
          else
            {
              if (FD_ISSET (pfd2[0], &blockSet) /*1 */ )
                {
                  waitParent (eMcuCommandPipeId);      //guranteed to not block
                  if (pop (buffer, mcuQueuePtr))
                    {
                      logit (eMcuLog, eLogDebug,
                             "\ngot mcu message from parent=%s", buffer);
                      writeCharsToSerialPort (com5, buffer,
                                              strlen (buffer) + 1);
                    }
                  else
                    {
                      logit (eMcuLog, eLogError,
                             "\npop returned nothing. shouldn't be here");
                    }
                }
              else
                logit(eMcuLog, eLogDebug, "\n selected data no longer available");
            }
        }
    }
}

void
doChildProcessToProcessGpsMsg (void)
{
  logit (eGpsLog, eLogDebug, "\ndoChildProcessToProcessGpsMsg():START");
  while (1)
    {
      char buffer[MSG_LENGTH];
      waitParent (eGpsCommandPipeId);
      if (pop (buffer, gpsQueuePtr))
        {
          //we have some thing
          logit (eGpsLog, eLogDebug, "\ngot GPS message=%s", buffer);
          char resp[96];
          if (0 == strncmp (buffer, "GPGGA", 5))
            {
              logit (eGpsLog, eLogDebug, "\n+++++++got GGA message+++++++");
              strncpy (latestGpsCordinates->ggaSentence, buffer, MSG_LENGTH);
              int status = parseGPSGGASentence (latestGpsCordinates);

              logit (eGpsLog, eLogDebug, "parseGPSSentence() return=%d\n",
                     status);
              status =
                convertNMEAGpsLatLonDataToDecLatLon (latestGpsCordinates);
              if (status == SWARM_SUCCESS)
                {
                  logit (eGpsLog, eLogDebug,
                         "\n Decimal lat:%lf lon:%lf utctime:%s \n",
                         latestGpsCordinates->latdd,
                         latestGpsCordinates->londd,
                         latestGpsCordinates->nmea_utctime);

                  decimalLatLongtoUTM (WGS84_EQUATORIAL_RADIUS_METERS,
                                       WGS84_ECCENTRICITY_SQUARED,
                                       latestGpsCordinates);
                  logit (eGpsLog, eLogDebug,
                         "Northing:%f,Easting:%f,UTMZone:%s\n",
                         latestGpsCordinates->UTMNorthing,
                         latestGpsCordinates->UTMEasting,
                         latestGpsCordinates->UTMZone);
                }
              else
                {
                  logit (eGpsLog, eLogError,
                         "\ncouldn't convertNMEAGpsLatLonDataToDecLatLon status="
                         "%d", status);
                }
//              sprintf (resp, "{orb=%d\nnorthing=%f\neasting=%f\nutmzone=%s}",
//                       myOrbId, latestGpsCordinates->UTMNorthing,
//                       latestGpsCordinates->UTMEasting,
//                       latestGpsCordinates->UTMZone);
              logit (eGpsLog, eLogInfo, "\n sending msg to spu=%s", resp);
              writeCharsToSerialPort (com2, resp, strlen (resp));
            }                   //end if GPGGA
          else if (0 == strncmp (buffer, "GPVTG", 5))
            {
              logit (eGpsLog, eLogDebug, "\n+++++++got VTG message+++++++");
              strncpy (latestGpsCordinates->vtgSentence, buffer, MSG_LENGTH);
              int nStatus = parseGPSVTGSentance (latestGpsCordinates);
              logit (eGpsLog, eLogDebug,
                     "\n parsed vtg sentence=%s \nreturn=%d",
                     latestGpsCordinates->vtgSentence, nStatus);
            }
          else
            logit (eGpsLog, eLogError,
                   "\n+++++++got unknown message+++++++, msg=%s",
                   latestGpsCordinates->ggaSentence);
        }
      else
        {
          logit (eGpsLog, eLogError,
                 "\npop returned nothing. shouldn't be here");
        }
    }
}

/* Parser calls this when there is a complete MCU command */
/* if the addr matches our IP, send the command str out COM2 */
void
dispatchMCUCmd (int spuAddr, cmdStruct * c)
{
  if (spuAddr != myOrbId)
    return;
  if (parseDebug == 5)
    printf ("Orb %d Got MCU command: \"%s\"\n", spuAddr, c->cmd);
  //    writeCharsToSerialPort(com5, c->cmd, c->cmd_len);
  if (push (c->cmd, mcuQueuePtr))
    {
      logit (eMcuLog, eLogDebug, "\n successfully pushed mcu msg");
      tellChild (eMcuCommandPipeId);
    }
  else
    {
      logit (eMcuLog, eLogWarn, "\n push failed. mcu Q full");
    }
}

/* Parser calls this when there is a complete LED command *//* if the addr matches our IP, send the command str out COM3 */
void
dispatchLEDCmd (int spuAddr, cmdStruct * c)
{
  if (spuAddr != myOrbId)
    return;
  if (parseDebug == 3)
    printf ("Orb %d Got LED command: \"%s\"\n", spuAddr, c->cmd);
  writeCharsToSerialPort (com3, c->cmd, c->cmd_len);
}

/* Parser calls this when there is a complete SPU command *//* if the addr matches our IP, handle it */
void
dispatchSPUCmd (int spuAddr, cmdStruct * c)
{
  if (spuAddr != myOrbId)
    return;

  if (parseDebug == 4)
    printf ("Orb %d Got SPU command: \"%s\"\n", spuAddr, c->cmd);
  /* handle the command here */
}

void
dispatchGpggaMsg (cmdStruct * c)
{
  //logit(eGpsLog, eLogDebug, "got gps gpgga msg: \"%s\"\n", c->cmd);

  if (push (c->cmd, gpsQueuePtr))
    {
      //logit(eGpsLog, eLogDebug, "\n successfully pushed GPS msg");
      tellChild (eGpsCommandPipeId);
    }
  else
    {
      //logit(eGpsLog, eLogWarn, "\n push failed. gps Q full");
    }
}

void
dispatchGpvtgMsg (cmdStruct * c)
{
  //logit(eGpsLog, eLogDebug, "got gps gpvtg msg: \"%s\"\n", c->cmd);
  if (push (c->cmd, gpsQueuePtr))
    {
      //logit(eGpsLog, eLogDebug, "\n successfully pushed GPS msg");
      tellChild (eGpsCommandPipeId);
    }
  else
    {
      //logit(eGpsLog, eLogWarn, "\n push failed. gps Q full");
    }
}

int
main (int argc, char *argv[])
{
  /* handle SIGINT, but only if it isn't ignored */
  if (signal (SIGINT, SIG_IGN) != SIG_IGN)
    {
      if (signal (SIGINT, signalHandler) == SIG_ERR)
        {
          fprintf (stderr, "\nFailed to handle SIGINT!\n");
          exit (EXIT_FAILURE);
        }
    }

  /* handle SIGQUIT, but only if it isn't ignored */
  if (signal (SIGQUIT, SIG_IGN) != SIG_IGN)
    {
      if (signal (SIGQUIT, signalHandler) == SIG_ERR)
        {
          fprintf (stderr, "\nFailed to handle SIGQUIT!\n");
          exit (EXIT_FAILURE);
        }
    }

  /*handle SIGTERM */
  if (signal (SIGTERM, signalHandler) == SIG_ERR)
    {
      fprintf (stderr, "\nFailed to handle SIGTERM");
      exit (EXIT_FAILURE);
    }

  /* increment this counter every time through 10 hz timeout loop */
  int tenHzticks = 0;

  /* init lemon parser here */
  void *pParser = ParseAlloc (malloc);
  //  int i = 0;        
  //int seconds = 0;            /* seconds we've been running */

  int optchar = 0;
  int dbgflags = 0;

  /* vars for the select() call */
  fd_set input;
  struct timeval tv;            /* store select() timeout here */
  int selectResult;

  /* store input data from COM2 here */
  char buff[BUFLENGTH + 1];
  int bytesRead = 0;

  //Get this orbs Address from its IP address
  char myIP[32];
  getIP ("eth0", myIP);

  printf ("\ndispatcher gotIP\n");
  //if(enableDebug)
  //fprintf(stderr,"\nMY IP ADDRESS: %s\n",myIP);
  char *orbAddStart = rindex (myIP, '.');
  myOrbId = atoi (&orbAddStart[1]);

  if (argc >= 3)
    {
      parseDebug = atoi (argv[2]);
      fprintf (stderr, "\ndispatcher:  verbose %d\n", parseDebug);
    }

  while ((optchar = getopt (argc, argv, "gilmo:st")) != EOF)
    {

      switch (optchar)
        {

        case 'g':
          {
            dbgflags |= DEBUG_GPS;
            break;
          }
        case 'i':
          {
            dbgflags |= DEBUG_IMU;
            break;
          }
        case 'l':
          {
            dbgflags |= DEBUG_LED;
            break;
          }
        case 'm':
          {
            dbgflags |= DEBUG_MCU;
            break;
          }
        case 'o':
          {
            myOrbId = atoi (optarg);
            break;
          }
        case 's':
          {
            dbgflags |= DEBUG_SPU;
            break;
          }
        case 't':
          {
            dbgflags |= DEBUG_T;
            break;
          }
        }
    }

  printf ("debug flags are %d\n", dbgflags);

  if (parseDebug)
    {
      fprintf (stderr, "Dispatcher running for Orb ID: %d\n", myOrbId);
    }
#ifdef LOCAL
#warning "compiling dispatcher.c for LOCAL use (not SPU)"
  /* simulate serial i/o with files */
  com2 = initSerialPort ("bigtestinput", 0);
  com3 = initSerialPort ("./com3out", 0);
  com5 = initSerialPort ("./com5out", 0);

#else
  /* Command stream comes in on COM2 */
  com2 = initSerialPort (COM2, 38400);

  /* LED commands go out on COM3 (write only) */
  com3 = initSerialPort (COM3, 38400);

  /* Motor Control commands go to and from COM5 */
  //com5 = initSerialPort (COM5, 38400);
  com5 = initSerialPortBlocking(COM5, 38400);
#endif


  /* find maximum fd for the select() */
  int max_fd;
  max_fd = (com2 > com1 ? com2 : com1);
  max_fd = (com3 > max_fd ? com3 : max_fd);
  max_fd = (com5 > max_fd ? com5 : max_fd);
  max_fd++;


  if (initSharedMem ())
    {
      logit (eGpsLog, eLogDebug, "\n shared memory initialized successfully");
    }
  else
    {
      fprintf (stderr, "\n shared memory init UNSUCCESSFUL");
      return (1);
    }
  //set up pipes 
  tellWait ();
  pid_t pid;
  //First fork doChildProcessToProcessGpsMsg()
  if ((pid = fork ()) < 0)
    {
      fprintf (stderr, "\n fork() unsuccessful");
      onShutdown ();
      return (2);
    }
  else if (pid == 0)
    {
      isParent = 0;
      doChildProcessToProcessGpsMsg ();
    }
#if LOCAL
  //
  else
    {                           //We don't run the gronkulator in LOCAL mode
#else
  else
    {                           //parent
      //Now fork gronkulator if not running in LOCAL mode
      //The gronkulator writes queries to the daughterboard/mcu com port and 
      //expects to get reuslts back. This is not easy to simulate in the test
      //mode. TBD
      if ((pid = fork ()) < 0)
        {
          fprintf (stderr, "\ngronkulator fork() unsuccessful");
          onShutdown ();
          return (2);
        }
      else if (pid == 0)
        {
          isParent = 0;
          doChildProcessToGronk ();
        }
      else
        {                       //still the parent
#endif
          while (1)
            {
              /* Initialize the input set for select() */
              FD_ZERO (&input);
              FD_SET (com2, &input);
              //FD_SET(com5, &input);

              /* set select timout value */
              tv.tv_sec = 0;
              tv.tv_usec = 100000;      // 100 ms or 10 Hz

              /* Do the select */
              selectResult = select (max_fd, &input, NULL, NULL, &tv);

              /* See if there was an error */
              if (selectResult < 0)
                {
                  printf ("Error during select\n");
                  continue;
                }
              if (selectResult == 0)
                {               /* select times out with a result of 0 */
                  ++tenHzticks;
                  // if we've turned it on to signal data in
                  setSpuLed (SPU_LED_RED_OFF);
                  if (tenHzticks == 5)
                    {
                      setSpuLed (SPU_LED_GREEN_ON);
                    }

                  if (tenHzticks == 10)
                    {
                      tenHzticks = 0;
                      setSpuLed (SPU_LED_GREEN_OFF);
                      //printf(" Runtime: %d\n", seconds++);
                    }
                }
              else
                {               /* we got a select; handle it */

                  if (FD_ISSET (com2, &input))
                    {

                      bytesRead =
                        readCharsFromSerialPort (com2, buff, BUFLENGTH);


                      if (bytesRead > 0)
                        {       /* if we actually got some data, then parse it */
                          buff[bytesRead] = 0;  /* null-term buffer if not done already */
                          /* got some input so flash red LED for indication */
                          setSpuLed (SPU_LED_RED_ON);
                          logit (eDispatcherLog, eLogInfo,
                                 "\nReceived \"%s\" from  com2\n", buff);
                          if (isLogging (eDispatcherLog, eLogInfo))
                            fflush (stdout);
                          /* send bytes down to the parser. When it gets a command it
                             will call the dispatch*() functions */
                          doScanner (pParser, buff);
                        }
                      else if (bytesRead < 0)
                        fprintf (stderr, "\nGot error from read");
#ifdef LOCAL
                      else
                        {       /* no bytes read means EOF */
                          onShutdown ();
                          return (0);
                        }
#endif
                    }
                }
            }                   // end while(1)
        }
#ifndef LOCAL
    }
#endif
  onShutdown ();
  return (0);                   // keep the compiler happy
    }

  //END main() 
