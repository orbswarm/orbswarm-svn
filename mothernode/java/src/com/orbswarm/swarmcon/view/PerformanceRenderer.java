package com.orbswarm.swarmcon.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

import org.apache.log4j.Logger;
import org.trebor.util.Angle;
import org.trebor.util.PathTool.PathPoint;

import com.orbswarm.swarmcon.performance.IEvent;
import com.orbswarm.swarmcon.performance.IPerformance;
import com.orbswarm.swarmcon.performance.PositionEvent;
import com.orbswarm.swarmcon.swing.SwarmCon.MouseMobject;

public class PerformanceRenderer extends ARenderer<IPerformance>
{
  @SuppressWarnings("unused")
  private static Logger log = Logger.getLogger(PerformanceRenderer.class);
  
  public IRenderable getSelected(Point2D selectionPoint, MouseMobject o)
  {
    throw new UnsupportedOperationException();
  }

  public Shape getShape(IPerformance o)
  {
    throw new UnsupportedOperationException();
    //return RenderingConstants.PATH_STROKE.createStrokedShape(bp.getPath());
  }

  public void render(Graphics2D g, IPerformance p)
  {
    g.setColor(new Color(0, 0, 0, 128));
    g.setStroke(new BasicStroke(0.01f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

    for (IEvent e : p.getEvents())
      g.draw(getShape(e));
  }
  
  private Shape getShape(IEvent event)
  {
    Shape shape = null;

    if (event instanceof PositionEvent)
    {
      PositionEvent pe = (PositionEvent)event;
      double l = RenderingConstants.PATH_WIDTH * pe.getVelocity() / 2;
      PathPoint p = pe.getPosition();
      Line2D line =
        new Line2D.Double(p.getX() - l, p.getY(), p.getX() + l, p.getY());
      AffineTransform t =
        AffineTransform.getRotateInstance(
          p.getAngle().rotate(new Angle(90,
             Angle.Type.DEGREES)).as(Angle.Type.RADIANS), p.getX(), p.getY());
      shape = t.createTransformedShape(line);
      
      //
      // Point2D ep =
      // pe.getPosition().getAngle())
      // .cartesian(, pe.getPosition());
      // shape = new Line2D.Double(pe.getPosition(), ep);
    }
    else
      throw new Error("unknown event type: " +
        event.getClass().getSimpleName());

    return shape;
  }
}
