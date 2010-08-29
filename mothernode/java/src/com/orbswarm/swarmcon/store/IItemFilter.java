package com.orbswarm.swarmcon.store;

import com.orbswarm.swarmcon.view.IRenderable;

public interface IItemFilter
{
  <T extends IRenderable> boolean accept(IItem<T> item);
}
