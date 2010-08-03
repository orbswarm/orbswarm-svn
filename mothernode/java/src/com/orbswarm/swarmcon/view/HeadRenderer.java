package com.orbswarm.swarmcon.view;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

import com.orbswarm.swarmcon.SwarmCon.MouseMobject;
import com.orbswarm.swarmcon.path.Head;
import com.orbswarm.swarmcon.vobject.IVobject;

import static com.orbswarm.swarmcon.Constants.ORB_DIAMETER;

public class HeadRenderer extends ARenderer<Head>
{
  private static final Color PATH_COLOR = Color.ORANGE;
  private static final double HEAD_SIZE = ORB_DIAMETER * 4;

  public IVobject getSelected(Point2D selectionPoint, MouseMobject o)
  {
    throw new UnsupportedOperationException();
  }

  public Shape getShape(Head h)
  {
    return new Ellipse2D.Double(h.getX() - HEAD_SIZE / 2, h.getY() -
      HEAD_SIZE / 2, HEAD_SIZE, HEAD_SIZE);
  }

  public void render(Graphics2D g, Head h)
  {
    g.setColor(PATH_COLOR);
    g.fill(getShape(h));
  }
}
