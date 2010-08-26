package com.orbswarm.swarmcon.view;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Point2D;


public interface IRenderer<Type extends IRenderable>
{
  /**
   * Render a given Vobject onto a given graphics context
   * 
   * @param g the graphics onto which to render the vobject
   * @param o the vobject to render
   */
  
  public void render(Graphics2D g, Type o);

  /**
   * Render a given Vobject in phantom form onto a given graphics context
   * 
   * @param g the graphics onto which to render the vobject
   * @param o the vobject to render in phantom form
   */

  public void renderAsPhantom(Graphics2D g, Type o, double phantomAlpha);

  public void drawText(Graphics2D g, double x, double y, String text);

  public void drawText(Graphics2D g, Point2D point, String text);

  /**
   * Establish this {@link IRenderable} is selected by clicking at
   * selectionPoint. A {@link IRenderable} is selected if the {@link Shape}
   * returned by {@link #getShape(IRenderable)}
   * {@link Shape#contains(Point2D)} selectionPoint, and the
   * {@link IRenderable} is the closest to the selectionPoint.
   * 
   * @param selectionPoint the point to select from
   * @param o the object to search for candidate {@link IRenderable}s.
   * @return the selected {@link IRenderable} or null if no valid candidates
   *         exist.
   */
  
  public IRenderable getSelected(Point2D selectionPoint, Type o);
  
  /**
   * Get the bounding shape of the {@link IRenderable}, used for selection.
   * 
   * @return the bounding shape of the {@link IRenderable}.
   */
  
  public Shape getShape(Type o);
  
  /**
   * Get the distance to the {@link IRenderable}, used for selection.
   * 
   * @param point the point to compute the distance from.
   * 
   * @return the distance from the point to the {@link IRenderable}.
   */
  
  public double getDistanceTo(Point2D point, Type o);
  
  /**
   * Get current master alpha value.
   * 
   * @return normalized value (0..1) of master alpha
   */

  public double getMasterAlpha();

  /**
   * Set current master alpha value.
   * 
   * @param masterAlpha normalized master alpha value from 0 to 1
   */

  public void setMasterAlpha(double masterAlpha);
}
