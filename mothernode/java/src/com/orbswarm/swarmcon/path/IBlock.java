package com.orbswarm.swarmcon.path;

import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;

import org.trebor.util.Angle;

import com.orbswarm.swarmcon.vobject.IVobject;

public interface IBlock extends IVobject
{
  /**
   * Get the preceding block before this one. This may be null in which
   * case this is the head of path. 
   * 
   * @return the previous block.
   */

  IBlock getPrevious();

  /**
   * Set previous block.
   * 
   * @param previous the block which precedes this block.
   */
  
  void setPreviouse(IBlock previous);
  
  /**
   * Get the angle at which this block ends.
   * 
   * @return the end angle of this block.
   */
  
  Angle getEndAngle();
  
  /**
   * Get the end position of this this block.
   */
  
  Point2D getEndPosition();
  
  /**
   * Return the general path for this block.
   * @return the path for this block.
   */
  
  GeneralPath getPath();
}
