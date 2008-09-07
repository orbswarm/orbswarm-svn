package com.orbswarm.swarmcon;

import java.awt.*;
import java.awt.geom.*;

import static java.lang.Math.*;
import static org.trebor.util.ShapeTools.*;
import static java.awt.BasicStroke.*;

// smooth path for printing on display

public class SmoothMobject extends Mobject
{
    private SmoothPath sp;
    private GeneralPath gp;
    private static final Stroke stroke = 
      new BasicStroke(1.5f, CAP_ROUND, JOIN_ROUND); 
    private static final Ellipse2D.Double bigDot = 
      new Ellipse2D.Double(-.3, -.3, .6, .6);
    private static final Ellipse2D.Double smallDot = 
      new Ellipse2D.Double(-.1, -.1, .2, .2);

    private Waypoint current = null;

    public SmoothMobject(SmoothPath sp)
    {
      super(1);
      this.sp = sp;
      this.gp = sp.getContinousePath();
      System.out.println("smooth count: " + sp.size());
    }

    public void setCurrentWaypoint(Waypoint current)
    {
      this.current = current;
    }

    // paint this object onto a graphics area
    
    public void paint(Graphics2D g)
    {
      g.setColor(new Color(255, 0, 0, 16));
      for (Waypoint wp: sp)
      {
        AffineTransform t = g.getTransform();
        g.translate(wp.getX(), wp.getY());
        g.fill(bigDot);
        g.setTransform(t);
      }

//       g.setColor(new Color(64, 64, 64));
//       for (Waypoint wp: sp)
//       {
//         AffineTransform t = g.getTransform();
//         g.translate(wp.getX(), wp.getY());
//         g.fill(smallDot);
//         g.setTransform(t);
//         if (wp == current)
//           break;
//       }
    }
}
