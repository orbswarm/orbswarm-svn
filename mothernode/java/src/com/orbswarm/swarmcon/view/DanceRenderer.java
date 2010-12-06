package com.orbswarm.swarmcon.view;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Line2D.Double;

import org.apache.log4j.Logger;
import org.trebor.util.Angle;
import org.trebor.util.PathTool.PathPoint;

import com.orbswarm.swarmcon.path.IBlockPath;
import com.orbswarm.swarmcon.path.IDance;
import com.orbswarm.swarmcon.path.IMarker;
import com.orbswarm.swarmcon.path.SyncAction;
import com.orbswarm.swarmcon.swing.SwarmCon.MouseMobject;

public class DanceRenderer extends ARenderer<IDance>
{
  private static Logger log = Logger.getLogger(DanceRenderer.class);

  public IRenderable getSelected(Point2D selectionPoint, MouseMobject o)
  {
    throw new UnsupportedOperationException();
  }

  public Shape getShape(IDance dance)
  {
    GeneralPath gp = new GeneralPath();
    for (IBlockPath bp: dance.getPaths())
      gp.append(bp.getPath(), false);

    return RenderingConstants.PATH_STROKE.createStrokedShape(gp);
  }

  public void render(Graphics2D g, IDance dance)
  {
    g.transform(dance.getTransform());

    // draw the paths

    for (IBlockPath bp : dance.getPaths())
      RendererSet.render(g, bp);

    // draw the sync lines

    g.setColor(RenderingConstants.SYNC_COLOR);
    g.setStroke(RenderingConstants.SYNC_STROKE);
    for (IMarker marker : dance.getMarkers())
    {
      SyncAction sa = marker.getSyncAction();
      if (null != sa)
      {
        PathPoint pp1 = marker.getPathPoint();
        Point2D p11 = computeSynchPoint(pp1, true);
        Point2D p12 = computeSynchPoint(pp1, false);
        PathPoint pp2 = sa.getSyncTo().getPathPoint();
        Point2D p21 = computeSynchPoint(pp2, true);
        Point2D p22 = computeSynchPoint(pp2, false);
        
        Point2D p1;
        Point2D p2;
        
        if (p11.distance(p21) < p12.distance(p21))
          p1 = p11;
        else
          p1 = p12;
        
        if (p1.distance(p21) < p1.distance(p22))
          p2 = p21;
        else
          p2 = p22;

          g.draw(new Line2D.Double(p1, p2));
      }
    }

    g.setColor(RenderingConstants.MARKER_COLOR);
    for (IMarker marker : dance.getMarkers())
    {
      PathPoint pp = marker.getPathPoint();
      AffineTransform t =
        AffineTransform.getTranslateInstance(pp.getX(), pp.getY());
      t.rotate(pp.getAngle().as(Angle.Type.RADIANS) + Math.PI / 2);
      g.fill(t.createTransformedShape(RenderingConstants.MARKER_SHAPE));
    }
  }
  
  public static Point2D computeSynchPoint(PathPoint pp, boolean isLeft)
  {
    AffineTransform t =
      AffineTransform
        .getTranslateInstance(
          (isLeft
            ? -1
            : 1) *
            (RenderingConstants.MARKER_INSIDE_OFFSET + RenderingConstants.MARKER_LENGTH),
          0);
    t.rotate(pp.getAngle().as(Angle.Type.RADIANS) + Math.PI / 2, pp.getX(),
      pp.getY());
    Point2D p = new Point2D.Double();
    return t.transform(pp, p);
  }
}
