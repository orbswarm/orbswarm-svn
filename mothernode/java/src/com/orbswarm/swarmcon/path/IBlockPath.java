package com.orbswarm.swarmcon.path;

import java.awt.Shape;
import java.util.Collection;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.trebor.util.Angle;

import com.orbswarm.swarmcon.store.IBlockPathAdapter;
import com.orbswarm.swarmcon.vobject.IVobject;

@XmlJavaTypeAdapter(IBlockPathAdapter.class)
public interface IBlockPath extends IVobject
{
  Angle getHeading();

  Shape getPath();
  
  void addBefore(IBlock... blocks);

  void addAfter(IBlock... blocks);

  void replace(IBlock block);
  
  int size();

  Collection<IBlock> getBlocks();
  
  boolean remove();
  
  void nextBlock();
  
  void previouseBlock();

  IBlock getCurrentBlock();
}
