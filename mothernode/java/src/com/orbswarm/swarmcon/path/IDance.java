package com.orbswarm.swarmcon.path;

import java.awt.geom.Rectangle2D;
import java.util.Collection;

import com.orbswarm.swarmcon.path.Dance.Layout;
import com.orbswarm.swarmcon.vobject.IVobject;

public interface IDance extends IVobject
{
  Rectangle2D getBounds();
  
  void add(IBlockPath path);
  
  boolean remove(IBlockPath path);

  Collection<IBlockPath> getPaths();

  void setLayout(Layout layout);

  Layout getLayout();

  void setSeperation(double distance);

  double getSeperation();
}