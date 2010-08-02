package com.orbswarm.swarmcon.view;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;

import com.orbswarm.swarmcon.path.SmoothPath;
import com.orbswarm.swarmcon.path.Target;
import com.orbswarm.swarmcon.path.Waypoint;

// smooth path for printing on display

public class SmoothPathRenderer extends ARenderer<SmoothPath>
{
  private static final Ellipse2D.Double bigDot = new Ellipse2D.Double(-.3,
    -.3, .6, .6);
  private static final Ellipse2D.Double smallDot = new Ellipse2D.Double(-.1,
    -.1, .2, .2);
  private static final Color SmoothPathColor = new Color(255, 0, 0, 16);
  private static final Color CurrentWaypointColor = new Color(255, 0, 0, 128);

  /** render the current active path */

  public void render(Graphics2D g, SmoothPath sp)
  {
    if (sp == null)
      return;

    // draw the path

    for (Waypoint wp : sp)
    {
      g.setColor(sp.getCurrentWaypoint() == wp
        ? CurrentWaypointColor
        : SmoothPathColor);

      AffineTransform t = g.getTransform();
      g.translate(wp.getX(), wp.getY());
      g.fill(bigDot);
      g.setTransform(t);
    }

    // draw the target points

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
