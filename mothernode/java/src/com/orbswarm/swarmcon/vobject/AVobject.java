package com.orbswarm.swarmcon.vobject;

import java.awt.geom.Point2D;

import com.orbswarm.swarmcon.path.Point;

import static java.lang.Math.atan2;
import static java.lang.Math.toDegrees;

/**
 * A visible object in the system.
 * 
 * @author trebor
 */

public abstract class AVobject implements IVobject
{
  /** position of mobject in space */

  private Point position = new Point();

  /** has this mobject been selected */

  private boolean selected = false;

  /**
   * Create a vobject.
   */

  public AVobject()
  {
  }

  /**
   * Create a mobject.
   * 
   * @param shape the shape of the object use for selection and to compute
   *        arrangement of object
   */

  /*
   * (non-Javadoc)
   * @see com.orbswarm.swarmcon.orb.IMobject#isSelected()
   */

  public boolean isSelected()
  {
    return selected;
  }

  public void setSelected(boolean selected)
  {
    this.selected = selected;
  }

  // position getter

  public Point getPosition()
  {
    return new Point(getX(), getY());
  }

  // get x position

  public double getX()
  {
    return position.getX();
  }

  // get y position

  public double getY()
  {
    return position.getY();
  }

  // position setter

  public void setPosition(Point2D.Double position)
  {
    setPosition(position.getX(), position.getY());
  }

  // position setter

  public void setPosition(double x, double y)
  {
    this.position.setLocation(x, y);
  }

  // set delta position

  void deltaPosition(double dX, double dY)
  {
    setPosition(getX() + dX, getY() + dY);
  }

  // update state of this object

  public abstract void update(double time);

  // compute heading to some point

  public double headingTo(IVobject other)
  {
    return headingTo(other.getPosition());
  }

  // compute heading to some point

  public double headingTo(Point2D.Double point)
  {
    double dx = point.getX() - getPosition().getX();
    double dy = point.getY() - getPosition().getY();
    double angle = atan2(dx, dy);
    return toDegrees(angle) + (dx < 0
      ? 360
      : 0);
  }

  // compute distance to some point

  public double distanceTo(IVobject other)
  {
    return distanceTo(other.getPosition());
  }

  // compute distance to some point

  public double distanceTo(Point2D.Double point)
  {
    return getPosition().distance(point);
  }
}
