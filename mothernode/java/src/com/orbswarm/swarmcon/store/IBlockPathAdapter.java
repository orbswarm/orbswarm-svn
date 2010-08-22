package com.orbswarm.swarmcon.store;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import com.orbswarm.swarmcon.path.BlockPath;
import com.orbswarm.swarmcon.path.IBlockPath;

public class IBlockPathAdapter extends XmlAdapter<BlockPath, IBlockPath>
{
  public BlockPath marshal(IBlockPath iBlockPath) throws Exception
  {
    return (BlockPath)iBlockPath;
  }

  public IBlockPath unmarshal(BlockPath bp) throws Exception
  {
    return bp;
  }
}

