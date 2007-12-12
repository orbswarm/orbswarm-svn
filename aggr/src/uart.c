#include <avr/io.h>		// include I/O definitions (port names, pin names, etc)
#include <avr/interrupt.h>	// include interrupt support
#define UBRR_VAL 23
#include "include/uart.h"


/*  
 * atmega640 UART notes. The 640 has three status and control registers for 
 * each USART. They are -
 * 
 * UCSRnA: This is primarily a status register 
 * 		#Bit7- RXCn: This bit is set whenever there is unread data in the receive buffer and 
 * 		cleared when the buffer is empty(i.e. read). Will generate interrupts when the 
 * 		RXCIEn bit is set.
 * 		#Bit6-TXCn: This bit is set when the entire Frame has been shifted out of the
 * 		transmit shift register and there is no new data in the transmit buffer(UDRn).
 * 		This flag is automatically cleared when a transmit complete interrupt is raised. 
 *		If used in a poll mode(no interrupt) the flag must be cleared before each transmit
 * 		i.e. the flag is NOT automatically cleared by writing to the UDRn buffer only.
 * 		#Bit5-UDREn: This bit is set when the transmit buffer(UDRn) is empty and after each reset.
 * 		This flag can be cleared by writing to the UDRn buffer. This bit will also
 * 		generate interrupts if the UDRIE flag is set.
 * 		#Bits 4,3,2 -FEn,DORn and UPEn : These are error flags and cannot be written to from code.
 * 		The FEn flag is set to 0 if the first stop bit is correctly read and is set to 1 
 * 		if incorrectly read. The DORn flag is set if both the shift register and receive 
 * 		buffer are full and a new start bit is received. This indicates that a frame 
 * 		was lost between the last frame read from the UDRn and the next frame to be read 
 * 		from the UDRn. The DORn flag is cleared when a frame is successfully moved
 * 		from the shift register to the receive buffer. 
 * 		#Bit 1- U2Xn
 * 		#Bit 0- MPCMn
 *
 * UCSRnB: This is primarily as control register
 * 		#B7- RXCIEn
 * 		#B6- TXCIEn
 * 		#B5- UDRIEn
 * 		#B4- RXENn
 * 		#B3- TXENn
 * 		#B2,1,0- UCSZn2, RXB8n, TXB8n
 * 
 * UCSRnC: This is the second control register
 * 		#B7,6- UMSELn1:0 : The value of 0,0 corresponds to async UART
 * 		#B5,4- UPMn1:0 : The value 0,0 corresponds to no parity
 * 		#B3- USBSn : The value 0 for one stop bit
 *      #B2,1- UCSZn1:0 : Charcter size. For 8 bits, UCSZn2=0, UCSZn1=1, UCSZn0=1	  
 * 		#B0- UCPOLn
 * */
static void (*  _handleXBeeRecv)(char, int) ;
static void (*  _handleSpuRecv)(char, int) ;
static void (* _handleGpsARecv)(char, int) ;
static char (* _getXBeeOutChar)(void);
static char (* _getSpuOutChar)(void);
//static char (* _getSpuGpsOutChar)(void);
//static char (* _currentSpuGetter)(void);
volatile static int s_isSpuSendInProgress=0;
volatile static int s_isXbeeSendInProgress=0;

////////////////////RXCIE interrupts
ISR(SIG_USART3_RECV)
{
  int nErrors=0;
  if((UCSR3A & (1<<FE3)) || 
   (UCSR3A & (1<<DOR3))) 
     { 
       nErrors=1; 
     }
  (*_handleXBeeRecv)(UDR3, nErrors);
}


ISR(SIG_USART0_RECV)
{
  int nErrors=0;
  if((UCSR0A & (1<<FE0)) || 
   (UCSR0A & (1<<DOR0))) 
     { 
       nErrors=1; 
     }
  (*_handleSpuRecv)(UDR0, nErrors);
}

ISR(SIG_USART1_RECV)
{
 
  int nErrors=0;
  if((UCSR1A & (1<<FE1)) || 
   (UCSR1A & (1<<DOR1))) 
     { 
       nErrors=1; 
     }
  (*_handleGpsARecv)(UDR1, nErrors);
  
}

