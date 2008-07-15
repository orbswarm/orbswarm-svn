#include <sys/times.h>
#include <stdio.h>
#include <unistd.h>
#include <stdlib.h> 

void start_clock(void);
void end_clock(char *msg);

static clock_t st_time;
static clock_t en_time;
static struct tms st_cpu;
static struct tms en_cpu;

void start_clock()
{
    st_time = times(&st_cpu);
}


/* This example assumes that the result of each subtraction
   is within the range of values that can be represented in
   an integer type. */
void end_clock(char *msg)
{
    float ClockTicksPerSecond; 

    en_time = times(&en_cpu);
    fputs(msg,stdout);
    printf("Real Time: %ld, User Time %ld, System Time %ld\n",
        (unsigned long int)(en_time - st_time),
        (unsigned long int)(en_cpu.tms_utime - st_cpu.tms_utime),
        (unsigned long int)(en_cpu.tms_stime - st_cpu.tms_stime));

    ClockTicksPerSecond = (double)sysconf(_SC_CLK_TCK);
    printf("Clock Ticks Per Second %f\n",ClockTicksPerSecond);
}
