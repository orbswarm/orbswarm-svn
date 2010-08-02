package com.orbswarm.swarmcon.vobject;

import java.awt.geom.Point2D;

import com.orbswarm.swarmcon.path.Point;

public interface IVobject
{

  /**
   * Is this mobject selected?
   * 
   * @return true if object selected
   */

  public boolean isSelected();

  /**
   * Set the selection state of this mobject.
   * 
   * @param selected selection state of mobject
   */

  public void setSelected(boolean selected);

  /**
   * Get the size of this mobject.
   * 
   * @return the nominal of this mobject
   */

//  public double getSize();

  // position getter

  public Point getPosition();

  // get x position

  public double getX();

  // get y position

  public double getY();

  // position setter

  public void setPosition(Point2D.Double position);

  // position setter

  public void setPosition(double x, double y);

  // set delta position

  public void update(double time);

  // compute heading to some point

  public double headingTo(IVobject other);

  // compute heading to some point

  public double headingTo(Point2D.Double point);

  // compute distance to some point

  public double distanceTo(IVobject other);

  // compute distance to some point

  public double distanceTo(Point2D.Double point);
}