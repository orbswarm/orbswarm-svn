package com.orbswarm.swarmcon.vobject;

import java.util.Collection;

public interface IVobjects<E extends IVobject> extends Collection<E>, IVobject
{
  public void update(double time);
}