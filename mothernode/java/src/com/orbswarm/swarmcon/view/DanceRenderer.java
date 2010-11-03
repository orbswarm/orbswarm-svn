package com.orbswarm.swarmcon.view;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;

import com.orbswarm.swarmcon.path.IBlockPath;
import com.orbswarm.swarmcon.path.IDance;
import com.orbswarm.swarmcon.swing.SwarmCon.MouseMobject;

public class DanceRenderer extends ARenderer<IDance>
{
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
    for (IBlockPath bp: dance.getPaths())
      RendererSet.render(g, bp);
    
//    for (IMarker marker: dance.getMarkers())
//      ;
  }
}
