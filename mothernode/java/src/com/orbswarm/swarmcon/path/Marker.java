package com.orbswarm.swarmcon.path;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

public class Marker implements IMarker
{
  private double mExtent = 0;
  private final IBlockPath mPath;
  private final Collection<IAction> mActions;
  
  public Marker(IBlockPath path)
  {
    mPath = path;
    mActions = new HashSet<IAction>();
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

  public void addAction(IAction action)
  {
    mActions.add(action);
  }

  public Collection<IAction> getActions()
  {
    return Collections.unmodifiableCollection(mActions);
  }

  public boolean removeAction(IAction action)
  {
    return mActions.remove(action);
  }
}
