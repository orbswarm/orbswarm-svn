package com.orbswarm.swarmcon.store;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import com.orbswarm.swarmcon.path.ABlock;
import com.orbswarm.swarmcon.path.BlockPath;
import com.orbswarm.swarmcon.vobject.IVobject;

public abstract class AItemStore implements IItemStore
{
  public static final String GUID_PATTERN =
    "[a-f0-9]{8}\\-[a-f0-9]{4}\\-[a-f0-9]{4}\\-[a-f0-9]{4}\\-[a-f0-9]{12}";
  private final Map<UUID, IItem<? extends IVobject>> mItems;
  private final Set<IStoreListener> mListeners;
  private final JAXBContext mContext;

  AItemStore()
  {
    // initialize containers
    
    mItems = new HashMap<UUID, IItem<? extends IVobject>>();
    mListeners = new HashSet<IStoreListener>();

    // initialize JAXB context
    
    JAXBContext context = null;
    try
    {
      context = JAXBContext.newInstance(IItem.class, BlockPath.class, ABlock.class);
    }
    catch (JAXBException e)
    {
      e.printStackTrace();
    }
    mContext = context;

    // synch store to catalog
    
    for (UUID id: catalog())
    {
      IItem<?> wrapped = unmarshal(restore(id));
      mItems.put(id, wrapped);
    }
  }

  public <T extends IVobject> IItem<T> add(T item, String name)
  {
    IItem<T> wrapped = new Item<T>(item, name);
    mItems.put(wrapped.getId(), wrapped);
    save(wrapped.getId(), marshal(wrapped));
    for (IStoreListener listener : mListeners)
      listener.itemAdded(wrapped);
    return wrapped;
  }

  public void addStoreListener(IStoreListener listner)
  {
    mListeners.add(listner);
  }

  public boolean removeStoreListener(IStoreListener listner)
  {
    return mListeners.remove(listner);
  }

  @SuppressWarnings("unchecked")
  public <T extends IVobject> boolean remove(IItem<T> item)
  {
    throw new UnsupportedOperationException();
//    Item<T> wrapped = (Item<T>)mItems.remove(item.getId());
//    if (null != wrapped)
//      for (IStoreListener listener : mListeners)
//        listener.itemRemoved(wrapped);
//    return wrapped;
  }

  public Collection<IItem<? extends IVobject>> getItems()
  {
    return Collections.unmodifiableCollection(mItems.values());
  }

  <T extends IVobject> String marshal(IItem<T> item)
  {
    StringWriter writer = new StringWriter();
    try
    {
      Marshaller marshaller;
      marshaller = mContext.createMarshaller();
      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
      marshaller.marshal(item, writer);
    }
    catch (JAXBException e)
    {
      e.printStackTrace();
    }

    return writer.toString();
  }

  @SuppressWarnings("unchecked")
  <T extends IVobject> Item<T> unmarshal(String xml)
  {
    StringReader reader = new StringReader(xml);
    Item<T> wrapped = null;

    try
    {
      Unmarshaller unmarshaller;
      unmarshaller = mContext.createUnmarshaller();
      unmarshaller
        .setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
      wrapped = (Item<T>)unmarshaller.unmarshal(reader);
    }
    catch (JAXBException e)
    {
      e.printStackTrace();
    }

    return wrapped;
  }

  abstract void save(UUID id, String xml);

  abstract String restore(UUID id);

  abstract Collection<UUID> catalog();
}
