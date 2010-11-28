package com.orbswarm.swarmcon.view;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

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
public abstract class APositionable implements IPositionable
{
  @XmlElement(name="heading")
  private Angle mHeading;
  
  @XmlElement(name="position")
  private final Point2D mPosition;
  
  /** has this mobject been selected */

  @XmlElement(name="selected")
  private boolean mSelected = false;
  @XmlTransient
  private boolean mSuppressed;
  
  
  /**
   * Create a vobject.
   */

  public APositionable()
  {
    mHeading = new Angle(0, Angle.Type.HEADING);
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
    return mSuppressed ? false : mSelected;
  }

  public void setSelected(boolean selected)
  {
    this.mSelected = selected;
  }

  public void setSuppressed(boolean suppressed)
  {
    mSuppressed = suppressed;
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
    mPosition.setLocation(x, y);
  }

  // clone

  public IPositionable clone() throws CloneNotSupportedException
  {
    APositionable clone = (APositionable)super.clone();
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
      AffineTransform
        .getTranslateInstance(mPosition.getX(), mPosition.getY());
    t.rotate(mHeading.rotate(90, Angle.Type.HEADING_RATE).as(
      Angle.Type.RADIANS));
    return t;
  }

  public Rectangle2D getBounds2D()
  {
    throw new UnsupportedOperationException();
  }
}
