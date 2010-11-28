package com.orbswarm.swarmcon.store;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import com.orbswarm.swarmcon.view.APositionable;
import com.orbswarm.swarmcon.view.IPositionable;

public class IPositionableAdapter extends XmlAdapter<APositionable, IPositionable>
{
  public APositionable marshal(IPositionable v) throws Exception
  {
    return (APositionable)v;
  }

  public IPositionable unmarshal(APositionable v) throws Exception
  {
    return v;
  }
}
