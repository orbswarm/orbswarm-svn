/*SPU monitoring daemon */ 
/*Launches tasks into the background and restarts them if they die           */
/* Written by 'dillo <armadilo@gothpunk.com> 6/2007 for the OrbSwarm project */
/*http://www.orbswarm.com - A large-scale kinetic art project                */

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

int launch_and_watch(char *clitask)
{
        int cli_pid;
	int fd;

        if ( (cli_pid = fork()) < 0) {
             perror ("launcher failed to fork before exec:");
             exit (-1);
        } else if (cli_pid == 0) {
		/*daemonize the child here*/
		/*Make us our own pgrp leader */
  			if (setsid() < 0) {
    				fprintf(stderr, "%s failed to daemonize because setsid (%s)\n", clitask,strerror(errno));
  			}
		/*Lose the tty (It never really did look good on us anyway) */		
			if ((fd = open("/dev/tty", O_RDWR)) >= 0) {
    				ioctl(fd, TIOCNOTTY, (char *) 0);
    				close(fd); 
  			}
		/*Close any remaining fds we got from the parent */
		/*Take a SWAG, there can't be THAT many out there */
		for (fd = 10;  fd >= 0;  fd--)
		{
			close(fd);
		}
		/*ignore STDIN*/	
  		if ((fd = open("/dev/null", O_RDWR)) < 0) 
		{
    			fprintf(stderr, "open: /dev/null (%s)\n", strerror(errno));			
  		}
		/*now that we're on our own, re-open stdio channels*/	
		dup2(fd, STDIN_FILENO);
		dup2(fd, STDOUT_FILENO);
    		dup2(fd, STDERR_FILENO);

		/*reposition to start from root */
		/*chdir("/"); */
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
		return(cli_pid);
}

int main (int argc, char **argv) {
int results,child_pid,child_stat;
struct stat statbuf;


      if (argc < 2) {
    fprintf(stderr,"spumond:please supply a file \n");
    exit(-1);
  }
  
  results = stat(argv[1], &statbuf);
   if (results < 0 ) {
    perror("stat");
    fprintf(stderr,"spumond:error trying to stat %s:\n",argv[1]);
    exit(-1);
  }
        fprintf(stderr,"Using %s\n",argv[1]);
        child_pid=launch_and_watch(argv[1]);
	 while(1) {
	 	if (waitpid((pid_t)child_pid, &child_stat,0) !=0) {
			fprintf(stderr,"ACK! %s died unexpectedly!!\n",argv[1]);
			fprintf(stderr,"Restarting %s...\n",argv[1]);
			child_pid=launch_and_watch(argv[1]);
	 	}
	 }
}
