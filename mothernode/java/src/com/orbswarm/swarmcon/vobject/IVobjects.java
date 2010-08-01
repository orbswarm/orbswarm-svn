package com.orbswarm.swarmcon.vobject;

import java.awt.geom.Point2D;
import java.util.Collection;

public interface IVobjects<E extends IVobject> extends Collection<E>, IVobject
{
  public E findSelected(Point2D.Double point);
  
  public void update(double time);
}