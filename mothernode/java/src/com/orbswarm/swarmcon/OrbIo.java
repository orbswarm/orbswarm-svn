package com.orbswarm.swarmcon;

import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.Properties;
import java.util.Map;
import java.text.NumberFormat;

import static java.lang.Character.*;
import static com.orbswarm.swarmcon.Message.Type.*;

/** OrbIo provids all I/O between phycical orbs and the orb objects in
 * this software.  There will be one OrbIo object for all the orbs (one
 * to rule them all).  Messages from physical orbs are dispached to the
 * correct orb object.
 */

public class OrbIo extends SerialIo
{
    /** the standard formatter for sending values. */

    public static NumberFormat StdFormat = NumberFormat.getNumberInstance();

    static
    {
      StdFormat.setMinimumIntegerDigits(1);
      StdFormat.setMaximumFractionDigits(3);
      StdFormat.setMinimumFractionDigits(3);
    }

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

    public OrbIo(String portName, final boolean debug)
    {
      super(portName, debug);
      
      // register the line listener which receives messages from the orbs

      registerLineListener(new LineListener()
        {
            public void lineEvent(String line)
            {
              Message m = new Message(line);
              dispatchOrbMessage(m);
              if (debug) 
                System.out.println("MSG: " + m);
            }
        });
    }

    public void dispatchOrbMessage(Message message)
    {
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

    /** Request survey postion. */

    public void requestSurvayPosition(int orbId)
    {
      orbCommand(orbId, "[s?]");
    }

    /** Request info report from the orb. */

    public void requestInfoReport(int orbId)
    {
      orbCommand(orbId, "[i?]");
    }

    /** Send the origin to an orb. */

    public void commandOrigin(int orbId, Point origin)
    {
      String command =
        "[o" +
        " e=" + format(origin.getX()) +
        " n=" + format(origin.getY()) +
        "]";

      orbCommand(orbId, command);
    }

    /** Send a waypoint requst to the orb. */

    public void sendWaypoint(int orbId, Waypoint wp)
    {
      String command =
        "[w" +
        " x=" + format(wp.getX()) +
        " y=" + format(wp.getY()) +
        " p=" + format(wp.getRadians()) +
        " pdot=" + format(wp.getDeltaRadians()) +
        " v=" + format(wp.getVelocity()) +
        "]";
       
      orbCommand(orbId, command);
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

    /** Format doubles for sending to an orb */

    public String format(double value)
    {
      return StdFormat.format(value);
    }

    /** The orb message class. */

    public interface IOrbListener
    {
        public void onOrbMessage(Message message);
    }
}
