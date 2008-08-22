package com.orbswarm.swarmcon;

import java.awt.*;
import java.awt.geom.*;

import org.trebor.util.Angle;

import static java.lang.Math.*;

/** A place to which the orb should go */

public class Waypoint extends Point
{
    /** Time expected to reach this waypoint in seconds. */

    private double time = 0;

    /** Expected velocity at this waypoint. */

    private double velocity = 0;

    /** Expected angle at this waypoint. */

    private Angle angle;

    /** The delta angle in radinas per second. */

    private double deltaAngle = 0;

    /** Construct a default waypoint */

    public Waypoint()
    {
      super();
    }

    /** Construct a waypoint */

    public Waypoint(double x, double y, double time, double velocity, Angle angle)
    {
      super(x, y);
      this.time = time;
      this.velocity = velocity;
      this.angle = angle;
    }

    /** Construct a Waypoint from a Point */

    public Waypoint(Point p, double time, double velocity, Angle angle)
    {
      this(p.x, p.y, time, velocity, angle);
    }

    /** Construct a Waypoint from a Point2D.Double */

    public Waypoint(Point2D p, double time, double velocity, Angle angle)
    {
      this(p.getX(), p.getY(), time, velocity, angle);
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

    /** return the angle in radians (north = pi/2) */

    public double getRadians()
    {
      Angle tmp = new Angle(angle);
      tmp.setDeltaAngle(-90);
      return tmp.radians();
    }

    /** Return the radians per second. */

    public double getDeltaRadians()
    {
      return deltaAngle;
    }

    /** Get the yaw position of the orb in degrees. */

    public double getYaw()
    {
      return angle.degrees();
    }

    /** Return the radians per second. */

    public void setDeltaRadians(Waypoint prev, Waypoint next)
    {
      double dTime = next.getTime() - prev.getTime();
      double dRadians = next.getRadians() - prev.getRadians();
      deltaAngle = dRadians / dTime;
      //System.out.println("dTime: " + dTime);
      //System.out.println("dRadians: " + dRadians);
    }

    /** Convert to string. */

    public String toString()
    {
      return 
        "WP:[x=" + getX() + 
        " y=" + getY() + 
        " t=" + getTime() + 
        " v=" + getVelocity() + 
        " p=" + getRadians() +
        " pdot=" + getDeltaRadians() +
        "]";
    }
}
