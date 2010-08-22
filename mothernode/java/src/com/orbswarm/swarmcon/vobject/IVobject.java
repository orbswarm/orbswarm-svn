package com.orbswarm.swarmcon.vobject;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.trebor.util.Angle;

import com.orbswarm.swarmcon.store.IVobjectAdapter;

@XmlJavaTypeAdapter(IVobjectAdapter.class)
public interface IVobject
{
  /**
   * Is this mobject selected?
   * 
   * @return true if vobject selected
   */

  boolean isSelected();

  /**
   * Set the selection state of this mobject.
   * 
   * @param selected selection state of mobject
   */

  void setSelected(boolean selected);

  // position getter

  Point2D getPosition();

  // get x position

  double getX();

  // get y position

  double getY();

  // position setter

  void setPosition(Point2D position);

  // position setter

  void setPosition(double x, double y);

  // set the heading
  
  void setHeading(Angle heading);
  
  /**
   * Get the transform for this vobject.
   * 
   * @return this VObjects transform.
   */

  AffineTransform getTransform();
}