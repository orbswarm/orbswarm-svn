package com.orbswarm.swarmcon.path;

import java.awt.geom.Point2D;

/** A place to which the orb should go */

@SuppressWarnings("serial")
public class Target extends Point2D.Double
{
  /** Expected speed at this target. */

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
