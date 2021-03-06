package com.orbswarm.swarmcon.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;

import com.orbswarm.swarmcon.orb.IOrb;
import com.orbswarm.swarmcon.orb.Orb;
import com.orbswarm.swarmcon.util.Constants;

import static java.lang.Math.sin;
import static java.lang.Math.abs;
import static java.lang.Math.toRadians;
import static org.trebor.util.Angle.Type.HEADING;
import static org.trebor.util.Angle.Type.RADIANS;
import static org.trebor.util.Angle.Type.DEGREES;
import static org.trebor.util.ShapeTools.*;
import static com.orbswarm.swarmcon.util.Constants.*;

import org.apache.log4j.Logger;
import org.trebor.util.ShapeTools;

/** Representation of an orb. */

public class OrbRenderer extends ARenderer<IOrb> implements IRenderer<IOrb>
{
  @SuppressWarnings("unused")
  private static Logger log = Logger.getLogger(OrbRenderer.class);

  private boolean drawFancyOrb = true;

  /** shape of the orb */

  private Shape shape = createOrbShape();

  /** shadow of the orb */

  Shape shadow = createOrbShadowShape();

  /** vector line for this orb */

  Shape vectorLine = new Line2D.Double(0, 0, 0, 1);

  /** vector stroke */

  BasicStroke vectorStroke = new BasicStroke((float)(ORB_DIAMETER / 8),
    BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

  public void render(Graphics2D g, IOrb mobject)
  {
    IOrb orb = (IOrb)mobject;

    // if not a phantom,

    if (!isPhantom())
    {
      // draw the command path

//      SmoothPath path = orb.getModel().getActivePath();
//      if (null != path)
//        RendererSet.render(g, path);

      // draw orb history

      renderHistory(g, orb);
    }

    // make the orb the center of the world

    g.translate(orb.getX(), orb.getY());

    // scale the rest of the drawing to the size of the orb

    g.scale(ORB_DIAMETER, ORB_DIAMETER);

    // draw orb shape

    setColor(g, orb.isSelected()
      ? SEL_ORB_CLR
      : orb.getOrbColor());
    g.fill(shape);

    // if fancy orb is to be drawn

    if (drawFancyOrb)
    {
      // draw orb shadow

      setColor(g, g.getColor().darker());
      g.fill(shadow);

      // draw orb frame

      setColor(g, ORB_FRAME_CLR);
      g.fill(rotateAboutCenter(createOrbFrameShape(orb), -orb.getYaw().as(
        HEADING)));

      // draw vector line

      g.setStroke(vectorStroke);
      setColor(g, VECTOR_CRL);
      g.draw(rotate(scale(vectorLine, orb.getModel().getVelocity(), orb
        .getModel().getVelocity()), -orb.getModel().getDirection()
        .as(HEADING)));
    }

    // setup for drawing text

    double txX = -ORB_DIAMETER / 2;
    double txY = ORB_DIAMETER / 2;
    g.setColor(TEXT_CLR);

    // if this is not a phantom just draw the orb id

    if (!isPhantom())
    {
      g.setFont(ORB_FONT);
      drawText(g, txX, txY, "" + orb.getId());
    }

    // otherwise show lots of data

    else
    {
      g.setFont(PHANTOM_ORB_FONT);
      double dTxY = g.getFontMetrics().getStringBounds("W", g).getHeight();
      txY -= dTxY;
      drawText(g, txX, txY, "  ID: " + orb.getId());
      txY -= dTxY;
      drawText(g, txX, txY, "EAST: " +
        Constants.UTM_FORMAT.format(orb.getX()));
      txY -= dTxY;
      drawText(g, txX, txY, "NORH: " +
        Constants.UTM_FORMAT.format(orb.getY()));
      txY -= dTxY;
      drawText(g, txX, txY, " YAW: " + (int)(orb.getYaw().as(HEADING)));
      txY -= dTxY;
    }
  }

  // draw text at a given location

  public Shape createOrbShape()
  {
    return CIRCLE;
  }

  // create orb shadow shape

  public Shape createOrbShadowShape()
  {
    Area shadow = new Area(CIRCLE);
    shadow.subtract(translate(new Area(CIRCLE), -0.06, 0.06));
    return new GeneralPath(shadow);
  }

  // create orb shadow shape

  public Shape createOrbFlairShape()
  {
    return translate(scale(CIRCLE, .10, .10), -0.25, 0.25);
  }

  // create orb shape

  public Shape createOrbFrameShape(IOrb orb)
  {
    GeneralPath arcs = new GeneralPath();

    // add a ring around the circle

    double strokeWidth = ORB_DIAMETER / 32;
    Stroke s = new BasicStroke((float)strokeWidth, BasicStroke.CAP_ROUND,
      BasicStroke.JOIN_ROUND);
    Shape arc = new Arc2D.Double(-0.5, -0.5, 1, 1, 0, 360, Arc2D.Double.OPEN);
    arcs.append(s.createStrokedShape(arc), true);

    // add the equator

    double width = -sin(orb.getModel().getRoll().as(RADIANS));
    arc = new Arc2D.Double(-abs(width / 2), -0.5, abs(width), 1, width > 0
      ? 270
      : 90, 180, Arc2D.Double.OPEN);
    arcs.append(s.createStrokedShape(arc), true);

    // add longitude lines

    strokeWidth = ORB_DIAMETER / 128;
    s = new BasicStroke((float)strokeWidth, BasicStroke.CAP_ROUND,
      BasicStroke.JOIN_ROUND);

    double pitch = orb.getModel().getPitch().as(DEGREES);
    for (int i = 0; i < ORB_SPAR_COUNT; ++i)
    {
      width = sin(toRadians(pitch));
      arc = new Arc2D.Double(-0.5, -abs(width / 2), 1, abs(width),
        pitch > 180 && pitch < 270 || pitch < 90
          ? 180
          : 0, 180, Arc2D.Double.OPEN);
      arcs.append(s.createStrokedShape(arc), true);
      pitch = (pitch + 180 / ORB_SPAR_COUNT) % 360;
    }
    // return frame a composition of arcs

    return arcs;
  }

  public void renderHistory(Graphics2D g, IOrb orb)
  {
    AffineTransform ot = g.getTransform();

    // draw orb history
    
    g.setColor(new Color(0, 0, 255, 32));
    for (Orb.HistoryElement he : orb.getHistory())
    {
      g.translate(he.position.getX(), he.position.getY());
      g.scale(ORB_DIAMETER, ORB_DIAMETER);
      g.fill(shape);
      g.setTransform(ot);
    }
  }

  public Shape getShape(IOrb o)
  {
    Shape shape = createOrbShape();
    shape = ShapeTools.scale(shape, ORB_DIAMETER, ORB_DIAMETER);
    shape = ShapeTools.translate(shape, o.getX(), o.getY());
    return shape;
  }
}
