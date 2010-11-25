package com.orbswarm.swarmcon.path;

import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.util.Collection;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.trebor.util.PathTool.PathPoint;

import com.orbswarm.swarmcon.store.IBlockPathAdapter;
import com.orbswarm.swarmcon.view.IRenderable;

@XmlJavaTypeAdapter(IBlockPathAdapter.class)
public interface IBlockPath extends IRenderable
{
  double getLength();
  
  PathPoint getStartPoint();

  PathPoint getPathPoint(double extent);
  
  PathPoint getEndPoint();
  
  GeneralPath getPath();
  
  Rectangle2D getBounds2D();
  
  void addBefore(IBlock... blocks);

  void addAfter(IBlock... blocks);

  void replace(IBlock block);
  
  int size();

  Collection<IBlock> getBlocks();
  
  boolean remove();
  
  void nextBlock();
  
  void previouseBlock();

  void firstBlock();
  
  void lastBlock();
  
  IBlock getCurrentBlock();
  
  IBlockPath clone() throws CloneNotSupportedException;
}
