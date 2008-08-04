package com.orbswarm.swarmcon;

import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.Properties;
import java.util.Map;

import static java.lang.Character.*;
import static com.orbswarm.swarmcon.Message.Type.*;

/** OrbIo provids all I/O between phycical orbs and the orb objects in
 * this software.  There will be one OrbIo object for all the orbs (one
 * to rule them all).  Messages from physical orbs are dispached to the
 * correct orb object.
 */

public class OrbIo extends SerialIo
{
    // message types
    
//     public static final String GPS_MSG_TYPE   = "GPS";
//     public static final String MSLOC_MSG_TYPE = "MSLOC";
//     public static final String DUMP_MSG_TYPE  = "DUMP_STATUS";
//     public static final String TRAJ_MSG_TYPE  = "TRAJ";
    
//     // known message fields
    
//     public static final String ORB_MSG_FLD      = "orb";
//     public static final String NORTH_MSG_FLD    = "northing";
//     public static final String EAST_MSG_FLD     = "easting";
//     public static final String UTMZONE_MSG_FLD  = "utmzone";
//     public static final String VELOCITY_MSG_FLD = "velocity";
//     public static final String HEADING_MSG_FLD  = "heading";
//     public static final String TIME_MSG_FLD     = "time";


    /** Hash of orbs to used to dispatch messages to orbs. */

    HashMap<Integer, Orb> orbs = new HashMap<Integer, Orb>();

    /** Construct a OrbIo object.
     *
     * @param portName name of serial port (/dev/XXX or comX)
     */

    public OrbIo(String portName)
    {
      this(portName, false);
    }

    /** Construct a OrbIo object.
     *
     * @param portName name of serial port (/dev/XXX or comX)
     * @param debug print io debugging text
     */

    public OrbIo(String portName, boolean debug)
    {
      super(portName, debug);
      
      // register the line listener which receives messages from the orbs

      registerLineListener(new LineListener()
        {
            public void lineEvent(String line)
            {
              dispatchOrbMessage(new Message(line));
            }
        });
    }

    public void dispatchOrbMessage(Message message)
    {
      System.out.println("Orb count: " + orbs.size());
      for (Orb orb: orbs.values())
        System.out.println("Orb: " + orb);

      // if this is an unknow message type, just ignore it for now

      if (message.getType() == UNKNOWN)
        return;

      // find the orb
        
      Orb orb = orbs.get(message.getSenderId());

      // if we found an orb, then go ahead and pass the message along

      if (orb != null)
        orb.onOrbMessage(message);
      else
        System.out.println("unhandled message: " + message);
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

    /** Request position report. */

    public void requestPositionReport(int orbId)
    {
      orbCommand(orbId, "[p?]");
    }

    /** Request info report from the orb. */

    public void requestInfoReport(int orbId)
    {
      orbCommand(orbId, "[i?]");
    }

    /** Send command to orb. */

    public void orbCommand(int orbId, String command)
    {
      super.send("{" + (60 + orbId) + " " + command + "}\n");
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
              Message m = new Message(line);
              System.out.println("foo got: " + m);
            }
        };

      oio.registerLineListener(ll);

      String test = "@61 p e=123.45 n=345.67\n";

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

    /** The orb message class. */

    public interface IOrbListener
    {
        public void onOrbMessage(Message message);
    }
}
