package com.orbswarm.swarmcon.store;

import com.orbswarm.swarmcon.vobject.IVobject;

/**
 * A store listen which is called when changes are made to the store.
 * 
 * @author trebor
 */

public interface IStoreListener
{
  <T extends IVobject> void itemAdded(IItem<T> item);
  <T extends IVobject> void itemRemoved(IItem<T> item);
}