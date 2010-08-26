package com.orbswarm.swarmcon.store;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import com.orbswarm.swarmcon.view.ARenderable;
import com.orbswarm.swarmcon.view.IRenderable;

public class IVobjectAdapter extends XmlAdapter<ARenderable, IRenderable>
{

  public ARenderable marshal(IRenderable v) throws Exception
  {
    return (ARenderable)v;
  }

  public IRenderable unmarshal(ARenderable v) throws Exception
  {
    return v;
  }
}
