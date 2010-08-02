package com.orbswarm.swarmcon.view;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.apache.log4j.Logger;

import com.orbswarm.swarmcon.path.Point;
import com.orbswarm.swarmcon.vobject.IVobject;

public abstract class ARenderer<Type extends IVobject> implements
  IRenderer<Type>
{
  @SuppressWarnings("unused")
  private static Logger log = Logger.getLogger(ARenderer.class);
  
  private boolean isPhantom = false;

  private double masterAlpha = 1.0d;

  public void renderAsPhantom(Graphics2D g, Type o, double phantomAlpha)
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
    g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(),
      (int)((color.getAlpha() / 255d) * masterAlpha * 255)));
  }

  public double getMasterAlpha()
  {
    return masterAlpha;
  }

  public void setMasterAlpha(double masterAlpha)
  {
    assert (masterAlpha >= 0 && masterAlpha <= 1);
    this.masterAlpha = masterAlpha;
  }

  public double getDistanceTo(Point2D point, Type o)
  {
    Rectangle2D bounds = getShape(o).getBounds2D();
    return point.distance(new Point2D.Double(bounds.getCenterX(), bounds.getCenterY()));
  }

  public IVobject getSelected(Point2D selectionPoint, Type o)
  {
    return getShape(o).contains(selectionPoint)
      ? o
      : null;
  }
}