package com.orbswarm.swarmcon.view;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;

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

//    if (bp.size() == 0)
//    {
//      g.setColor(bp.isSelected()
//        ? RenderingConstants.SELECTED_PATH_COLOR
//        : RenderingConstants.PATH_COLOR);
//
//      g.setStroke(RenderingConstants.PATH_STROKE);
//      g.draw(new Line2D.Double(new Point2D.Double(0, 0), new Point2D.Double(
//        0, 0)));
//    }
//    else 
      
    if (bp.isSelected())
    {
      g.setStroke(RenderingConstants.BLOCK_STROKE);

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
      g.draw(bp.getPath());
    }
  }
}
