package com.orbswarm.swarmcon.xml;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import com.orbswarm.swarmcon.path.ABlock;
import com.orbswarm.swarmcon.path.IBlock;

public class IBlockAdapter extends XmlAdapter<Object, IBlock>
{
  public ABlock marshal(IBlock iBlock) throws Exception
  {
    return (ABlock)iBlock;
  }

  public IBlock unmarshal(Object aBlock) throws Exception
  {
    return (IBlock)aBlock;
  }
}
