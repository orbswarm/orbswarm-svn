package com.orbswarm.swarmcon.view;

import java.awt.*;
import java.awt.geom.*;

import com.orbswarm.swarmcon.orb.Phantom;

public class PhantomRenderer extends ARenderer<Phantom>
{
  public void render(Graphics2D g, Phantom phantom)
  {
    IRenderable mobject = phantom.getMobject();

    // record transform and scale

    double scale = phantom.getScale();
    AffineTransform oldTransform = g.getTransform();
    g.scale(scale, scale);

    // record alpha and set alpha

    // record and scale current position

    Point2D tmpPos = phantom.getPosition();

    // position the phantom

    phantom.setPosition(phantom.getX() / scale, phantom.getY() / scale);
//    g.translate(phantom.getX() - mobject.getX(), phantom.getY() -
//      mobject.getY());

    // paint phantom

    RendererSet.renderAsPhantom(g, mobject, 0.35);

    // restore position

    phantom.setPosition(tmpPos);

    // restore transform

    g.setTransform(oldTransform);
  }

  public Shape getShape(Phantom p)
  {
    IRenderable o = p.getMobject();
    return RendererSet.getRenderer(o).getShape(o);
  }

  public IRenderable getSelected(Point2D selectionPoint, Phantom o)
  {
    return null;
  }
}
