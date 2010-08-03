package com.orbswarm.swarmcon.path;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Rectangle2D;

import org.trebor.util.Angle;

public class Curve extends ABlock
{
  public static enum Type
  {
    LEFT, RIGHT
  }
  
  private double mRadius;
  private Type mType;
  
  public Curve(IBlock previous, Angle delta, double radius, Type type)
  {
    super(previous);
    setDeltaAngle(delta);
    setRadius(radius);

    double diameter = 2 * radius;
    Rectangle2D extent = new Rectangle2D.Double(-diameter, -radius, diameter,
      diameter);

    Shape shape = new Arc2D.Double(extent, 0, -delta.as(Angle.Type.DEGREES),
      Arc2D.OPEN);
    
    if (type == Type.RIGHT)
      shape = AffineTransform.getScaleInstance(-1, 1).createTransformedShape(shape);

    setSegmentShape(shape);
  }

  private void computeCurveShape()
  {
    
  }
  
  public double getRadius()
  {
    return mRadius;
  }


  public void setRadius(double radius)
  {
    mRadius = radius;
    computeCurveShape();
  }

  public void setType(Type type)
  {
    mType = type;
    computeCurveShape();
  }

  public Type getType()
  {
    return mType;
  }
}
