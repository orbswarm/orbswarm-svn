package com.orbswarm.swarmcon.path;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "syncAction")
@XmlAccessorType(XmlAccessType.FIELD)
public class SyncAction extends AAction
{
  @XmlIDREF
  private final IMarker mSyncTo;
  
  public SyncAction()
  {
    this(null);
  }

  public SyncAction(IMarker syncTo)
  {
    mSyncTo = syncTo;
  }

  public IMarker getSyncTo()
  {
    return mSyncTo;
  }

  public String toString()
  {
    return "SyncAction [mSyncTo=" + mSyncTo + "]";
  }
}
