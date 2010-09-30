package com.orbswarm.swarmcon.store;

import java.util.Calendar;
import java.util.UUID;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.orbswarm.swarmcon.view.IRenderable;

@XmlRootElement
public class Item<T extends IRenderable> implements IItem<T>
{
  @XmlAnyElement(lax=true)
  private T mItem;
  @XmlElement(name="uuid")
  private UUID mId;
  @XmlElement(name="author")
  private String mAuthor;
  @XmlElement(name="created")
  private Calendar mCreated;
  @XmlElement(name="modified")
  private Calendar mModified;
  @XmlElement(name="name")
  private String mName;

  public Item()
  {
    this(null, "");
  }
  
  public Item(T item, String name)
  {
    mItem = item;
    mId = UUID.randomUUID();
    mName = name;
    mCreated = Calendar.getInstance();
    mModified = Calendar.getInstance();
    mAuthor = System.getProperty("user.name");
  }
  
  @XmlTransient
  public String getAuthor()
  {
    return mAuthor;
  }

  @XmlTransient
  public Calendar getCreateTime()
  {
    return mCreated;
  }

  @XmlTransient
  public UUID getId()
  {
    return mId;
  }

  @XmlTransient
  public T getItem()
  {
    return mItem;
  }

  @XmlTransient
  public Calendar getModifiedTime()
  {
    return mModified;
  }

  @XmlTransient
  public String getName()
  {
    return mName;
  }

  public void setItem(T item)
  {
    mItem = item;
    mModified = Calendar.getInstance();
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public IItem<T> clone() throws CloneNotSupportedException
  {
    Item<T> other = (Item<T>)super.clone();
    other.mItem = (T)mItem.clone();
    other.mId = mId;
    other.mAuthor = mAuthor;
    other.mCreated = (Calendar)mCreated.clone();
    other.mModified = (Calendar)mModified.clone();
    other.mName = mName;
    
    return other;
  }
}
