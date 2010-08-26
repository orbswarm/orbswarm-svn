package com.orbswarm.swarmcon.store;

import java.util.Collection;

import com.orbswarm.swarmcon.view.IRenderable;

public interface IItemStore
{
  /**
   * Add an {@link @IItem} to the item store. A unique key will be
   * returned for that {@link @IItem}.
   * 
   * @param item the item to add to the store
   * @param name the name of the item
   * 
   * @return the item wrapped in an item wrapper
   */
  
  <T extends IRenderable> IItem<T> add(T item, String name);
  
  /**
   * Remove an {@link @IItem} from the store.
   * @param key the key used to uniquely identify the item.
   * 
   * @return true if the item was removed
   */
  
  <T extends IRenderable> boolean remove(IItem<T> item);

  /**
   * Return the collected items in this store.
   * 
   * @return a collection of contained items
   */
  
  Collection<IItem<? extends IRenderable>> getItems();

  /**
   * Add a listener to the store.
   * 
   * @param listner
   */
  
  void addStoreListener(IStoreListener listner);
  
  /**
   * Remove a store listener.
   * 
   * @param listner
   * @return true if the listener was removed
   */
  
  boolean removeStoreListener(IStoreListener listner);
}
