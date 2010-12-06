package com.orbswarm.swarmcon.path;

import java.util.Collections;
import java.util.List;
import java.util.Vector;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@SuppressWarnings("serial")
@XmlAccessorType(XmlAccessType.FIELD)
public class MarkerHolder
{
  @SuppressWarnings("unused")
  @XmlElement(name="marker")
  private final List<IMarker> mMakers;
  
  public MarkerHolder()
  {
    mMakers = new Vector<IMarker>();
  }

  public void add(IMarker marker)
  {
    mMakers.add(marker);
  }

  public List<IMarker> getAll()
  {
    return Collections.unmodifiableList(mMakers);
  }
}
