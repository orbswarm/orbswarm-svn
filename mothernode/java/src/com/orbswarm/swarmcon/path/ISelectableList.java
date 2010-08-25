package com.orbswarm.swarmcon.path;

import java.util.Collection;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.orbswarm.swarmcon.store.ISelectableListAdapter;

@XmlJavaTypeAdapter(ISelectableListAdapter.class)
public interface ISelectableList<T extends ISelectable>
{
  void addBefore(T... items);

  void addAfter(T... items);

  boolean remove();
  
  void replace(T item);

  void first();
  
  void next();
  
  void previouse();

  void last();
  
  T getCurrent();

  int size();
  
  boolean isEmpty();
  
  Collection<T> getAll();
}
