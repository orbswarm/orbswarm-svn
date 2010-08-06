package com.orbswarm.swarmcon.path;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Rectangle2D;

import org.apache.log4j.Logger;
import org.trebor.util.Angle;

public class CurveBlock extends ABlock
{
  @SuppressWarnings("unused")
  private static Logger log = Logger.getLogger(CurveBlock.class);

  public static enum Type
  {
    LEFT, RIGHT
  }
  
  private double mRadius;
  private Type mType;
  
  public CurveBlock(IBlock previous, Angle delta, double radius, Type type)
  {
    super(previous);
    mRadius = radius;
    mType = type;
    setDeltaAngle(delta);
  }

  public void computePath()
  {
    double diameter = 2 * getRadius();
    Rectangle2D extent =
      new Rectangle2D.Double(-diameter, -getRadius(), diameter, diameter);

    double angle = getType() == Type.LEFT
      ? -getDeltaAngle().as(Angle.Type.DEGREE_RATE)
      : getDeltaAngle().as(Angle.Type.DEGREE_RATE);

    Shape shape = new Arc2D.Double(extent, 0, angle == 0 ? 360 : angle, Arc2D.OPEN);

    if (getType() == Type.RIGHT)
      shape =
        AffineTransform.getScaleInstance(-1, 1).createTransformedShape(shape);

    setPathShape(shape);
  }
  
  protected void setDeltaAngle(Angle deltaAngle)
  {
    if (getType() == Type.RIGHT)
      super.setDeltaAngle(new Angle(-deltaAngle.as(Angle.Type.DEGREES),
        Angle.Type.DEGREES));
    else
      super.setDeltaAngle(deltaAngle);
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
}
