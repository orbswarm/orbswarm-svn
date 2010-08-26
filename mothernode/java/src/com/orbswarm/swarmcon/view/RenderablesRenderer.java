package com.orbswarm.swarmcon.view;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.util.HashSet;
import java.util.Set;


public class RenderablesRenderer extends ARenderer<IRenderables<?>>
{
  @SuppressWarnings("unchecked")
  public void render(Graphics2D g, IRenderables<?> vobjects)
  {
    for (IRenderable vobject: vobjects)
      RendererSet.render(g, vobject);
  }

  public Shape getShape(IRenderables<?> vobjects)
  {
    throw new UnsupportedOperationException();
  }

  public IRenderable getSelected(Point2D selectionPoint, IRenderables<?> vobjects)
  {
    Set<IRenderable> candidates = new HashSet<IRenderable>();

    for (IRenderable vobject : vobjects)
    {
      IRenderable candidate = RendererSet.getRenderer(vobject).getSelected(
        selectionPoint, vobject);
      
      if (null != candidate)
        candidates.add(candidate);
    }

    double shortestDistance = Double.MAX_VALUE;
    IRenderable closest = null;
    for (IRenderable vobject : candidates)
    {
      double distance = RendererSet.getRenderer(vobject).getDistanceTo(
        selectionPoint, vobject);
      if (distance < shortestDistance)
      {
        closest = vobject;
        shortestDistance = distance;
      }
    }

    return closest;
  }
}
