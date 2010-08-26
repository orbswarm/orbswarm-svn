package com.orbswarm.swarmcon.path;

import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.util.Collection;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.trebor.util.Angle;

import com.orbswarm.swarmcon.store.IBlockPathAdapter;
import com.orbswarm.swarmcon.view.IRenderable;

@XmlJavaTypeAdapter(IBlockPathAdapter.class)
public interface IBlockPath extends IRenderable
{
  Angle getHeading();

  Shape getPath();
  
  Rectangle2D getBounds();
  
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
