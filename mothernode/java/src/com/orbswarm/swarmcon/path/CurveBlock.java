package com.orbswarm.swarmcon.path;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Rectangle2D;

import javax.xml.bind.annotation.XmlTransient;

import org.apache.log4j.Logger;
import org.trebor.util.Angle;

import static java.lang.Math.PI;

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

  public double getRadius()
  {
    return mRadius;
  }

  public void setRadius(double radius)
  {
    mRadius = radius;
    computePath();
  }

  public void setLength(double length)
  {
    double dLength = length - getLength();
    double dExtent = 360 * (dLength / (2 * PI * getRadius()));
    mExtent += dExtent;
    computePath();
  }
  
  public void setType(Type type)
  {
    mType = type;
    computePath();
  }

  public Type getType()
  {
    return mType;
  }

  public void setExtent(double extent)
  {
    mExtent = extent;
    computePath();
  }

  public double getExtent()
  {
    return mExtent;
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

}
