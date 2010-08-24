package com.orbswarm.swarmcon.view;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;

import com.orbswarm.swarmcon.path.IBlock;
import com.orbswarm.swarmcon.path.IBlockPath;
import com.orbswarm.swarmcon.swing.SwarmCon.MouseMobject;
import com.orbswarm.swarmcon.vobject.IVobject;

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
  
  public IVobject getSelected(Point2D selectionPoint, MouseMobject o)
  {
    throw new UnsupportedOperationException();
  }

  public Shape getShape(IBlockPath bp)
  {
    return RenderingConstants.PATH_STROKE.createStrokedShape(bp.getPath());
  }

  public void render(Graphics2D g, IBlockPath bp)
  {
    g.setColor(RenderingConstants.PATH_COLOR);
    g.setStroke(RenderingConstants.PATH_STROKE);

    g.transform(bp.getTransform());

    if (bp.isSelected())
    {
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
      g.setColor(RenderingConstants.PATH_COLOR);
      g.draw(bp.getPath());
    }
  }
}
