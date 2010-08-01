package com.orbswarm.swarmcon.mobject;

import java.awt.geom.Point2D;
import java.util.Collection;

public interface IMobjects<E extends IMobject> extends Collection<E>, IMobject
{
  public E findSelected(Point2D.Double point);
  
  public void update(double time);
}