package com.orbswarm.swarmcon.path;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import org.apache.log4j.Logger;
import org.trebor.util.Angle;
import org.trebor.util.Angle.Type;

/**
 * An immutable position and heading. The state can be absolute or
 * relative, and are used to position blocks.
 * 
 * @author trebor
 */

public class BlockState
{
  @SuppressWarnings("unused")
  private static Logger log = Logger.getLogger(BlockState.class);

  private final Angle mAngle;
  private final Point2D mPosition;

  public BlockState()
  {
    this(new Angle(), new Point2D.Double());
  }
  
  public BlockState(Angle angle, Point2D position)
  {
    if (null == angle)
      throw new IllegalArgumentException();
    
    if (null == position)
      throw new IllegalArgumentException();
      
    mAngle = angle;
    mPosition = position;
  }
  
  public BlockState add(BlockState other)
  {
    Angle rotAngle =
      new Angle(getAngle().as(Type.DEGREE_RATE), Type.DEGREE_RATE);

    AffineTransform t =
      AffineTransform.getRotateInstance(rotAngle.as(Type.RADIAN_RATE));

    // t.concatenate(AffineTransform.getScaleInstance(1, -1));

    Point2D rotatedDelta = new Point2D.Double();
    t.deltaTransform(other.getPosition(), rotatedDelta);

    return new BlockState(mAngle.rotate(other.getAngle()),
      new Point2D.Double(mPosition.getX() + rotatedDelta.getX(), mPosition
        .getY() +
        rotatedDelta.getY()));
  }

  public Point2D getPosition()
  {
    return mPosition;
  }

  public double getX()
  {
    return mPosition.getX();
  }
  
  public double getY()
  {
    return mPosition.getY();
  }
  
  public Angle getAngle()
  {
    return mAngle;
  }
  
  public double getAngleAs(Angle.Type type)
  {
    return mAngle.as(type);
  }

  public String toString()
  {
    return "BlockState [mAngle=" + mAngle + ", mPosition=" + mPosition + "]";
  }

  public Shape creatTransformedShape(Shape shape)
  {
    AffineTransform t = AffineTransform.getTranslateInstance(getX(), getY());
    t.rotate(getAngleAs(Type.RADIANS));
    return t.createTransformedShape(shape);
  }
}
