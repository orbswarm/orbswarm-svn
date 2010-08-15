package com.orbswarm.swarmcon.store;


public interface IItemStore
{
  interface IItemKey
  {
  }
  
  /**
   * Add an {@link @IItem} to the item store. A unique key will be
   * returned for that {@link @IItem}.
   * 
   * @param item the item to add to the store
   * 
   * @return a unique key created for that item.
   */
  
  IItemKey add(IItem<?> item);
  
  /**
   * Remove an {@link @IItem} from the store.
   * @param key the key used to uniquely identify the item.
   * 
   * @return
   */
  
  boolean remove(IItemKey key);
  
}
