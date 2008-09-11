package com.orbswarm.swarmcon;

import java.awt.*;
import java.awt.geom.*;

import static java.lang.Math.*;

/** A place to which the orb should go */

public class Target extends Point
{
    /** Expected speed  at this target. */

    public double speed;

    /** Construct a default target */

    public Target()
    {
      super();
    }

    /** Construct a target */

    public Target(double x, double y, double speed)
    {
      super(x, y);
      this.speed = speed;
    }

    /** Construct a Target from a Point */

    public Target(Point p, double speed)
    {
      this(p.x, p.y, speed);
    }

    /** Construct a Target from a Point2D.Double */

    public Target(Point2D p, double speed)
    {
      this(p.getX(), p.getY(), speed);
    }

    /** Construct a target, with default speed. */

    public Target(double x, double y)
    {
      super(x, y);
      this.speed = 0;
    }

    /** Construct a Target from a Point, with default speed. */

    public Target(Point p)
    {
      this(p.x, p.y);
    }

    /** Construct a Target from a Point2D.Double, with default speed. */

    public Target(Point2D p)
    {
      this(p.getX(), p.getY());
    }

    /** Getter for speed. */

    public double getSpeed()
    {
      return speed;
    }
}
