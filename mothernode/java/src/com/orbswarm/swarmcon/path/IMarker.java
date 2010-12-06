package com.orbswarm.swarmcon.path;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.trebor.util.PathTool.PathPoint;

import com.orbswarm.swarmcon.store.IMarkerAdapter;

@XmlJavaTypeAdapter(IMarkerAdapter.class)
public interface IMarker extends Comparable<IMarker>
{
  IBlockPath getPath();
  double getExtent();
  void setExtent(double extent);
  SyncAction getSyncAction();
  void setSyncAction(SyncAction syncAction);
  PathPoint getPathPoint();
}