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
    double startSize = ORB_DIAMETER * 3;
    double endSize = ORB_DIAMETER * 3.5;
    
    Shape start = new Ellipse2D.Double(b.getX() - startSize / 2, b.getY() -
      startSize / 2, startSize, startSize);
    Shape end = new Ellipse2D.Double(e.getX() - endSize / 2, e.getY() -
      endSize / 2, endSize, endSize);
    
    g.setColor(new Color(0, 0, 255, 128));
    g.fill(start);
    g.setColor(new Color(0, 255, 0, 128));
    g.fill(end);
    
    g.setColor(PATH_COLOR);
    g.setStroke(PATH_STROKE);
    g.draw(getShape(b));
  }
}
