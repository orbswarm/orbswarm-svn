package com.orbswarm.swarmcon.store;

import java.util.Collection;

import com.orbswarm.swarmcon.view.IRenderable;

public interface IItemStore
{
  public static final IItemFilter ACCEPT_ALL = new IItemFilter()
  {
    public <T extends IRenderable> boolean accept(IItem<T> item)
    {
      return true;
    }
  };
  
  /**
   * Create an {@link @IItem} to the item store. A unique key will be
   * returned for the new {@link @IItem}.
   * 
   * @param item the item to create in the store
   * @param name the name of the item
   * 
   * @return the item wrapped in an item wrapper
   */
  
  <T extends IRenderable> IItem<T> create(T item, String name);
  
  /**
   * Update an item in the store. If the item is not already present it is
   * added to the store.
   * 
   * @param item item to update in the store.
   */
  
  <T extends IRenderable> void update(IItem<T> item);
  
  /**
   * Remove an {@link @IItem} from the store.
   * @param key the key used to uniquely identify the item.
   * 
   * @return true if the item was removed
   */
  
  <T extends IRenderable> boolean delete(IItem<T> item);

  /**
   * Return all the items in this store.
   * 
   * @return a collection of contained items
   */
  
  Collection<IItem<? extends IRenderable>> getAll();

  /**
   * Return items which are accepted by the provided filter.
   * 
   * @return a collection of contained items
   */
  
  Collection<IItem<? extends IRenderable>> getSome(IItemFilter filter);

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
