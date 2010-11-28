package com.orbswarm.swarmcon.view;

import java.awt.geom.Rectangle2D;

import com.orbswarm.swarmcon.util.ISelectable;

public interface IRenderable extends ISelectable
{
  /**
   * Compute the bounds of this particular renderable.
   * 
   * @return the rectangular bounds of this renderable.
   */
  
  Rectangle2D getBounds2D();
}