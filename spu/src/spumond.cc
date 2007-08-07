// ---------------------------------------------------------------------
// 
//	File: spumond.cc
//      SWARM Orb SPU code http://www.orbswarm.com
//      Description:Launches tasks into the background and restarts them if they die     
//
//      usage: spumond <binary executable to watch>
//      other descriptive data here, including dependencies
//      
//
//	Written by Steve " 'dillo" Okay <armadilo@gothpunk.com>
// -----------------------------------------------------------------------


#include <unistd.h>
#include <stdarg.h>
#include <stdio.h>
#include <stdlib.h>
#include <sys/reboot.h>
#include <string.h>
#include <errno.h>
#include <fcntl.h>
#include <signal.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <sys/mount.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <syscall.h>
#include <dirent.h>
#include "../include/swarmserial.h"
#include "../include/swarmdefines.h"
#include "../include/swarmspuutils.h"

time_t logtime;
char logbuf[80];
char curtime[30];

char *logdir="/tmp";

int launch_and_watch(char *clitask)
{
        int cli_pid;
	int fd;

        if ( (cli_pid = fork()) < 0) {
             perror ("launcher failed to fork before exec:");
	     time(&logtime);
 	     strncpy(curtime,(ctime(&logtime)),26);
	     snprintf(logbuf,80,"spumond:%s:FATAL - launcher failed to fork before exec\n",curtime);
	     spulog(logbuf,80,logdir);
             exit (-1);
        } else if (cli_pid == 0) {
		/*daemonize the child here*/
		/*Make us our own pgrp leader */
  			if (setsid() < 0) {
    				fprintf(stderr, "%s failed to daemonize because setsid (%s)\n", clitask,strerror(errno));
	     			time(&logtime);
 	     			strncpy(curtime,(ctime(&logtime)),26);
	     			snprintf(logbuf,80,"spumond:%s:FATAL-%s failed on setsid",curtime,clitask);
	     			spulog(logbuf,80,logdir);
				snprintf(logbuf,80,"spumond:%s:setsid failed because %s",curtime,strerror(errno));
				spulog(logbuf,80,logdir);
  			}
		   
			if ((fd = open("/dev/tty", O_RDWR)) >= 0) {
    				ioctl(fd, TIOCNOTTY, (char *) 0);
    				close(fd); 
  			}
	
		/*Close any remaining fds we got from the parent */
		/*We should be a child of init, so we'll just skip all this  */
	/*	for (fd = 10;  fd >= 0;  fd--)
		{
			close(fd);
		}
         */
		/*ignore STDIN*/	
  	/*	if ((fd = open("/dev/null", O_RDWR)) < 0) 
		{
    			fprintf(stderr, "open: /dev/null (%s)\n", strerror(errno));			
  		}
	*/
		/*now that we're on our own, re-open stdio channels*/	
	  /*
		dup2(fd, STDIN_FILENO);
		dup2(fd, STDOUT_FILENO);
    		dup2(fd, STDERR_FILENO);
	   */	
		/*reposition to start from root */
		chdir("/"); 
		/*Normally a good idea, but probably not necessary here */
		/*Let's see what happens				*/

		/*set the umask to something that normal people use*/
		umask(022);

		fprintf(stderr,"Launching %s..\n",clitask);
		/*do the exec here */
		if ((execve(clitask,NULL,NULL)) < 0) {
			perror("execve failed because:");
			exit(-1);
		}
        } 
		fprintf(stderr,"Launched %s with pid %d\n",clitask,cli_pid);
	        time(&logtime);
 	        strncpy(curtime,(ctime(&logtime)),26);
	        snprintf(logbuf,80,"spumond:OK-launched %s with pid %d at %s",clitask,cli_pid,curtime);
	        spulog(logbuf,80,logdir);
		return(cli_pid);
}

int main (int argc, char **argv) {
int results,child_pid,child_stat;
struct stat statbuf;
pid_t mypid;


      if (argc < 2) {
    fprintf(stderr,"spumond:please supply a file \n");
    exit(-1);
  }
  
  mypid=getpid();
  time(&logtime);
  results = stat(argv[1], &statbuf);
   if (results < 0 ) {
    perror("stat");
    fprintf(stderr,"spumond:error trying to stat %s:\n",argv[1]);
    strncpy(curtime,(ctime(&logtime)),26);
    snprintf(logbuf,80,"spumond[%d]:FATAL! could not find %s...exiting\n",mypid,curtime);
    spulog(logbuf,80,logdir);
    exit(-1);
  }

	/* Yay! We started up okay. Tell the world! */

	mypid=getpid();
	time(&logtime);
        fprintf(stderr,"Using %s\n",argv[1]);
 	strncpy(curtime,(ctime(&logtime)),26);
	snprintf(logbuf,80,"spumond:startup okay at %s\n",curtime);
	spulog(logbuf,80,logdir);

	/*launch the program to be monitored */
        child_pid=launch_and_watch(argv[1]);
	 while(1) {
	 	if (waitpid((pid_t)child_pid, &child_stat,0) !=0) {
	                time(&logtime);
  			strncpy(curtime,(ctime(&logtime)),26);
  			snprintf(logbuf,80,"spumond:ERROR:%s died unexpectedly at %s\n",argv[1],curtime);
                        spulog(logbuf,80,logdir);

			snprintf(logbuf,80,"spumond:Restarting %s...\n",argv[1]);
			spulog(logbuf,80,logdir);

			fprintf(stderr,"ACK! %s died unexpectedly!!\n",argv[1]);
			fprintf(stderr,"Restarting %s...\n",argv[1]);
			child_pid=launch_and_watch(argv[1]);
	 	}
	 }
}
