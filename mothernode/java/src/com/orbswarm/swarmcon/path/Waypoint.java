package com.orbswarm.swarmcon.path;

import java.awt.geom.Point2D;
import java.text.NumberFormat;

import org.trebor.util.Angle;

import static org.trebor.util.Angle.Type.DEGREE_RATE;
import static org.trebor.util.Angle.Type.HEADING;

/** A place to which the orb should go */

@SuppressWarnings("serial")
public class Waypoint extends Point2D.Double
{
  /** Time expected to reach this waypoint in seconds. */

  private double time = 0;

  /** Expected velocity at this waypoint. */

  private double velocity = 0;

  /** Expected yaw at this waypoint. */

  private Angle yaw = new Angle();

  /** The turn rate in angular units per second. */

  private Angle yawRate = new Angle();

  /** Waypoint preceding this one. */

  private Waypoint previous;

  /** Waypoint following this one. */

  private Waypoint next;

  /** Construct a default waypoint */

  public Waypoint()
  {
    super();
  }

  /** Construct a waypoint */

  public Waypoint(double x, double y, double time, double velocity, Angle yaw)
  {
    super(x, y);
    this.time = time;
    this.velocity = velocity;
    this.yaw = yaw;
  }

  /** Construct a Waypoint from a Point2D.Double */

  public Waypoint(Point2D p, double time, double velocity, Angle yaw)
  {
    this(p.getX(), p.getY(), time, velocity, yaw);
  }

  /** Velocity getter. */

  public double getVelocity()
  {
    return velocity;
  }

  /** Time getter. */

  public double getTime()
  {
    return time;
  }

  /** Return yaw rate in angular units per second. */

  public Angle getYawRate()
  {
    return yawRate;
  }

  /** Get the yaw position of the orb in degrees. */

  public Angle getYaw()
  {
    return yaw;
  }

  /** Set the next and previous waypoints */

  public void setNextPrevious(Waypoint next, Waypoint previous)
  {
    this.next = next;
    this.previous = previous;
    setYawRate();
  }

  /** Get next waypoint. */

  public Waypoint getNext()
  {
    return next;
  }

  /** Get previous waypoint. */

  public Waypoint getPrevious()
  {
    return previous;
  }

  /** Return the radians per second. */

  protected void setYawRate()
  {
    if (next == null)
      yawRate = new Angle(0, DEGREE_RATE);
    else
    {
      double dTime = next.getTime() - getTime();
      yawRate = new Angle(Angle.difference(getYaw(), next.getYaw()).as(
        DEGREE_RATE) /
        dTime, DEGREE_RATE);
    }
  }

  public static NumberFormat NumFmt = NumberFormat.getNumberInstance();

  /** static initializations */

  static
  {
    // NumFmt.setMaximumIntegerDigits();
    NumFmt.setMinimumIntegerDigits(3);
    NumFmt.setMinimumFractionDigits(2);
    NumFmt.setMaximumFractionDigits(5);
  }

  /** Convert to string. */

  public String toString()
  {
    return "WP:[x=" + NumFmt.format(getX()) + " y=" + NumFmt.format(getY()) +
      " t=" + NumFmt.format(getTime()) + " v=" +
      NumFmt.format(getVelocity()) + " yaw=" +
      NumFmt.format(getYaw().as(HEADING)) + " dyaw=" +
      NumFmt.format(getYawRate().as(DEGREE_RATE)) + "]";
  }
}
