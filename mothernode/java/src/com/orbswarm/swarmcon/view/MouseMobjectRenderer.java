package com.orbswarm.swarmcon.view;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

import org.trebor.util.ShapeTools;

import com.orbswarm.swarmcon.swing.SwarmCon;
import com.orbswarm.swarmcon.swing.SwarmCon.MouseMobject;
import com.orbswarm.swarmcon.vobject.IVobject;

import static com.orbswarm.swarmcon.Constants.ORB_DIAMETER;

public class MouseMobjectRenderer extends ARenderer<SwarmCon.MouseMobject>
{
  // shape to be drawn

  Shape shape = new Ellipse2D.Double(-ORB_DIAMETER / 4, -ORB_DIAMETER / 4,
    ORB_DIAMETER / 2, ORB_DIAMETER / 2);

  public void render(Graphics2D g, SwarmCon.MouseMobject mo)
  {
    g.setColor(Color.RED);
    g.fill(getShape(mo));
  }

  public Shape getShape(MouseMobject mo)
  {
    return ShapeTools.translate(shape, mo.getX(), mo.getY());
  }

  public IVobject getSelected(Point2D selectionPoint, MouseMobject o)
  {
    return null;
  }
}
