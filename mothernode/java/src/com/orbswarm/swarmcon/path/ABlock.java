package com.orbswarm.swarmcon.path;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;

import org.trebor.util.Angle;
import org.trebor.util.Angle.Type;

import com.orbswarm.swarmcon.vobject.AVobject;

public abstract class ABlock extends AVobject implements IBlock
{
  private IBlock mPrevious;
  protected Angle mDeltaAngle;
  private Shape mPathShape;
  
  public ABlock()
  {
    setPreviouse(null);
  }
  
  public ABlock(IBlock previous)
  {
    if (null == previous)
      throw new IllegalArgumentException();
    setPreviouse(previous);
  }
  
  public void update(double time)
  {
  }

  public Point2D getEndPosition()
  {
    PathIterator pi = getPath().getPathIterator(null);
    double[] coords = new double[6];
    int type = PathIterator.SEG_CLOSE;

    while (!pi.isDone())
    {
      type = pi.currentSegment(coords);
      pi.next();
    }

    double x = 0;
    double y = 0;

    switch (type)
    {
    case PathIterator.SEG_MOVETO:
    case PathIterator.SEG_LINETO:
      x = coords[0];
      y = coords[1];
      break;
    case PathIterator.SEG_QUADTO:
      x = coords[2];
      y = coords[3];
      break;
    case PathIterator.SEG_CUBICTO:
      x = coords[4];
      y = coords[5];
      break;
    default:
      throw new Error();
    }
    
    return new Point2D.Double(x, y);
  }

  public Angle getEndAngle()
  {
    Angle startAngle = getPrevious() != null
      ? getPrevious().getEndAngle()
      : new Angle();

    return startAngle.rotate(mDeltaAngle);
  }

  public IBlock getPrevious()
  {
    return mPrevious;
  }

  public void setPreviouse(IBlock previous)
  {
    mPrevious = previous;
  }

  protected void setDeltaAngle(Angle deltaAngle)
  {
    mDeltaAngle = deltaAngle;
    computePath();
  }

  protected Angle getDeltaAngle()
  {
    return mDeltaAngle;
  }

  public Point2D getPosition()
  {
    if (getPrevious() != null)
      return getPrevious().getEndPosition();
    return super.getPosition();
  }

  public void setPosition(Point2D position)
  {
    if (getPrevious() != null)
      throw new IllegalArgumentException();
    super.setPosition(position);
  }

  protected void setPathShape(Shape segmentShape)
  {
    mPathShape = segmentShape;
  }

  protected Shape getPathShape()
  {
    return mPathShape;
  }
  
  public Shape getPath()
  {
    Shape shape = mPathShape;
    IBlock previous = getPrevious();
    Point2D startPoint = new Point2D.Double();
    Angle startAngle = new Angle();

    if (null != previous)
    {
      startPoint = previous.getEndPosition();
      startAngle = previous.getEndAngle();
    }

    // rotate the curve the correct amount

    shape =
      AffineTransform.getRotateInstance(startAngle.as(Type.RADIANS))
        .createTransformedShape(shape);

    // translate the curve to the correct location

    shape =
      AffineTransform.getTranslateInstance(startPoint.getX(),
        startPoint.getY()).createTransformedShape(shape);

    // return the path

    return shape;
  }
}
