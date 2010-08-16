package com.orbswarm.swarmcon.store;

import java.util.Calendar;
import java.util.UUID;

import com.orbswarm.swarmcon.vobject.IVobject;

public interface IItem<T extends IVobject>
{
  UUID getId();
  
  Calendar getCreateTime();
  
  Calendar getModifiedTime();
  
  String getAuthor();
  
  String getName();
  
  T getItem();
  
  void setItem(T item);
}
