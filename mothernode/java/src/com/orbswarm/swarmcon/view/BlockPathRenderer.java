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
  private static final Color PATH_COLOR = Color.PINK;
  private static Stroke mPathStroke = new BasicStroke((float)(ORB_DIAMETER * 2));

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
    g.setStroke(mPathStroke);
    g.draw(getShape(bp));
  }
}
