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
import com.orbswarm.swarmcon.view.IRenderable;

public abstract class AItemStore implements IItemStore
{
  public static final String GUID_PATTERN =
    "[a-f0-9]{8}\\-[a-f0-9]{4}\\-[a-f0-9]{4}\\-[a-f0-9]{4}\\-[a-f0-9]{12}";
  private final Map<UUID, IItem<? extends IRenderable>> mItemCache;
  private final Set<IStoreListener> mListeners;
  private final Marshaller mMarshaller;
  private final Unmarshaller mUnmarshaller;

  AItemStore()
  {
    // initialize containers

    mItemCache = new HashMap<UUID, IItem<? extends IRenderable>>();
    mListeners = new HashSet<IStoreListener>();

    // initialize JAXB context

    Marshaller marshaller = null;
    Unmarshaller unmarshaller = null;
    try
    {
      JAXBContext context =
        JAXBContext.newInstance(Item.class, BlockPath.class, ABlock.class);
      marshaller = context.createMarshaller();
      unmarshaller = context.createUnmarshaller();
      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
    }
    catch (JAXBException e)
    {
      e.printStackTrace();
    }
    mMarshaller = marshaller;
    mUnmarshaller = unmarshaller;
  }

  // synch cache to the store
  
  protected void initialize()
  {
    for (UUID id : catalog())
    {
      IItem<?> wrapped = unmarshal(restore(id));
      mItemCache.put(id, wrapped);
    }
  }

  public <T extends IRenderable> IItem<T> add(T item, String name)
  {
    IItem<T> wrapped = new Item<T>(item, name);
    save(wrapped.getId(), marshal(wrapped));
    mItemCache.put(wrapped.getId(), wrapped);
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
  public <T extends IRenderable> boolean remove(IItem<T> item)
  {
    throw new UnsupportedOperationException();
//    Item<T> wrapped = (Item<T>)mItems.remove(item.getId());
//    if (null != wrapped)
//      for (IStoreListener listener : mListeners)
//        listener.itemRemoved(wrapped);
//    return wrapped;
  }

  public Collection<IItem<? extends IRenderable>> getItems()
  {
    return Collections.unmodifiableCollection(mItemCache.values());
  }

  <T extends IRenderable> String marshal(IItem<T> item)
  {
    StringWriter writer = new StringWriter();
    try
    {
      mMarshaller.marshal(item, writer);
    }
    catch (JAXBException e)
    {
      e.printStackTrace();
    }

    return writer.toString();
  }

  @SuppressWarnings("unchecked")
  <T extends IRenderable> Item<T> unmarshal(String xml)
  {
    Item<T> wrapped = null;

    try
    {
      wrapped = (Item<T>)mUnmarshaller.unmarshal(new StringReader(xml));
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
