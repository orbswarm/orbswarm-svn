package com.orbswarm.swarmcon.path;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.List;

import com.orbswarm.swarmcon.path.Dance.Layout;
import com.orbswarm.swarmcon.view.IPositionable;
import com.orbswarm.swarmcon.view.IRenderable;

public interface IDance extends IPositionable, IRenderable
{
  Rectangle2D getBounds2D();

  Shape getPath();
  
  void addBefore(IBlockPath... paths);

  void addAfter(IBlockPath... paths);

  void replace(IBlockPath path);

  boolean remove();

  int size();

  List<IBlockPath> getPaths();
  
  List<IMarker> getMarkers();
  
  void setLayout(Layout layout);

  Layout getLayout();

  void setSeperation(double distance);

  double getSeperation();

  void previousePath();
  
  void nextPath();
  
  IBlockPath getCurrentPath();

  /**
   * Get the transform for this dance.
   * 
   * @return this VObjects transform.
   */

  AffineTransform getTransform();
}