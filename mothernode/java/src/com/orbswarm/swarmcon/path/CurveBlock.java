package com.orbswarm.swarmcon.path;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Rectangle2D;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.log4j.Logger;
import org.trebor.util.Angle;

import static java.lang.Math.PI;

@XmlRootElement(name = "curveBlock")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class CurveBlock extends ABlock
{
  @SuppressWarnings("unused")
  private static Logger log = Logger.getLogger(CurveBlock.class);

  public static enum Type
  {
    LEFT, RIGHT
  }
  
  private double mExtent;
  private double mRadius;
  private Type mType;

  public CurveBlock(double extent, double radius, Type type)
  {
    mExtent = extent;
    mRadius = radius;
    mType = type;
    computePath();
  }

  public CurveBlock(Angle extent, double radius, Type type)
  {
    this(extent.as(Angle.Type.DEGREES), radius, type);
  }

  public CurveBlock()
  {
    this(new Angle(), 1, Type.LEFT);
  }
    
  public void computePath()
  {
    double diameter = 2 * getRadius();
    
    Shape shape =
      new Arc2D.Double(new Rectangle2D.Double(-diameter, -getRadius(),
        diameter, diameter), 0, -mExtent, Arc2D.OPEN);
    
    if (getType() == Type.RIGHT)
      shape =
        AffineTransform.getScaleInstance(-1, 1).createTransformedShape(shape);

    setPathShape(shape);
  }

  @XmlElement(name="radius")
  public double getRadius()
  {
    return mRadius;
  }

  public CurveBlock setRadius(double radius)
  {
    return new CurveBlock(getExtent(), radius, getType());
  }

  
  public CurveBlock setType(Type type)
  {
    return new CurveBlock(getExtent(), getRadius(), type);
  }

  @XmlElement(name="type")
  public Type getType()
  {
    return mType;
  }

  public CurveBlock setExtent(double extent)
  {
    return new CurveBlock(extent, getRadius(), getType());
  }

  @XmlElement(name="extent")
  public double getExtent()
  {
    return mExtent;
  }
  
  public CurveBlock setLength(double length)
  {
    double dLength = length - getLength();
    double dExtent = 360 * (dLength / (2 * PI * getRadius()));
    return new CurveBlock(mExtent + dExtent, getRadius(), getType());
  }
 
  @XmlTransient
  public double getLength()
  {
    return getArcLength(getExtent(), getRadius());
  }
  
  public static double getArcLength(double extent, double radius)
  {
    return (Math.toRadians(extent) / (2 * PI)) * (radius  * 2 * PI);
  }
  
  public CurveBlock clone() throws CloneNotSupportedException
  {
    CurveBlock clone = (CurveBlock)super.clone();
    return clone;
  }
}
