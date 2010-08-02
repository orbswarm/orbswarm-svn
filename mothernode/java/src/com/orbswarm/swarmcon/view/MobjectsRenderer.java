package com.orbswarm.swarmcon.view;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.util.HashSet;
import java.util.Set;

import com.orbswarm.swarmcon.vobject.IVobject;
import com.orbswarm.swarmcon.vobject.IVobjects;

public class MobjectsRenderer extends ARenderer<IVobjects<?>>
{
  @SuppressWarnings("unchecked")
  public void render(Graphics2D g, IVobjects<?> vobjects)
  {
    for (IVobject vobject: vobjects)
      Renderer.render(g, vobject);
  }

  public Shape getShape(IVobjects<?> vobjects)
  {
    return null;
  }

  public IVobject getSelected(Point2D selectionPoint, IVobjects<?> vobjects)
  {
    Set<IVobject> candidates = new HashSet<IVobject>();

    for (IVobject vobject : vobjects)
    {
      IVobject candidate = Renderer.getRenderer(vobject).getSelected(
        selectionPoint, vobject);
      
      if (null != candidate)
        candidates.add(candidate);
    }

    double shortestDistance = Double.MAX_VALUE;
    IVobject closest = null;
    for (IVobject vobject : candidates)
    {
      double distance = Renderer.getRenderer(vobject).getDistanceTo(
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
