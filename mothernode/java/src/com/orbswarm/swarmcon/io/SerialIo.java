package com.orbswarm.swarmcon.io;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import java.util.Vector;
import java.util.Enumeration;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.LineNumberReader;
import java.io.InputStreamReader;
import java.lang.Thread;
import org.trebor.util.JarTools;
import org.apache.log4j.Logger;


/** SerialIo provides serial I/O between this software and devices on
 * serial ports. */

public class SerialIo
{
  private static Logger log = Logger.getLogger(SerialIo.class);

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

      /** registered line listeners */

      Vector<LineListener> lineListeners = new Vector<LineListener>();


      /** load correct library */

      static
      {
         try
         {
            String libName = "rxtxSerial";
            String name = "/usr/lib/" + System.mapLibraryName(libName);

            try
            {
               log.debug("loading from jar: " + libName);
               JarTools.loadLibrary("lib", libName);
            }
            catch (java.lang.Throwable t1)
            {
               try
               {
                  log.debug("loading from system: " + libName);
                  System.loadLibrary(libName);
               }
               catch (java.lang.Throwable t2)
               {
                  try
                  {
                     log.debug("manually loading: " + name);
                     System.loadLibrary(name);
                  }
                  catch (java.lang.Throwable t3)
                  {
                     log.error("Failed to load " + libName + " from jar:");
                     t1.printStackTrace();
                     log.error("Failed to load " + libName + " from system:");
                     t2.printStackTrace();
                     log.error("Failed to load " + name + " manuall:");
                     t3.printStackTrace();
                     log.error("Ensure that the directory containing " + 
                                 System.mapLibraryName(libName) +
                                 " appears in the correct enviroment variable:");
                     log.error("   Mac:     DYLD_LIBRARY_PATH");
                     log.error("   Unix:    DYLD_LIBRARY_PATH?");
                     log.error("   Windows: PATH?");
                  }
               }
            }
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
      }

      /** Construct a SerialIo object.
       * 
       * @param portName correct name for serial port on this OS
       */

      public SerialIo(String portName)
      {
         try
         {
            if (portName != null)
            {
               portId = CommPortIdentifier.getPortIdentifier(portName);
               open();
            }
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
            throw(new Exception("Port " + portId.getName() + 
                                " is currently in use"));
         
         // open com port

         CommPort commPort = portId.open("SwarmCon", 2000);

         // if not a serial port, say so

         if (!(commPort instanceof SerialPort))
            throw(new Exception("Port " + portId.getName() + 
                                " is not a serial port"));

         // shazam your a serial port

         serialPort = (SerialPort)commPort;
         serialPort.setSerialPortParams(baud, databits, stopbits, parity);
            
         // get port streams

         in = serialPort.getInputStream();
         out = serialPort.getOutputStream();

         // activate listeners

         activateListeners();
      }
      
      /** List available serial ports.
       *
       * @return A vector of strings which are valid serial ports on this system.
       */

      public static Vector<String> listSerialPorts()
      {
         Enumeration<?> portEnum = CommPortIdentifier.getPortIdentifiers();
         Vector<String> list = new Vector<String>();
         while (portEnum.hasMoreElements())
            list.add(((CommPortIdentifier) portEnum.nextElement()).getName());
         return list;
      }

      /** for testing */
      
      public static void main(String[] args)
      {
         SerialIo sio = new SerialIo("/dev/tty.PL2303-0000201A");
         for (String port: SerialIo.listSerialPorts())
            log.debug("port: " + port);


         LineListener ll = new LineListener()
            {
                  public void lineEvent(String line)
                  {
                    log.debug("got1: " + line);
                    Message m = new Message(line);
                    log.debug("got2: " + m);
                  }
            };

         sio.registerLineListener(ll);
         
         String test = "@61 p e=123.45 n=345.67\n";
         
         while (true)
         {
            try
            {
               sio.send(test);
               log.debug("sent: " + test);
               java.lang.Thread.sleep(500);
            }
            catch (Exception e)
            {
               e.printStackTrace();
            }
         }
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

      public synchronized void send(String string)
      {
         try
         {
            if (out != null)
            {
               out.write(string.getBytes());
               out.flush();
               debugIo(string, true);
            }
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
      }

      /** Convenience sleep function with exceptions already trapped.
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

      /** Standard way to print serial debugging messages. 
       *
       * @param message string being sent or received
       * @param isOutbound true if message is headed out to serial device
       * @param beforSend true is this is before the messages is sent
       */
      
      public void debugIo(String message, boolean isOutbound)
      {
        log.debug((isOutbound 
            ? "-> "
            : "<- ") + message);
      }

      /** Activate line listening thread */

      public void activateListeners()
      {
         // construct a thread to read GPS data from
         
         new Thread()
         {
               public void run()
               {
                  try
                  {
                     LineNumberReader lnr = new LineNumberReader(
                        new InputStreamReader(in));

                     while(true)
                     {
                        String line = lnr.readLine();
                        debugIo(line, false);
                        for (LineListener l: lineListeners)
                           l.lineEvent(line);
                     }
                  }
                  catch (Exception e)
                  {
                     e.printStackTrace();
                  }
               }
         }.start();
      }
      /** Register a line listener to receive line events.
       *
       * @param lineListener listener which will receive line events
       */

      public void registerLineListener(LineListener lineListener)
      {
         lineListeners.add(lineListener);
      }

      /** Listener for line events coming from serial device */

      public static abstract class LineListener
      {
         abstract void lineEvent(String line);
      }
}
