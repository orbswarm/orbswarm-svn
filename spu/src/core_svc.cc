#include  <stdio.h>    /* Standard input/output definitions */
#include  <unistd.h>
#include  <sys/ioctl.h>
#include  <sys/types.h>
#include  <sys/time.h>
#include  <sys/select.h>
#include "../include/swarmserial.h"
#include "../include/swarmdefines.h"
/*dummy core svc to monitor */
/*Should be replaced by the real core service*/

int main() {
int com2;
char serbuf[80];
int serbuflen = 0;
	 sprintf(serbuf,"Hi, I'm a core service\n");
	 serbuflen = strlen(serbuf);
	 serbuf[serbuflen+1] = '\0';	
	 com2 = initSerialPort("/dev/ttyAM1", 9600);
	for(;;) {
		writeCharsToSerialPort(com2, serbuf, serbuflen);	
	}
}
