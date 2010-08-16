package com.orbswarm.swarmcon.store;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import com.orbswarm.swarmcon.path.ABlock;
import com.orbswarm.swarmcon.path.IBlock;

public class IBlockAdapter extends XmlAdapter<ABlock, IBlock>
{
  public ABlock marshal(IBlock iBlock) throws Exception
  {
    return (ABlock)iBlock;
  }

  public IBlock unmarshal(ABlock aBlock) throws Exception
  {
    return aBlock;
  }
}
