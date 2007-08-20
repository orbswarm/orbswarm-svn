package com.orbswarm.swarmcon;

import java.util.HashMap;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.LineNumberReader;
import java.io.InputStreamReader;
import java.text.DecimalFormat;

/** OrbIo provids all I/O between phycical orbs and the orb objects in
 * this software as well as the joystick information.  There will be one
 * OrbIo object for all the orbs (one to rule them all).  Messages from
 * physical orbs are dispached to the correct orb object and joystick
 * state is made available for orbs to read.
 */

public class OrbIo extends SerialIo
{
      /** format for printing orb id's to spu not used */

      //DeciDecimalFormat orbIdFmt = new DecimalFormat("###");
      
      /** hash of orbs to dispatch messages to */

      HashMap<Integer, Orb> orbs = new HashMap<Integer, Orb>();

      /** Construct a OrbIo object. */

      public OrbIo(String portName, boolean debug)
      {
         super(portName, debug);
      }
      public OrbIo(String portName)
      {
         super(portName);
      }
      /** open a serial port */

      public void open() throws Exception
      {
         // open serial port

         super.open();

         // create a thread to read in orb data

         new Thread()
         {
               public void run()
               {
                  try
                  {
                     LineNumberReader lnr = new LineNumberReader(
                        new InputStreamReader(in));

                     // now start working

                     while (true)
                     {
                        String line = lnr.readLine();
                        System.out.println("from orb: " + line);
                     }                        
                  }
                  catch (Exception e)
                  {
                     e.printStackTrace();
                  }
               }
         }.start();
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

      /** Send a message to the orbs.
       */

      public static final int POWER_RANGE = 25;
      public void motorCommand(int orbId, double roll, double pitch)
      {
         //super.send("{" + 60 + orbId + " drive $" + x + " " + y + "}");
      }
      /** command */

      public void driveOrb(int orbId, double x, double y)
      {
         ///super.send("{" + orbId + " xy " + x + " " + y + "}");
      }
      /** for testing */
      
      public static void main(String[] args)
      {
         OrbIo oio = new OrbIo(args[0], true);
         for (String port: oio.listSerialPorts())
            System.out.println("port: " + port);

         LineListener ll = new LineListener()
            {
                  public void lineEvent(String line)
                  {
                     System.out.println("got: " + line);
                  }
            };

         oio.registerLineListener(ll);
         
         String test = "this is a test\n";
         
         while (true)
         {
            try
            {
               oio.send(test);
               System.out.println("sent: " + test);
               java.lang.Thread.sleep(1000);
            }
            catch (Exception e)
            {
               e.printStackTrace();
            }
         }
      }
}
