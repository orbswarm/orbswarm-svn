package org.trebor.swarmcon;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.util.HashMap;
import java.util.Enumeration;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/** OrbIo provids all I/O between phycical orbs and the orb objects in
 * this software as well as the joystick information.  There will be one
 * OrbIo object for all the orbs (one to rule them all).  Messages from
 * physical orbs are dispached to the correct orb object and joystick
 * state is made available for orbs to read.
 */

public class OrbIo
{
         /** baud */

      public static final int BAUD     = 57600;

         /** data bits */

      public static final int DATABITS = SerialPort.DATABITS_8;

         /** stop bits */

      public static final int STOPBITS = SerialPort.STOPBITS_1;

         /** parity */

      public static final int PARITY   = SerialPort.PARITY_NONE;

         /** hash of orbs to dispatch messages to */

      HashMap<Integer, Orb> orbs = new HashMap<Integer, Orb>();

         /** id of serial port */

      private CommPortIdentifier portId;
      
         /** serial port */

      private SerialPort serialPort;
      
         /** input stream from serial port */

      private InputStream in;

         /** output stream to serial port */

      private OutputStream out;

         /** listener to events serial */

      SerialPortEventListener eventListener;

         /** load correct library */

      static
      {
         System.out.println("loading...");
         try
         {
            JarLibrary.load("rxtxSerial");
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
         System.out.println("loaded");
      }
         /** Construct a OrbIo object. */

      public OrbIo(String portName)
      {
         try
         {
               //portId = CommPortIdentifier.getPortIdentifier(portName);
               //open();
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
      }
         /** open a serial port */

      public void open() throws Exception
      {
         listPorts();

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
         serialPort.setSerialPortParams(BAUD, DATABITS, STOPBITS, PARITY);
            
            // get port streams

         in = serialPort.getInputStream();
         out = serialPort.getOutputStream();

         new Thread()
         {
               public void run()
               {
                  byte[] buffer = new byte[256];
                  try
                  {
                     while (true)
                     {
                        while (in.available() > 0)
                        {
                           in.read(buffer);
                           System.out.println("[" + new String(buffer) + "]");
                        }
                        System.out.print(".");
                        sleep(500);
                     }
                  }
                  catch (Exception e)
                  {
                  }
               }
         }.start();
         
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
         System.out.println("sucess!");
      }
         /** List available ports. */

      static void listPorts()
      {
         Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();
         while ( portEnum.hasMoreElements() ) 
         {
            CommPortIdentifier portIdentifier = (CommPortIdentifier) portEnum.nextElement();
            System.out.println(portIdentifier.getName() + " - " + getPortTypeName(portIdentifier.getPortType()) );
         }
      }
         /** Map port type numbers to a string. */

      static String getPortTypeName ( int portType )
      {
         switch ( portType )
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
         /** Register an orb as activly recieiving messages from this
          * object.
          * 
          * @param orb the orb which is to be registered
          */

      public void register(Orb orb)
      {
         orbs.put(orb.getId(), orb);
      }
         /** Signal a message to an orb.
          * 
          * @param message message to be sent to the orb
          * @param id      identifies which orb the message goes to
          */

      public void signal(String message, int id)
      {
         orbs.get(id).handleMessage(message);
      }
  }
