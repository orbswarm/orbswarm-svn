package com.orbswarm.swarmcon.path;

import java.awt.Shape;
import java.awt.geom.AffineTransform;

import com.orbswarm.swarmcon.util.ISelectable;

public interface IBlock extends ISelectable, Cloneable
{
  /**
   * Return the shape of the path north facing and located at 0,0.

   * @return the block path as a shape
   */
  
  Shape getPath();
  
  /** 
   * Get the length of this block.
   * 
   * @return the path length of this block.
   */

  double getLength();
  
  /**
   * Return the transform which should be applied to subsequent blocks.
   * 
   * @return the transform effect of this block.
   */
  
  AffineTransform getBlockTransform();

  /**
   * Create a copy of this block.
   * 
   * @return a separate copy of this block.
   */
  
  IBlock clone() throws CloneNotSupportedException;
}
