package com.orbswarm.swarmcon;

import java.util.HashMap;

/** OrbIo provids all I/O between phycical orbs and the orb objects in
 * this software.  There will be one OrbIo object for all the orbs (one
 * to rule them all).  Messages from physical orbs are dispached to the
 * correct orb object.
 */

public class OrbIo extends SerialIo
{
      /** Hash of orbs to used to dispatch messages to orbs. */

      HashMap<Integer, Orb> orbs = new HashMap<Integer, Orb>();

      /** Construct a OrbIo object. 
       *
       * @param portName name of serial port (/dev/XXX or comX)
       * @param debug print io debugging text
       */

      public OrbIo(String portName, boolean debug)
      {
         super(portName, debug);
      }

      public OrbIo(String portName)
      {
         this(portName, false);
      }
      /** open a serial port */

      public void open() throws Exception
      {
         // open serial port

         super.open();
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

      /** Send a steering message to orb. */

      public void steerOrb(int orbId, int roll)
      {
         orbMotorCommand(orbId, "s" + roll);
      }

      /** Send power command to orb. */

      public void powerOrb(int orbId, int power)
      {
         orbMotorCommand(orbId, "p" + power);
      }

      /** Send motor command to orb */

      public void orbMotorCommand(int orbId, String motorCommand)
      {
         orbCommand(orbId, "$" + motorCommand + "*");
      }

      /** Send command to orb. */

      public void orbCommand(int orbId, String command)
      {
         super.send("{" + (60 + orbId) + " " + command + "}");
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
