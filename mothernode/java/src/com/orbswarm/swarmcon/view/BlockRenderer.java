package com.orbswarm.swarmcon.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

import com.orbswarm.swarmcon.SwarmCon.MouseMobject;
import com.orbswarm.swarmcon.path.IBlock;
import com.orbswarm.swarmcon.vobject.IVobject;

import static com.orbswarm.swarmcon.Constants.ORB_DIAMETER;

public class BlockRenderer extends ARenderer<IBlock>
{
  private static final Color PATH_COLOR = new Color(255, 0, 0, 128);
  private static final Stroke PATH_STROKE = new BasicStroke((float)(ORB_DIAMETER * 2), BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND);
  private static final double HEAD_SIZE = ORB_DIAMETER * 3;

  public IVobject getSelected(Point2D selectionPoint, MouseMobject o)
  {
    throw new UnsupportedOperationException();
  }

  public Shape getShape(IBlock b)
  {
    return b.getPath();
  }

  public void render(Graphics2D g, IBlock b)
  {
    Point2D e = b.getEndPosition();
    Shape start = new Ellipse2D.Double(b.getX() - HEAD_SIZE / 2, b.getY() -
      HEAD_SIZE / 2, HEAD_SIZE, HEAD_SIZE);
    Shape end = new Ellipse2D.Double(e.getX() - HEAD_SIZE / 2, e.getY() -
      HEAD_SIZE / 2, HEAD_SIZE, HEAD_SIZE);
    
    g.setColor(new Color(0, 0, 255, 128));
    g.fill(start);
    g.setColor(new Color(0, 255, 0, 128));
    g.fill(end);
    
    g.setColor(PATH_COLOR);
    g.setStroke(PATH_STROKE);
    g.draw(getShape(b));
  }
}
