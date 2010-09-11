package com.orbswarm.swarmcon.view;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.trebor.util.Angle;

import com.orbswarm.swarmcon.store.IVobjectAdapter;
import com.orbswarm.swarmcon.util.ISelectable;

@XmlJavaTypeAdapter(IVobjectAdapter.class)
public interface IRenderable extends ISelectable
{
  // heading getter
  
  Angle getHeading();

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

  /**
   * Compute the bounds of this particular renderable.
   * 
   * @return the rectangular bounds of this renderable.
   */
  
  Rectangle2D getBounds2D();
}