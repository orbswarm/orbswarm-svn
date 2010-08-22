package com.orbswarm.swarmcon.path;

import java.awt.Shape;
import java.awt.geom.AffineTransform;

public interface IBlock
{
  /**
   * Return the shape of the path north facing and located at 0,0.

   * @return the block path as a shape
   */
  
  Shape getPath();
  
  /**
   * Compute the path shape of this block.
   */
  
  void computePath();
  
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
   * Returns true is this block is selected.
   * 
   * @return true if selected
   */
  
  boolean isSelected();

  /**
   * Set the selected state of this block.
   * 
   * @param selected true if selected
   */

  void setSelected(boolean selected);
}
