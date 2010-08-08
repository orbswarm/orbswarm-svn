package com.orbswarm.swarmcon.path;

import java.awt.Shape;
import java.util.Collection;

import org.trebor.util.Angle;

import com.orbswarm.swarmcon.vobject.IVobject;

public interface IBlockPath extends IVobject
{
  Angle getHeading();

  Shape getPath();
  
  void add(IBlock block);

  IBlock lastElement();

  int size();

  Collection<IBlock> getBlocks();

  BlockState getState();
}
