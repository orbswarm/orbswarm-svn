package com.orbswarm.swarmcon.path;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Rectangle2D;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.log4j.Logger;
import org.trebor.util.Angle;

import static java.lang.Math.PI;

@XmlRootElement(name = "curveBlock")
@XmlAccessorType(XmlAccessType.FIELD)
public class CurveBlock extends ABlock
{
  @SuppressWarnings("unused")
  private static Logger log = Logger.getLogger(CurveBlock.class);

  public static enum Type
  {
    LEFT, RIGHT
  }
  
  @XmlElement(name="extent")
  private double mExtent;
  @XmlElement(name="radius")
  private double mRadius;
  @XmlElement(name="type")
  private Type mType;

  public CurveBlock(double extent, double radius, Type type)
  {
    mExtent = extent;
    mRadius = radius;
    mType = type;
  }

  public CurveBlock(Angle extent, double radius, Type type)
  {
    this(extent.as(Angle.Type.DEGREES), radius, type);
  }

  public CurveBlock()
  {
    this(new Angle(), 1, Type.LEFT);
  }
    
  public Shape computePath()
  {
    double diameter = 2 * getRadius();
    
    Shape shape =
      new Arc2D.Double(new Rectangle2D.Double(-diameter, -getRadius(),
        diameter, diameter), 0, -mExtent, Arc2D.OPEN);
    
    if (getType() == Type.RIGHT)
      shape =
        AffineTransform.getScaleInstance(-1, 1).createTransformedShape(shape);

    return shape;
  }

  public double getRadius()
  {
    return mRadius;
  }

  public CurveBlock setRadius(double radius)
  {
    return new CurveBlock(mExtent, radius, mType);
  }

  public Type getType()
  {
    return mType;
  }
  
  public CurveBlock setType(Type type)
  {
    return new CurveBlock(mExtent, mRadius, type);
  }

  public double getExtent()
  {
    return mExtent;
  }
  
  public CurveBlock setExtent(double extent)
  {
    return new CurveBlock(extent, mRadius, mType);
  }

  public CurveBlock setLength(double length)
  {
    double dLength = length - getLength();
    double dExtent = 360 * (dLength / (2 * PI * getRadius()));
    return new CurveBlock(getExtent() + dExtent, mRadius, mType);
  }
 
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

  public String toString()
  {
    return "CurveBlock [mExtent=" + mExtent + ", mRadius=" + mRadius +
      ", mType=" + mType + "]";
  }
}
