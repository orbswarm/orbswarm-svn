package com.orbswarm.swarmcon.store;

import java.util.Calendar;
import java.util.UUID;

import com.orbswarm.swarmcon.vobject.IVobject;

class Item<T extends IVobject> implements IItem<T>
{
  private T mItem;
  private final UUID mId;
  private final String mAuthor;
  private final Calendar mCreated;
  private Calendar mModified;
  private String mName;
  
  Item(T item, String name)
  {
    mItem = item;
    mId = UUID.randomUUID();
    mName = name;
    mCreated = Calendar.getInstance();
    mModified = Calendar.getInstance();
    mAuthor = System.getProperty("user.name");
  }
  
  public String getAuthor()
  {
    return mAuthor;
  }

  public Calendar getCreateTime()
  {
    return mCreated;
  }

  public UUID getId()
  {
    return mId;
  }

  public T getItem()
  {
    return mItem;
  }

  public Calendar getModifiedTime()
  {
    return mModified;
  }

  public String getName()
  {
    return mName;
  }

  public void setItem(T item)
  {
    mItem = item;
    mModified = Calendar.getInstance();
  }
}
