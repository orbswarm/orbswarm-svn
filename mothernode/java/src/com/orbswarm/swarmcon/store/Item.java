package com.orbswarm.swarmcon.store;

import java.util.Calendar;
import java.util.UUID;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.orbswarm.swarmcon.view.IRenderable;

@XmlRootElement
class Item<T extends IRenderable> implements IItem<T>
{
  @XmlElement(name="item")
  private T mItem;
  @XmlElement(name="uuid")
  private final UUID mId;
  @XmlElement(name="author")
  private final String mAuthor;
  @XmlElement(name="created")
  private final Calendar mCreated;
  @XmlElement(name="modified")
  private Calendar mModified;
  @XmlElement(name="name")
  private String mName;

  public Item()
  {
    this(null, "");
  }
  
  Item(T item, String name)
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
}
