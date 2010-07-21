package com.orbswarm.swarmcon.view;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;

import org.trebor.util.ShapeTools;

import com.orbswarm.swarmcon.SwarmCon;
import com.orbswarm.swarmcon.orb.IMobject;

public class MouseMobjectRenderer extends ARenderer<SwarmCon.MouseMobject>
{
  // shape to be drawn

  Shape shape = new Ellipse2D.Double(
    -SwarmCon.ORB_DIAMETER / 4, -SwarmCon.ORB_DIAMETER / 4,
    SwarmCon.ORB_DIAMETER / 2, SwarmCon.ORB_DIAMETER / 2);

  public void render(Graphics2D g, IMobject o)
  {
    g.setColor(Color.RED);
    g.fill(ShapeTools.translate(shape, o.getX(), o.getY()));
  }
}
