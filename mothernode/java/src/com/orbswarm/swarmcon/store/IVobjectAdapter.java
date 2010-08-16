package com.orbswarm.swarmcon.store;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import com.orbswarm.swarmcon.vobject.AVobject;
import com.orbswarm.swarmcon.vobject.IVobject;

public class IVobjectAdapter extends XmlAdapter<AVobject, IVobject>
{

  public AVobject marshal(IVobject v) throws Exception
  {
    return (AVobject)v;
  }

  public IVobject unmarshal(AVobject v) throws Exception
  {
    return v;
  }
}
