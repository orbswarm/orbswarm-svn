package com.orbswarm.swarmcon.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.Vector;

import org.trebor.util.PathTool;

import com.orbswarm.swarmcon.path.IBlock;
import com.orbswarm.swarmcon.path.IBlockPath;
import com.orbswarm.swarmcon.swing.SwarmCon.MouseMobject;

public class BlockPathRenderer extends ARenderer<IBlockPath>
{
  private final Shape mArrowShape;
  
  public BlockPathRenderer()
  {
    GeneralPath arrowShape = new GeneralPath();
    arrowShape.moveTo(-RenderingConstants.PATH_WIDTH / 2, 0);
    arrowShape.lineTo(RenderingConstants.PATH_WIDTH / 2, 0);
    arrowShape.lineTo(0, RenderingConstants.PATH_WIDTH / 2);
    arrowShape.closePath();

    mArrowShape = arrowShape;
  }  
  
  public IRenderable getSelected(Point2D selectionPoint, MouseMobject o)
  {
    throw new UnsupportedOperationException();
  }

  public Shape getShape(IBlockPath bp)
  {
    return RenderingConstants.PATH_STROKE.createStrokedShape(bp.getPath());
  }

  public void render(Graphics2D g, IBlockPath bp)
  {
    g.transform(bp.getTransform());

    if (bp.getBlocks().size() > 0)
    {
      g.setColor(new Color(0, 0, 0, 128));
      g.setStroke(new BasicStroke(0.05f));
      PathTool pt = new PathTool(bp.getPath(), 0);
      double step = pt.getLength() / (pt.getLength() / .5);
      Collection<PathTool.PathPoint> points =
        new Vector<PathTool.PathPoint>();
      points.add(pt.getStartPoint());
      for (double d = 0; d < pt.getLength(); d += step)
        points.add(pt.getPathPoint(d));
      points.add(pt.getEndPoint());
      for (PathTool.PathPoint pp : points)
      {
        Point2D ep = pp.getAngle().cartesian(1, pp);
        Line2D line = new Line2D.Double(pp, ep);
        g.draw(line);
      }
    }

    if (bp.isSelected())
    {
      g.setStroke(RenderingConstants.BLOCK_STROKE);

      if (bp.getBlocks().isEmpty())
      {
        g.setColor(RenderingConstants.SELECTED_BLOCK_COLOR);
        g.fill(mArrowShape);
      }
      else
        for (IBlock b : bp.getBlocks())
        {
          g.setColor(b.isSelected()
            ? RenderingConstants.SELECTED_BLOCK_COLOR
            : RenderingConstants.SELECTED_PATH_COLOR);

          g.draw(b.getPath());
          g.fill(mArrowShape);
          g.transform(b.getBlockTransform());
        }
    }
    else
    {
      g.setStroke(RenderingConstants.PATH_STROKE);
      g.setColor(RenderingConstants.PATH_COLOR);

      if (bp.getBlocks().isEmpty())
        g.fill(mArrowShape);
      else
        g.draw(bp.getPath());
    }

  }
}
