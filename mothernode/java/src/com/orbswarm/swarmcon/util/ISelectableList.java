package com.orbswarm.swarmcon.util;

import java.util.List;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


@XmlJavaTypeAdapter(ISelectableListAdapter.class)
public interface ISelectableList<T extends ISelectable> extends Cloneable
{
  void addBefore(T... items);

  void addAfter(T... items);

  boolean isFirst();

  boolean isLast();
  
  boolean remove();
  
  boolean removeAll();
  
  void replace(T item);

  void first();
  
  void next();
  
  void previouse();

  void last();
  
  T getCurrent();

  int size();
  
  boolean isEmpty();
  
  List<T> getAll();
  
  ISelectableList<T> clone() throws CloneNotSupportedException;
}
