package com.orbswarm.swarmcon.view;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;

import com.orbswarm.swarmcon.orb.IMobject;

public interface IRenderer<Type extends IMobject>
{
  public void render(Graphics2D g, IMobject o);
  
  public void renderAsPhantom(Graphics2D g, IMobject o, double phantomAlpha);
  
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
