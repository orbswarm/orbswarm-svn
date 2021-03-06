// putstr.c - Utility routines for outputing data to the serial port

#include <avr/io.h>
#include "UART.h"
#include "putstr.h"

// -----------------------------------------------------------
// Send a string of chars out the UART port.
// Chrs are put into circular ring buffer.
// Routine returns immediately unless no room in Ring Buf. 
// Interrupts Transmit chrs out of ring buf.

void putstr(char *str)
{
  char ch;

  while((ch=*str)!= '\0')
  {
    UART_send_byte(ch);
    str++;
  }
}

// ----------------------------------------------------------------------



// print a single digit. 0 <= n < 10 or results wrong. 
void put1digit(unsigned char n)
{
  UART_send_byte(n + '0');
}



void putU8(unsigned char number)
{
  char value[3]={0,0,0};

  while((number - 100)>=0)
  {
    number -= 100;
    value[2]++;
  }
  value[2] += '0';

  while((number - 10)>=0)
  {
    number -= 10;
    value[1]++;
  }
  value[1] += '0';

  value[0] = number + '0';

  UART_send_byte(32);	// space
  UART_send_byte(value[2]);
  UART_send_byte(value[1]);
  UART_send_byte(value[0]);
}

// --------------------------------------------------------------------------------------------------------------------------------------------------------------------------

void putint(int number)
{
  char value[6]={0,0,0,0,0,0};

   while((number - 10000)>=0)
  {
    number -= 10000;
    value[5]++;
  }
  value[5] += '0';

  while((number - 1000)>=0)
  {
    number -= 1000;
    value[4]++;
  }
  value[4] += '0';

  while((number - 100)>=0)
  {
    number -= 100;
    value[3]++;
  }
  value[3] += '0';

  while((number - 10)>=0)
  {
    number -= 10;
    value[2]++;
  }
  value[2] += '0';

  value[1] = number + '0';
  value[0] = '\0';

  UART_send_byte(32);	// space
  UART_send_byte(value[5]);
  UART_send_byte(value[4]);
  UART_send_byte(value[3]);
  UART_send_byte(value[2]);
  UART_send_byte(value[1]);
}

// -----------------------------------------------------------------------

void putS16(short number)
{
  char value[6]={0,0,0,0,0,0};

  if(number >= 0)
  {
    value[5]='+';
  }
  else
  {
    value[5]='-';
    number *= -1;
  }

  while((number - 10000)>=0)
  {
    number -= 10000;
    value[4]++;
  }
  value[4] += '0';

  while((number - 1000)>=0)
  {
    number -= 1000;
    value[3]++;
  }
  value[3] += '0';

  while((number - 100)>=0)
  {
    number -= 100;
    value[2]++;
  }
  value[2] += '0';

  while((number - 10)>=0)
  {
    number -= 10;
    value[1]++;
  }
  value[1] += '0';

  value[0] = number + '0';

  UART_send_byte(32);	// space
  UART_send_byte(value[5]);
  UART_send_byte(value[4]);
  UART_send_byte(value[3]);
  UART_send_byte(value[2]);
  UART_send_byte(value[1]);
  UART_send_byte(value[0]);
}

// --------------------------------------------------------------------------------------------------------------------------------------------------------------------------
// End of File
