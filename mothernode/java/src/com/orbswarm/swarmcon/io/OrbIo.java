package com.orbswarm.swarmcon.io;

import java.awt.geom.Point2D;
import java.util.HashMap;
import org.apache.log4j.Logger;

import com.orbswarm.swarmcon.orb.Orb;
import com.orbswarm.swarmcon.path.Waypoint;
import com.orbswarm.swarmcon.util.Constants;

import static org.trebor.util.Angle.Type.RADIANS;
import static org.trebor.util.Angle.Type.RADIAN_RATE;
import static com.orbswarm.swarmcon.io.Message.Type.*;

/**
 * OrbIo provides all I/O between physical orbs and the orb objects in this
 * software. There will be one OrbIo object for all the orbs (one to rule
 * them all). Messages from physical orbs are dispatched to the correct orb
 * object.
 */

public class OrbIo
{
  SerialIo mIo;

  private static Logger log = Logger.getLogger(OrbIo.class);

  /** Hash of orbs to used to dispatch messages to orbs. */

  HashMap<Integer, Orb> orbs = new HashMap<Integer, Orb>();

  /**
   * Construct a OrbIo object.
   * 
   * @param portName name of serial port (/dev/XXX or comX)
   */

  public OrbIo(String portName)
  {
    mIo = new SerialIo(portName);

    // register the line listener which receives messages from the orbs

    mIo.registerLineListener(new SerialIo.LineListener()
    {
      public void lineEvent(String line)
      {
        Message m = new Message(line);
        dispatchOrbMessage(m);
        log.debug("MSG: " + m);
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
      log.debug("unhandled message: " + message);
  }

  /** open a serial port */

  public void open() throws Exception
  {
    // open serial port

    mIo.open();
  }

  /**
   * Register an orb as activly recieiving messages from this object.
   * 
   * @param orb the orb which is to be registered
   */

  public void register(Orb orb)
  {
    orbs.put(orb.getId(), orb);
  }

  /**
   * Signal a message to an orb.
   * 
   * @param message message to be sent to the orb
   * @param id identifies which orb the message goes to
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

  public void commandOrigin(int orbId, Point2D origin)
  {
    String command = "[o" + " e=" + format(origin.getX()) + " n=" +
      format(origin.getY()) + "]";

    orbCommand(orbId, command);
  }

  /** Send a waypoint requst to the orb. */

  public void sendWaypoint(int orbId, Waypoint wp)
  {
    String command = "[w" + " x=" + format(wp.getX()) + " y=" +
      format(wp.getY()) + " p=" + format(wp.getYaw().as(RADIANS)) + " pdot=" +
      format(wp.getYawRate().as(RADIAN_RATE)) + " v=" +
      format(wp.getVelocity()) + "]";

    orbCommand(orbId, command);
  }

  /** Send command to orb. */

  public void orbCommand(int orbId, String command)
  {
    mIo.send("{" + (60 + orbId) + " " + command + "}");
  }

  /** for testing */

  public static void main(String[] args)
  {
    OrbIo oio = new OrbIo(args[0]);
    for (String port : SerialIo.listSerialPorts())
      log.debug("port: " + port);

    SerialIo.LineListener ll = new SerialIo.LineListener()
    {
      public void lineEvent(String line)
      {
        Message m = new Message(line);
        log.debug("foo got: " + m);
      }
    };

    oio.mIo.registerLineListener(ll);

    String test = "@61 p e=123.45 n=345.67\n";

    while (true)
    {
      try
      {
        oio.mIo.send(test);
        log.debug("sent: " + test);
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
    return Constants.UTM_FORMAT.format(value);
  }

  public void send(String message)
  {
    mIo.send(message);
  }

  /** The orb message class. */

  public interface IOrbListener
  {
    public void onOrbMessage(Message message);
  }
}
