package com.orbswarm.swarmcon.view;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;

import com.orbswarm.swarmcon.path.BlockState;
import com.orbswarm.swarmcon.path.IBlock;
import com.orbswarm.swarmcon.path.IBlockPath;
import com.orbswarm.swarmcon.swing.SwarmCon.MouseMobject;
import com.orbswarm.swarmcon.vobject.IVobject;

public class BlockPathRenderer extends ARenderer<IBlockPath>
{
  private final Shape mArrowShape;
  
  private boolean mRenderComponents = true;


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
    return bp.getPath();
  }

  public void render(Graphics2D g, IBlockPath bp)
  {
    g.setColor(RenderingConstants.PATH_COLOR);
    g.setStroke(RenderingConstants.PATH_STROKE);
    
    if (mRenderComponents)
    {
      BlockState state = bp.getState();
      
      for (IBlock b : bp.getBlocks())
      {
        g.draw(b.getPath(state));
        g.fill(state.creatTransformedShape(mArrowShape));
        state = state.add(b.getDeltaState());
      }
    }
    else
      g.draw(getShape(bp));
  }
}
