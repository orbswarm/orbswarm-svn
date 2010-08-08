package com.orbswarm.swarmcon.path;

import java.awt.Shape;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.orbswarm.swarmcon.xml.IBlockAdapter;

@XmlJavaTypeAdapter(IBlockAdapter.class)
public interface IBlock
{
  /**
   * Return the change in state as a result of this block.
   * 
   * @return the relative change in state produced by this block.
   */
  
  BlockState getDeltaState();
  
  /**
   * Return the shape of the path for this block properly positioned and
   * oriented.
   * 
   * @param startAngle the starting angle of this block
   * @param startPosition the starting position of this block
   * @return the absolutely positioned and oriented block path as a shape
   */
  
  Shape getPath(BlockState startState);
  
  /**
   * Compute the path shape of this block.
   */
  
  void computePath();
}
