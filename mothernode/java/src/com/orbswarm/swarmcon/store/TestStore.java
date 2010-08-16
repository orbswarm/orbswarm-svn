package com.orbswarm.swarmcon.store;


import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.orbswarm.swarmcon.path.BlockPath;
import com.orbswarm.swarmcon.path.CurveBlock;
import com.orbswarm.swarmcon.path.StraightBlock;

public class TestStore extends AItemStore
{
  @SuppressWarnings("unused")
  private static Logger log = Logger.getLogger(TestStore.class);
  
  private final Map<UUID, String> mStore;

  public TestStore()
  {
    mStore = new HashMap<UUID, String>();
    
    CurveBlock lt = new CurveBlock(90, 4, CurveBlock.Type.LEFT);
    CurveBlock rt = new CurveBlock(90, 4, CurveBlock.Type.RIGHT);
    StraightBlock st = new StraightBlock(4);
    
    BlockPath bp1 = new BlockPath(lt, st, lt, rt, rt);
    BlockPath bp2 = new BlockPath(rt, st, rt, rt, lt);
    BlockPath bp3 = new BlockPath(st, lt, rt, rt);

    add(bp1, "path 1");
    add(bp2, "path 2");
    add(bp3, "path 3");
    
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
