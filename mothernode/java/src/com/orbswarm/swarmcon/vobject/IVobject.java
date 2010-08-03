package com.orbswarm.swarmcon.vobject;

import java.awt.geom.Point2D;

public interface IVobject
{
  /**
   * Is this mobject selected?
   * 
   * @return true if vobject selected
   */

  public boolean isSelected();

  /**
   * Set the selection state of this mobject.
   * 
   * @param selected selection state of mobject
   */

  public void setSelected(boolean selected);

  // position getter

  public Point2D getPosition();

  // get x position

  public double getX();

  // get y position

  public double getY();

  // position setter

  public void setPosition(Point2D position);

  // position setter

  public void setPosition(double x, double y);

  // set delta position

  public void update(double time);
}