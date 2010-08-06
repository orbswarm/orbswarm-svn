package com.orbswarm.swarmcon.view;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;

import org.trebor.util.Angle;

import com.orbswarm.swarmcon.SwarmCon.MouseMobject;
import com.orbswarm.swarmcon.path.IBlock;
import com.orbswarm.swarmcon.vobject.IVobject;

public class BlockRenderer extends ARenderer<IBlock>
{
  private final Shape mArrowShape;

  public BlockRenderer()
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

  public Shape getShape(IBlock b)
  {
    return b.getPath();
  }

  public void render(Graphics2D g, IBlock b)
  {
    g.setColor(RenderingConstants.PATH_COLOR);
    g.setStroke(RenderingConstants.PATH_STROKE);
    g.draw(getShape(b));
    Point2D pos = b.getPosition();
    AffineTransform at =
      AffineTransform.getTranslateInstance(pos.getX(), pos.getY());
    at.concatenate(AffineTransform.getRotateInstance(b.getPrevious()
      .getEndAngle().as(Angle.Type.RADIANS)));
    g.fill(at.createTransformedShape(mArrowShape));
  }
}
