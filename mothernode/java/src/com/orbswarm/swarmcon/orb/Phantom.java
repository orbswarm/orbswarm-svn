package com.orbswarm.swarmcon.orb;

import java.awt.geom.Point2D;

import com.orbswarm.swarmcon.vobject.AVobject;
import com.orbswarm.swarmcon.vobject.IVobject;

import static java.lang.Math.PI;
import static java.lang.Math.sin;

public class Phantom extends AVobject
{
  /** the amount of wind-up before moving */

  private double cheeky = 0.125;

  /** origin of phantom */

  private Point2D origin;

  /** target of phantom */

  private Point2D target;

  /** target scale */

  private double targetScale;

  /** original scale */

  private double originScale;

  /** current scale */

  private double scale;

  /** time since phantom was activated */

  private double time;

  /** the period over which the phantom should move */

  private double period;

  /** mobject that is a phantom of */

  private IVobject mobject;

  /**
   * create a phantom from a given mobject
   * 
   * @param mobject Mobject from which to create phantom
   * @param period period of time over which phantom will move
   */

  public Phantom(IVobject mobject, double period)
  {
    this.time = 0;
    this.origin = mobject.getPosition();
    this.setPosition(origin);
    this.target = origin;
    this.period = period;
    this.mobject = mobject;
    this.originScale = 1;
    this.targetScale = 1;
    this.scale = 1;
    update(0);
  }

  /**
   * Set target position and scale. This resets the time.
   * 
   * @param position place phantom will move to
   * @param scale size phantom will expand to
   */

  public void setTarget(Point2D.Double position, Double scale)
  {
    this.origin = getPosition();
    this.target = position;
    this.originScale = this.scale;
    this.targetScale = scale;
    this.time = 0;
  }

  /**
   * Is the given point (think mouse click point) eligable to select this
   * object?
   * 
   * @param clickPoint the point where the mouse was clicked
   */

  public boolean isSelectedBy(Point2D.Double clickPoint)
  {
    return false;
  }

  /**
   * Return mobject that this is a phantom of.
   * 
   * @return contained Mobject
   */

  public IVobject getMobject()
  {
    return mobject;
  }

  /**
   * Compute linear progress of phantom from origin to target.
   * 
   * @return a value from 0 to 1
   */

  public double progress()
  {
    if (time >= period)
      return 1;

    return time / period;
  }

  /**
   * Compute mapping between linear progress and smooth motion between
   * origin and target.
   * 
   * @param progress linear progress
   * @return smooth non-linear motion progress
   */

  public double motion(double progress)
  {
    return .5 + ((sin(3 * progress * PI + PI / 2) * cheeky + sin(progress *
      PI - PI / 2) *
      (1 - cheeky)) / (2 * ((1 - cheeky) - cheeky)));
  }

  /**
   * Computes dynamic scale as phantom travels to target.
   * 
   * @param progress linear progress
   * @return scale
   */

  public double scale(double progress)
  {
    return (originScale * (1 - progress)) + (progress * targetScale);
  }

  /**
   * Update the position of this phantom.
   * 
   * @param time time since last update in seconds
   */

  public void update(double time)
  {
    if (isActive())
    {
      // update time

      this.time += time;

      // compute linear progress scale and motion

      double progress = progress();
      scale = scale(progress);
      double motion = motion(progress);

      // set position based on motion progress

      setPosition(origin.getX() * (1 - motion) + target.getX() * motion,
        origin.getY() * (1 - motion) + target.getY() * motion);
    }
  }

  /**
   * Is this phantom actively moving?
   * 
   * @return true if this phantom is still expected to exist
   */

  boolean isActive()
  {
    return mobject.isSelected();
  }

  public double getScale()
  {
    return scale;
  }
}
