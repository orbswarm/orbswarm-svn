package com.orbswarm.swarmcon.vobject;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

import org.trebor.util.Angle;

/**
 * A visible object in the system.
 * 
 * @author trebor
 */

@XmlAccessorType(XmlAccessType.FIELD)
public abstract class AVobject implements IVobject
{
  @XmlElement(name="heading")
  private Angle mHeading;
  
  @XmlElement(name="position")
  private final Point2D mPosition;
  
  /** has this mobject been selected */

  @XmlTransient
  private boolean mSelected = false;

  /**
   * Create a vobject.
   */

  public AVobject()
  {
    mHeading = new Angle();
    mPosition = new Point2D.Double();
  }

  /**
   * Create a mobject.
   * 
   * @param shape the shape of the object use for selection and to compute
   *        arrangement of object
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
    return mPosition;
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
    mPosition.setLocation(x, y);
  }

  // clone
  
  public IVobject clone() throws CloneNotSupportedException
  {
    AVobject clone = (AVobject)super.clone();
    clone.mHeading = mHeading;
    clone.mPosition.setLocation(mPosition);
    return clone;
  }

  public void setHeading(Angle heading)
  {
    mHeading = heading;
  }
  
  public Angle getHeading()
  {
    return mHeading;
  }
  
  public AffineTransform getTransform()
  {
    AffineTransform t =
      AffineTransform.getRotateInstance(mHeading.as(Angle.Type.RADIANS));
    t.translate(mPosition.getX(), mPosition.getY());
    return t;
  }
}