////////////////////UDRIE interrupt handlers
ISR(SIG_USART3_DATA)
{
  char c = (*_getXBeeOutChar)();
  if(c !=0 ){
    UDR3 = c;
  }
  else{//end of q
    UCSR3B &= ~(1 << UDRIE3);//turn off interrupt
    s_isXbeeSendInProgress=0;
  }

}

ISR(SIG_USART0_DATA)
{
  char c = (*_getSpuOutChar)();
  if(c !=0 ){
    UDR0 = c;
  }
  else{//end of q
    UCSR0B &= ~(1 << UDRIE0);//turn off interrupt
    s_isSpuSendInProgress=0;
  }
}

//////////////Polling(synchronous) senders
void sendGPSAMsg(const char *s)
{
	//No need to disable/enable UDRIE interrupts here because there is no
	//asynch sender for GPSA
  	while(*s)
    {
		while(!(UCSR1A & (1<<UDRE1)))
			;
      	UDR1 = *(s++);
    }
}

void sendSpuMsg(const char *s)
{
  	while(*s)
    {
      	UDR0 = *(s++);
		while(!(UCSR0A & (1<<UDRE0)))
			;
    }
}

void debug(const char *s)
{
  sendSpuMsg(s);
}

void startAsyncXBeeTransmit(void)
{
  if(!s_isXbeeSendInProgress){
	while(!(UCSR3A & (1<<UDRE3)))
		;
    UCSR3B |=  (1 <<UDRIE3);//enable interrupt
    s_isXbeeSendInProgress=1;
  }
}

void stopAsyncXbeeTransmit(void)
{
	if(s_isXbeeSendInProgress){
		UCSR3B &= ~(1 << UDRIE3);//disable interrupt
		//wait for existing frame to be sent
		while(!(UCSR3A & (1<<UDRE3)))
			;
		s_isXbeeSendInProgress=0;
	}
}

void startAsyncSpuTransmit(void)
{
  if(!s_isSpuSendInProgress){
  	//wait for existing frame to be sent
	while(!(UCSR0A & (1<<UDRE0)))
		;
  	UCSR0B |=  (1 <<UDRIE0);//enable interrupt
  	s_isSpuSendInProgress=1;
    }
}

void stopAsyncSpuTransmit(void)
{
	if(s_isSpuSendInProgress){
		UCSR0B &= ~(1 << UDRIE0);//disable interrupt
		//wait for existing frame to be sent
		while(!(UCSR0A & (1<<UDRE0)))
			;
		s_isSpuSendInProgress=0;
	}
}

int isSpuSendInProgress(void)
{
  return s_isSpuSendInProgress;
}


int isXBeeSendInProgress(void)
{
  return s_isXbeeSendInProgress;
}


int uart_init( void (*handleXBeeRecv)(char c, int isError),
	       void (*handleSpuRecv)(char c, int isErrror),
	       void (*handleGpsARecv)(char c, int isErrror),
	       char (*getXBeeOutChar)(void),
	       char (*getSpuOutChar)(void))
{
  //Set up XBee on USART3
  //Asynchronous UART, no parity, 1 stop bit, 8 data bits, 38400 baud
  UCSR3B = (1<<RXCIE3) | (1<<RXEN3) | (1<<TXEN3) ;
  UCSR3C = (1<<UCSZ31) | (1<< UCSZ30);
  UBRR3 = UBRR_VAL;
  _handleXBeeRecv=handleXBeeRecv;
  _getXBeeOutChar=getXBeeOutChar;

  //Set up SPU on USART0
  //Asynchronous UART, no parity, 1 stop bit, 8 data bits, 38400 baud
  UCSR0B = (1<<RXCIE0) | (1<<RXEN0) | (1<<TXEN0);
  UCSR0C = (1<<UCSZ01) | (1<< UCSZ00);
  UBRR0 = UBRR_VAL;
  _handleSpuRecv  = handleSpuRecv;
  _getSpuOutChar=getSpuOutChar;
  
  //Set up GPSA
  UCSR1B = (1<<RXCIE1) | (1<<RXEN1) | (1<<TXEN1);
  UCSR1C = (1<<UCSZ11) | (1<< UCSZ10);
  UBRR1 = UBRR_VAL;
  _handleGpsARecv = handleGpsARecv;
//  _getSpuGpsOutChar = getSpuGpsOutChar;

  return 0;
}
