package com.orbswarm.swarmcon;

import java.awt.*;
import java.awt.geom.*;

import static java.lang.Math.*;

/** A place to which the orb should go */

public class Waypoint extends Point
{
      /** Time expected to reach this waypoint in seconds. */

      public double time;

      /** Expected speed  at this waypoint. */

      public double speed;

      /** Construct a default waypoint */

      public Waypoint()
      {
         super();
      }

      /** Construct a waypoint */

      public Waypoint(double x, double y, double time, double speed)
      {
         super(x, y);
         this.time = time;
         this.speed = speed;
      }

      /** Construct a Waypoint from a Point */

      public Waypoint(Point p, double time, double speed) 
      {
         this(p.x, p.y, time, speed);
      }

      /** Construct a Waypoint from a Point2D.Double */

      public Waypoint(Point2D p, double time, double speed)
      {
         this(p.getX(), p.getY(), time, speed);
      }
}
