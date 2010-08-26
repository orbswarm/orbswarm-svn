package com.orbswarm.swarmcon.store;

import java.util.Calendar;
import java.util.UUID;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.orbswarm.swarmcon.view.IRenderable;

@XmlJavaTypeAdapter(IItemAdapter.class)
public interface IItem<T extends IRenderable>
{
  UUID getId();
  
  Calendar getCreateTime();
  
  Calendar getModifiedTime();
  
  String getAuthor();
  
  String getName();
  
  T getItem();
  
  void setItem(T item);
}
