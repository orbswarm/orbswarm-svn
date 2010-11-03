package com.orbswarm.swarmcon.path;

import java.util.Collection;

public interface IMarker
{
  IBlockPath getPath();
  double getExtent();
  void setExtent(double extent);
  void addAction(IAction action);
  Collection<IAction> getActions();
  boolean removeAction(IAction action);
}