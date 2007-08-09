package com.orbswarm.swarmcon;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import java.util.Vector;
import java.util.Enumeration;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.Thread;

/** SerialIo provids serial I/O between this software and devices on
 * serial ports. */

public class SerialIo
{
      /** baud */

      int baud     = 38400;

      /** data bits */

      int databits = SerialPort.DATABITS_8;

      /** stop bits */

      int stopbits = SerialPort.STOPBITS_1;

      /** parity */

      int parity   = SerialPort.PARITY_NONE;

      /** id of serial port */

      CommPortIdentifier portId;
      
      /** serial port */

      SerialPort serialPort;
      
      /** input stream from serial port */

      InputStream in;

      /** output stream to serial port */

      OutputStream out;

      /** listener to events serial */

      SerialPortEventListener eventListener;

      /** load correct library */

      static
      {
         try
         {
            System.out.println("loading...");
            JarLibrary.load("rxtxSerial");
            System.out.println("loaded");
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
      }
      /** Construct a SerialIo object. */

      public SerialIo(String portName)
      {
         try
         {
            portId = CommPortIdentifier.getPortIdentifier(portName);
            open();
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
      }
      /** open a serial port */

      public void open() throws Exception
      {
         // if port unavailable, say so

         if (portId.isCurrentlyOwned())
            throw(new Exception("Port " + portId.getName() + " is currently in use"));
         
         // open com port

         CommPort commPort = portId.open("SwarmCon", 2000);

         // if not a serial port, say so

         if (!(commPort instanceof SerialPort))
            throw(new Exception("Port " + portId.getName() + " is not a serial port"));

         // shazam your a serial port

         serialPort = (SerialPort)commPort;
         serialPort.setSerialPortParams(baud, databits, stopbits, parity);
            
         // get port streams

         in = serialPort.getInputStream();
         out = serialPort.getOutputStream();
      }
      
      /** Add an event listener to the serial port.  I can't seem to
       * get the event listner to work, so this is likely dead code.
       */

      public void addEventListener()
      {
         try
         {
            // and add event listener
            
            eventListener = new SerialPortEventListener()
               {
                     public void serialEvent(SerialPortEvent ev)
                     {
                        System.out.println("got data!");
                        try
                        {
                           byte[] buffer = new byte[256];
                           switch (ev.getEventType())
                           {
                              case SerialPortEvent.DATA_AVAILABLE:
                                 in.read(buffer);
                                 System.out.println("got: {" + new String(buffer) + "}");
                                 break;
                              default:
                                 System.out.println("unknown event type: " + ev.getEventType());
                           }
                        }
                        catch (Exception e)
                        {
                           e.printStackTrace();
                        }
                     }
               };
            serialPort.addEventListener(eventListener);
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
      }
      /** List available ports. */

      static Vector<String> listSerialPorts()
      {
         Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();
         Vector<String> list = new Vector<String>();
         while (portEnum.hasMoreElements())
            list.add(((CommPortIdentifier) portEnum.nextElement()).getName());
         return list;
      }
      /** Map port type numbers to a string. */

      static String getPortTypeName(int portType)
      {
         switch (portType)
         {
            case CommPortIdentifier.PORT_I2C:
               return "I2C";
            case CommPortIdentifier.PORT_PARALLEL:
               return "Parallel";
            case CommPortIdentifier.PORT_RAW:
               return "Raw";
            case CommPortIdentifier.PORT_RS485:
               return "RS485";
            case CommPortIdentifier.PORT_SERIAL:
               return "Serial";
            default:
               return "unknown type";
         }
      }
      /** Send string to serial port
       *
       * @param string string to sent out to the serial port
       */

      public void send(String string)
      {
         try
         {
            out.write(string.getBytes());
            out.flush();
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
      }

      /** Convenance sleep function with exections already trapped.
       *
       * @param millis milliseconds to sleep
       */
      
      public void sleep(long millis)
      {
         try
         {
            Thread.sleep(millis);
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
      }
}
