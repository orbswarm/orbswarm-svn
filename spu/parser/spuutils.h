

/* stuff for getIP() */
#ifndef LOCAL
#include <sys/socket.h>
#include <net/if.h>
#include <net/ethernet.h>
#include <arpa/inet.h>
#include  <sys/ioctl.h>
#else
#warning "compiling spuutils.h for LOCAL use (not SPU)"
#endif

#define SPU_LED_RED_ON 40
#define SPU_LED_GREEN_ON 41
#define SPU_LED_BOTH_ON 42
#define SPU_LED_BOTH_OFF 43
#define SPU_LED_RED_OFF 44
#define SPU_LED_GREEN_OFF 45

int setSpuLed(const unsigned int ledState);
int getIP(const char *Interface, char *ip);
