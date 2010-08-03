package com.orbswarm.swarmcon.vobject;

import java.awt.geom.Point2D;

/**
 * A visible object in the system.
 * 
 * @author trebor
 */

public abstract class AVobject implements IVobject
{
  /** position of mobject in space */

  private final Point2D mPosition;

  /** has this mobject been selected */

  private boolean mSelected = false;

  /**
   * Create a vobject.
   */

  public AVobject()
  {
    mPosition = new Point2D.Double();
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
    return mSelected;
  }

  public void setSelected(boolean selected)
  {
    this.mSelected = selected;
  }

  // position getter

  public Point2D getPosition()
  {
    return (Point2D)mPosition.clone();
  }

  // get x position

  public double getX()
  {
    return mPosition.getX();
  }

  // get y position

  public double getY()
  {
    return mPosition.getY();
  }

  // position setter

  public void setPosition(Point2D position)
  {
    setPosition(position.getX(), position.getY());
  }

  // position setter

  public void setPosition(double x, double y)
  {
    this.mPosition.setLocation(x, y);
  }

  // update state of this object

  public abstract void update(double time);
}
