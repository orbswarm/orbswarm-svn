package com.orbswarm.swarmcon.path;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;

import com.orbswarm.swarmcon.orb.Mobject;

// smooth path for printing on display

public class SmoothMobject extends Mobject
{
    private SmoothPath sp;
    private static final Ellipse2D.Double bigDot = 
      new Ellipse2D.Double(-.3, -.3, .6, .6);
    private static final Ellipse2D.Double smallDot = 
      new Ellipse2D.Double(-.1, -.1, .2, .2);

    public SmoothMobject(SmoothPath sp)
    {
      super(1);
      this.sp = sp;
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

      g.setColor(Color.BLACK);
      for (Target target: sp.getTargets())
      {
        AffineTransform t = g.getTransform();
        g.translate(target.getX(), target.getY());
        g.fill(smallDot);
        g.setTransform(t);
      }
    }
}
