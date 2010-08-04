package com.orbswarm.swarmcon.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Point2D;

import com.orbswarm.swarmcon.SwarmCon.MouseMobject;
import com.orbswarm.swarmcon.path.IBlockPath;
import com.orbswarm.swarmcon.vobject.IVobject;

import static com.orbswarm.swarmcon.Constants.ORB_DIAMETER;

public class BlockPathRenderer extends ARenderer<IBlockPath>
{
  private static final Color PATH_COLOR = new Color(255, 0, 0, 128);
  private static final Stroke PATH_STROKE = new BasicStroke((float)(ORB_DIAMETER * 2), BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND);

  public IVobject getSelected(Point2D selectionPoint, MouseMobject o)
  {
    throw new UnsupportedOperationException();
  }

  public Shape getShape(IBlockPath bp)
  {
    return bp.getPath();
  }

  public void render(Graphics2D g, IBlockPath bp)
  {
    g.setColor(PATH_COLOR);
    g.setStroke(PATH_STROKE);
    g.draw(getShape(bp));
  }
}
