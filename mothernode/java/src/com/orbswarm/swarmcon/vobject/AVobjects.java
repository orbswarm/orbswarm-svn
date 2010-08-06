package com.orbswarm.swarmcon.vobject;

import java.awt.geom.Point2D;
import java.util.Vector;

import com.orbswarm.swarmcon.path.Point;

public class AVobjects<E extends IVobject> extends Vector<E>
  implements IVobjects<E>
{
  private static final long serialVersionUID = -1347610749830598878L;

  public void update(double time)
  {
    for (E vobject: this)
      vobject.update(time);
  }

  public double distanceTo(IVobject other)
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

  public double headingTo(IVobject other)
  {
    throw new UnsupportedOperationException();
  }

  public double headingTo(Point2D point)
  {
    throw new UnsupportedOperationException();
  }

  public boolean isSelected()
  {
    throw new UnsupportedOperationException();
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
}

