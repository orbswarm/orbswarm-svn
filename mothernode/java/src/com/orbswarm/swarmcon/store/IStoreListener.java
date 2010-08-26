package com.orbswarm.swarmcon.store;

import com.orbswarm.swarmcon.view.IRenderable;

/**
 * A store listen which is called when changes are made to the store.
 * 
 * @author trebor
 */

public interface IStoreListener
{
  <T extends IRenderable> void itemAdded(IItem<T> item);
  <T extends IRenderable> void itemRemoved(IItem<T> item);
}