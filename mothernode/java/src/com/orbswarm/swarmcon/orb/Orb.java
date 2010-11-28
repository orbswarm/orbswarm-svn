package com.orbswarm.swarmcon.orb;

import java.awt.Color;
import java.awt.geom.Point2D;

import java.util.Vector;
import java.util.concurrent.Delayed;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.TimeUnit;

import com.orbswarm.swarmcon.behavior.Behavior;
import com.orbswarm.swarmcon.io.Message;
import com.orbswarm.swarmcon.io.OrbIo.IOrbListener;
import com.orbswarm.swarmcon.model.IMotionModel;
import com.orbswarm.swarmcon.model.AMotionModel;
import com.orbswarm.swarmcon.view.APositionable;

import org.trebor.util.Angle;

import static java.lang.System.currentTimeMillis;
import static com.orbswarm.swarmcon.util.Constants.ORB_CLR;

import org.apache.log4j.Logger;

/** Representation of an orb. */

public class Orb extends APositionable implements IOrbListener, IOrb
{
  private static Logger log = Logger.getLogger(Orb.class);

  /** orb identifer */

  private int id;

  /** color of this orb. */

  private Color orbColor = ORB_CLR;

  /**
   * The length of the displayed history in milliseconds. This is NOT
   * properly factored.
   */

  public static long historyLength = 5000;

  /** history of were this orb has been */

  private HistoryQueue mHistory = new HistoryQueue();

  /** library of behaviors available to the orb */

  private Vector<Behavior> behaviors = new Vector<Behavior>();
  private Behavior behavior = null;

  /** physical model of the orb, either live or simulated */

  private AMotionModel model;

  // construct an orb

  public Orb(AMotionModel model, int id)
  {
    this.model = model;
    this.id = id;
  }

  // position setter

  @Override
  public void setPosition(double x, double y)
  {
    super.setPosition(x, y);
    model.setPosition(getX(), getY());

    // record our history

    mHistory.add(this);
    mHistory.removeOld();
  }

  @Override
  public void setHeading(Angle heading)
  {
    model.setYaw(heading);
  }
  
  void setOrbColor(Color val)
  {
    this.orbColor = val;
  }

  public Color getOrbColor()
  {
    if (this.orbColor == null)
    {
      return ORB_CLR;
    }
    else
    {
      return this.orbColor;
    }
  }

  public IMotionModel getModel()
  {
    return model;
  }

  public int getId()
  {
    return id;
  }

  public void handleMessage(String message)
  {
    log.debug("Message: " + message);
  }
  public void add(Behavior behavior)
  {
    behavior.setOrb(this);
    behaviors.add(behavior);
    this.behavior = behavior;
  }

  public void nextBehavior()
  {
    if (behavior != null)
    {
      behavior = behaviors.get((behaviors.indexOf(behavior) + 1) %
        behaviors.size());
    }
  }

  public Behavior getBehavior()
  {
    return behavior;
  }

  public void previousBehavior()
  {
    if (behavior != null)
    {
      behavior = behaviors.get((behaviors.indexOf(behavior) +
        behaviors.size() - 1) %
        behaviors.size());
    }
  }

  public Angle getRoll()
  {
    return model.getRoll();
  }

  public Angle getPitch()
  {
    return model.getPitch();
  }

  public Angle getYaw()
  {
    return model.getYaw();
  }

  public Angle getYawRate()
  {
    return model.getYawRate();
  }

  public double getVelocity()
  {
    return model.getVelocity();
  }

  public double getSpeed()
  {
    return model.getSpeed();
  }

  public void onOrbMessage(Message message)
  {
    model.onOrbMessage(message);
  }

  public void update(double time)
  {
    // update the vehicle behavior

    if (behavior != null)
      behavior.update(time, model);

    // update the model

    model.update(time);

    // set location to the model location

    setPosition(model.getPosition());
  }

  /** Object used to store historical information about the orb. */

  public class HistoryElement implements Delayed
  {
    /** position of orb */

    public Point2D position;

    /** the velocity of the orb */

    public double velocity;

    /** the time at which this history element was recorded */

    private long inceptTime;

    /**
     * Construct a history object.
     * 
     * @param position the position of this orb at this time
     */

    public HistoryElement(IOrb orb)
    {
      inceptTime = currentTimeMillis();
      position = orb.getPosition();
      velocity = orb.getVelocity();
    }

    /**
     * Get the remaining delay for this element.
     * 
     * @param unit the unit of time which this will report remaining delay
     *        in.
     * @return the remaining delay.
     */

    public long getDelay(TimeUnit unit)
    {
      return unit.convert(historyLength - (currentTimeMillis() - inceptTime),
        TimeUnit.MILLISECONDS);
    }

    /**
     * Get the incept time of this element
     * 
     * @return incept time of this element in milliseconds
     */

    public long getInceptTime()
    {
      return inceptTime;
    }

    /**
     * Get the position of this element
     * 
     * @return position of the orb
     */

    public Point2D getPosition()
    {
      return position;
    }

    /** Compare two history elements for sorting. */

    public int compareTo(Delayed o)
    {
      HistoryElement other = (HistoryElement)o;
      if (inceptTime < other.inceptTime)
        return -1;
      if (inceptTime > other.inceptTime)
        return 1;
      return 0;
    }
  }

  /** A storage receptacle for orb history. */

  public class HistoryQueue extends DelayQueue<HistoryElement>
  {
    HistoryElement last = null;

    /**
     * Add the orb as it is at this moment to the history.
     * 
     * @param orb the orb state to be added to the history
     */

    public void add(IOrb orb)
    {
      if (last == null ||
        ((currentTimeMillis() - last.getInceptTime()) > 100 && !last
          .getPosition().equals(getPosition())))
      {
        last = new HistoryElement(orb);
        add(last);
      }
    }

    /** Remove all timed out elements from the queue. */

    public void removeOld()
    {
      while (poll() != null)
        ;
    }
  }

  public HistoryQueue getHistory()
  {
    return mHistory;
  };
}
