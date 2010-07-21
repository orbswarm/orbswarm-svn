package com.orbswarm.swarmcon.view;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import com.orbswarm.swarmcon.orb.IMobject;
import com.orbswarm.swarmcon.path.Point;

public abstract class ARenderer<Type extends IMobject> implements IRenderer<Type>
{
  private boolean isPhantom = false;
  
  private double masterAlpha = 1.0d;
  
  public void renderAsPhantom(Graphics2D g, IMobject o, double phantomAlpha)
  {
    setPhantom(true);
    double oldAlpha = getMasterAlpha();
    setMasterAlpha(phantomAlpha * getMasterAlpha());
    render(g, o);
    setPhantom(false);
    setMasterAlpha(oldAlpha);
  }
  
  public void drawText(Graphics2D g, double x, double y, String text)
  {
    drawText(g, new Point(x, y), text);
  }

  public void drawText(Graphics2D g, Point2D point, String text)
  {
    AffineTransform old = g.getTransform();
    g.setTransform(new AffineTransform());
    g.setFont(g.getFont().deriveFont(
      AffineTransform.getScaleInstance(old.getScaleX(), -old.getScaleY())));
  
    Point2D n = old.transform(point, new Point2D.Double());
    g.drawString(text, (int)n.getX(), (int)n.getY());
    g.setTransform(old);
  }

  private void setPhantom(boolean isPhantom)
  {
    this.isPhantom = isPhantom;
  }

  protected boolean isPhantom()
  {
    return isPhantom;
  }
  
  public void setColor(Graphics2D g, Color color)
  {
     g.setColor(new Color(
                   color.getRed(),
                   color.getGreen(),
                   color.getBlue(),
                   (int)((color.getAlpha() / 255d) * 
                         masterAlpha * 255)));
  }

  public double getMasterAlpha()
  {
    return masterAlpha;
  }

  public void setMasterAlpha(double masterAlpha)
  {
    assert(masterAlpha >= 0 && masterAlpha <= 1);
    this.masterAlpha = masterAlpha;
  }
}