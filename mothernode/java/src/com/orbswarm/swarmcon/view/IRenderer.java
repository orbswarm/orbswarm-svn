package com.orbswarm.swarmcon.view;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;

import com.orbswarm.swarmcon.vobject.IVobject;

public interface IRenderer<Type extends IVobject>
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
