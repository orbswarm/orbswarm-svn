package com.orbswarm.swarmcon.util;

import java.util.Collection;
import java.util.Collections;
import java.util.ListIterator;
import java.util.Vector;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.log4j.Logger;

@XmlRootElement(name="selectables")
public class SelectableList<T extends ISelectable> implements ISelectableList<T>
{
  @SuppressWarnings("unused")
  private static Logger log = Logger.getLogger(SelectableList.class);

  @XmlTransient
  private ListIterator<T> mItemItr;
  @XmlAnyElement(lax = true)
  private Collection<T> mItems;
  @XmlElement(name="wrap")
  private boolean mWrap;
  
  
  public SelectableList()
  {
    this(false);
  }
  
  public SelectableList(boolean wrap)
  {
    mWrap = wrap;
    Vector<T> items = new Vector<T>();
    mItems = items;
    mItemItr = items.listIterator();
  }

  @SuppressWarnings("unchecked")
  public SelectableList(T... items)
  {
    this();
    for (T item : items)
      addAfter(item);
  }
  
  @SuppressWarnings("unchecked")
  public void initializeIterator()
  {
    mItemItr = ((Vector<T>)mItems).listIterator();
    
    while (mItemItr.hasNext())
      if (mItemItr.next().isSelected())
      {
        mItemItr.previous();
        break;
      }
  }

  public Collection<T> getAll()
  {
    return Collections.unmodifiableCollection(mItems);
  }

  public void addBefore(T... items)
  {
    select(false);
    for (T item : items)
    {
      if (mItemItr.hasPrevious())
        mItemItr.previous();
      mItemItr.add(item);
    }
    select(true);
  }

  public void addAfter(T... items)
  {
    if (items.length > 0)
    {
      select(false);
      if (mItemItr.hasNext())
        mItemItr.next();
      for (T item : items)
        mItemItr.add(item);
      mItemItr.previous();
      select(true);
    }
  }

  public boolean isEmpty()
  {
    return getAll().isEmpty();
  }
  
  private void select(boolean selected)
  {
    if (!isEmpty())
      getCurrent().setSelected(selected);
  }

  public void replace(T item)
  {
    select(false);
    mItemItr.set(item);
    select(true);
  }

  public int size()
  {
    return getAll().size();
  }

  public boolean remove()
  {
    if (!isEmpty())
    {
      select(false);
      mItemItr.remove();
      if (!mItemItr.hasNext() && mItemItr.hasPrevious())
        mItemItr.previous();
      select(true);
      return true;
    }
    return false;
  }

  public boolean removeAll()
  {
    if (isEmpty())
      return false;

    while (!isEmpty())
      remove();
    return true;
  }

  public void first()
  {
    select(false);
    while (mItemItr.hasPrevious())
      mItemItr.previous();
    
    select(true);
  }
  
  public void next()
  {
    if (mItemItr.hasNext())
    {
      select(false);
      mItemItr.next();
      if (!mItemItr.hasNext())
        if (mWrap)
          while (mItemItr.hasPrevious())
            mItemItr.previous();
        else
          mItemItr.previous();
      select(true);
    }
  }

  public void previouse()
  {
    if (mItemItr.hasPrevious())
    {
      select(false);
      mItemItr.previous();
      select(true);
    }
    else if (mWrap)
      last();
  }

  public void last()
  {
    select(false);
    while (mItemItr.hasNext())
      mItemItr.next();
    if (mItemItr.hasPrevious())
      mItemItr.previous();
    select(true);
  }
  
  public T getCurrent()
  {
    T current = null;

    if (!isEmpty())
    {
      current = mItemItr.next();
      mItemItr.previous();
    }

    return current;
  }
}
