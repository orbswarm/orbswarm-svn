package com.orbswarm.swarmcon.path;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Rectangle2D;

import org.apache.log4j.Logger;

import com.sun.xml.internal.txw2.annotation.XmlElement;

@XmlElement
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
}
