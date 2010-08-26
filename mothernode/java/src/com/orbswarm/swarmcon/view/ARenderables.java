package com.orbswarm.swarmcon.view;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.Vector;

import org.trebor.util.Angle;

import com.orbswarm.swarmcon.path.Point;

public class ARenderables<E extends IRenderable> extends Vector<E>
  implements IRenderables<E>
{
  private static final long serialVersionUID = -1347610749830598878L;

  public double distanceTo(IRenderable other)
  {
    return getPosition().distance(other.getPosition());
  }

  public double distanceTo(Point2D point)
  {
    return getPosition().distance(point);
  }

  public Point2D getPosition()
  {
    double x = 0;
    double y = 0;
    
    for (E vobject: this)
    {
      x += vobject.getX();
      y += vobject.getY();
    }

    if (!isEmpty())
    {
      x /= size();
      y /= size();
    }
    
    return new Point(x,y);
  }

  public double getSize()
  {
    throw new UnsupportedOperationException();
  }

  public double getX()
  {
    return getPosition().getX();
  }

  public double getY()
  {
    return getPosition().getY();
  }

  public double headingTo(IRenderable other)
  {
    throw new UnsupportedOperationException();
  }

  public double headingTo(Point2D point)
  {
    throw new UnsupportedOperationException();
  }

  public boolean isSelected()
  {
    return false;
    //throw new UnsupportedOperationException();
  }

  public boolean isSelectedBy(Point2D clickPoint)
  {
    throw new UnsupportedOperationException();
  }

  public void setPosition(Point2D position)
  {
    throw new UnsupportedOperationException();
  }

  public void setPosition(double x, double y)
  {
    throw new UnsupportedOperationException();
  }

  public void setSelected(boolean selected)
  {
    for (E vobject: this)
      vobject.setSelected(selected);
  }

  public void setHeading(Angle heading)
  {
    throw new UnsupportedOperationException();
  }

  public AffineTransform getTransform()
  {
    throw new UnsupportedOperationException();
  }
}

