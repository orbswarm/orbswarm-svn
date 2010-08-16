package com.orbswarm.swarmcon.store;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class IItemAdapter extends XmlAdapter<Item<?>, IItem<?>>
{

  public Item<?> marshal(IItem<?> v) throws Exception
  {
    return (Item<?>)v;
  }

  public IItem<?> unmarshal(Item<?> v) throws Exception
  {
    return v;
  }
}
