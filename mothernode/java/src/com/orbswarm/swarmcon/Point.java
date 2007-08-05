package com.orbswarm.swarmcon;

import java.awt.geom.Point2D;

/** A point abstraction */

public class Point extends Point2D.Double
{
      public Point()
      {
         super();
      }
      public Point(double x, double y)
      {
         super(x, y);
      }
      public Point(Point p)
      {
         super(p.getX(), p.getY());
      }
      public Point(Point2D p)
      {
         super(p.getX(), p.getY());
      }


      public void translate(Point2D delta)
      {
         translate(delta.getX(), delta.getY());
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
