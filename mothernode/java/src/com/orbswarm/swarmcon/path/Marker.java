package com.orbswarm.swarmcon.path;

import java.util.UUID;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.trebor.util.PathTool.PathPoint;

@XmlRootElement(name="marker")
@XmlAccessorType(XmlAccessType.FIELD)
public class Marker implements IMarker
{
  @SuppressWarnings("unused")
  @XmlID
  private final String mId;
  @XmlIDREF
  private final IBlockPath mPath;
  @XmlElement(name = "extent")
  private double mExtent = 0;
  @XmlElement(name = "syncAction")
  private SyncAction mSyncAction;
  
  public Marker()
  {
    this(null, 0);
  }
  
  public Marker(IBlockPath path)
  {
    this(path, path.getLength() / 2);
  }
  
  public Marker(IBlockPath path, double extent)
  {
    mId = UUID.randomUUID().toString();
    mPath = path;
    mExtent = extent;
    mSyncAction = null;
  }

  public IBlockPath getPath()
  {
    return mPath;
  }

  public double getExtent()
  {
    return mExtent;
  }

  public void setExtent(double extent)
  {
    mExtent = extent;
  }

  public int compareTo(IMarker other)
  {
    return Double.compare(getExtent(), other.getExtent());
  }

  public void setSyncAction(SyncAction syncAction)
  {
    mSyncAction = syncAction;
  }

  public SyncAction getSyncAction()
  {
    return mSyncAction;
  }

  public PathPoint getPathPoint()
  {
    return getPath().getPathTool().getPathPoint(getExtent());
  }
}
