package com.orbswarm.swarmcon.store;


import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;

public class TestStore extends AItemStore
{
  @SuppressWarnings("unused")
  private static Logger log = Logger.getLogger(TestStore.class);
  
  private final Map<UUID, String> mStore;

  public TestStore()
  {
    super(false);
    mStore = new HashMap<UUID, String>();
    initialize();
  }

  Collection<UUID> catalog()
  {
    return mStore.keySet();
  }

  String restore(UUID id)
  {
    log.debug(String.format("restore %s: %s", id, mStore.get(id)));
    return mStore.get(id);
  }

  void save(UUID id, String xml)
  {
    log.debug(String.format("save %s: %s", id, xml));
    mStore.put(id, xml);
  }
}
