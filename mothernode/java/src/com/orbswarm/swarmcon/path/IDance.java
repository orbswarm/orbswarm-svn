package com.orbswarm.swarmcon.path;

import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.util.Collection;

import com.orbswarm.swarmcon.path.Dance.Layout;
import com.orbswarm.swarmcon.vobject.IVobject;

public interface IDance extends IVobject
{
  Rectangle2D getBounds();

  Shape getPath();
  
  void addBefore(IBlockPath... paths);

  void addAfter(IBlockPath... paths);

  void replace(IBlockPath path);

  boolean remove();

  int size();

  Collection<IBlockPath> getPaths();
  
  void setLayout(Layout layout);

  Layout getLayout();

  void setSeperation(double distance);

  double getSeperation();

  void previousePath();
  
  void nextPath();
  
  IBlockPath getCurrentPath();
}