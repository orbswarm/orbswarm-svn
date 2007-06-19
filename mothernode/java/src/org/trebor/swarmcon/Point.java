package org.trebor.swarmcon;

/** A point abstraction */

public class Point extends java.awt.geom.Point2D.Double
{
      public Point(double x, double y)
      {
         super(x, y);
      }
      public Point(Point p)
      {
         super(p.getX(), p.getY());
      }


      public void translate(Point delta)
      {
         translate(delta.getX(), delta.getY());
      }

      public void translate(double dX, double dY)
      {
         setLocation(getX() + dX, getY() + dY);
      }
}
