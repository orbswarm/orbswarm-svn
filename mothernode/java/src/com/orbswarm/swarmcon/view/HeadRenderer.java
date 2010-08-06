package com.orbswarm.swarmcon.view;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

import com.orbswarm.swarmcon.SwarmCon.MouseMobject;
import com.orbswarm.swarmcon.path.Head;
import com.orbswarm.swarmcon.vobject.IVobject;

public class HeadRenderer extends ARenderer<Head>
{
  public IVobject getSelected(Point2D selectionPoint, MouseMobject o)
  {
    throw new UnsupportedOperationException();
  }

  public Shape getShape(Head h)
  {
    double headSize = RenderingConstants.HEAD_WIDTH;
    return new Ellipse2D.Double(h.getX() - headSize / 2, h.getY() - headSize /
      2, headSize, headSize);
  }

  public void render(Graphics2D g, Head h)
  {
    g.setColor(RenderingConstants.PATH_COLOR);
    g.fill(getShape(h));
  }
}
