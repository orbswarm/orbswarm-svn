package com.orbswarm.swarmcon.view;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;

import com.orbswarm.swarmcon.path.SmoothMobject;
import com.orbswarm.swarmcon.path.SmoothPath;
import com.orbswarm.swarmcon.path.Target;
import com.orbswarm.swarmcon.path.Waypoint;
import com.orbswarm.swarmcon.vobject.IMobject;

// smooth path for printing on display

public class SmoothPathRenderer extends ARenderer<SmoothMobject>
{
  private static final Ellipse2D.Double bigDot = new Ellipse2D.Double(-.3,
    -.3, .6, .6);
  private static final Ellipse2D.Double smallDot = new Ellipse2D.Double(-.1,
    -.1, .2, .2);

  public void render(Graphics2D g, IMobject o)
  {
    SmoothPath sp = (SmoothPath)o;

    g.setColor(new Color(255, 0, 0, 16));
    for (Waypoint wp : sp)
    {
      AffineTransform t = g.getTransform();
      g.translate(wp.getX(), wp.getY());
      g.fill(bigDot);
      g.setTransform(t);
    }

    g.setColor(Color.BLACK);
    for (Target target : sp.getTargets())
    {
      AffineTransform t = g.getTransform();
      g.translate(target.getX(), target.getY());
      g.fill(smallDot);
      g.setTransform(t);
    }
  }
}
