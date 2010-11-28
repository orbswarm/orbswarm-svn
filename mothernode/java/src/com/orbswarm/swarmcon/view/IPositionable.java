package com.orbswarm.swarmcon.view;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.trebor.util.Angle;

import com.orbswarm.swarmcon.store.IPositionableAdapter;
import com.orbswarm.swarmcon.util.ISelectable;

@XmlJavaTypeAdapter(IPositionableAdapter.class)
public interface IPositionable extends ISelectable, Cloneable
{
  // get x position

  double getX();

  // get y position

  double getY();

  // position getter

  Point2D getPosition();

  // position setter

  void setPosition(Point2D position);

  // position setter

  void setPosition(double x, double y);

  // heading getter
  
  Angle getHeading();

  // set the heading
  
  void setHeading(Angle heading);
  
  /**
   * Get the transform produced by the position and heading values.
   * 
   * @return this positions transform.
   */

  AffineTransform getTransform();
  
  /**
   * Clone this {@link IPositionable}.
   * 
   * @return a clone of this this {@link IPositionable}.
   */
  
  IPositionable clone() throws CloneNotSupportedException;
}