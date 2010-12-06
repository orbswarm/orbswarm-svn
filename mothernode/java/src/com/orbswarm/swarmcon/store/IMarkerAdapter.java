package com.orbswarm.swarmcon.store;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import com.orbswarm.swarmcon.path.IMarker;
import com.orbswarm.swarmcon.path.Marker;

public class IMarkerAdapter extends XmlAdapter<Marker, IMarker>
{
  public Marker marshal(IMarker iMarker) throws Exception
  {
    return (Marker)iMarker;
  }

  public IMarker unmarshal(Marker marker) throws Exception
  {
    return marker;
  }
}