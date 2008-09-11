package com.orbswarm.swarmcon;

import javax.swing.*;
import javax.swing.event.*;

import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.awt.image.*;

import java.text.NumberFormat;

import java.util.Vector;
import java.util.Calendar;
import java.util.concurrent.Delayed;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.TimeUnit;

import com.orbswarm.swarmcon.OrbIo.IOrbListener;
import org.trebor.util.Angle;

import static java.lang.System.*;
import static java.awt.Color.*;
import static java.lang.Math.*;
import static com.orbswarm.swarmcon.SwarmCon.*;
import static org.trebor.util.ShapeTools.*;
import static org.trebor.util.Angle.Type.*;

/** Representation of an  orb. */

public class Orb extends Mobject
  implements IOrbListener, com.orbswarm.choreography.Orb
{
    /** optionally draw fancy orb on screen */

    private boolean drawFancyOrb = true;

    /** is this a phantom we are painting */

    private boolean isPhantom = false;

    /** orb identifer */

    private int id;

    /** shape of the orb */

    private Shape shape = createOrbShape();

    /** settable color of this orb. */

    private Color orbColor = ORB_CLR;

    /** The lenght of the displayed history in milliseconds.  This is
     * NOT properly factored. */

    public static long historyLength = 6000;

    /** history of were this orb has been */

    private HistoryQueue history = new HistoryQueue();

    /** shadow of the orb */

    Shape shadow = createOrbShadowShape();

    /** vector line for this orb */

    Shape vectorLine = new Line2D.Double(0, 0, 0, 1);

    /** vector stroke */

    BasicStroke vectorStroke =
      new BasicStroke((float)(ORB_DIAMETER / 8),
      BasicStroke.CAP_ROUND,
      BasicStroke.JOIN_ROUND);

    /** library of behaviors available to the orb */

    private Vector<Behavior> behaviors = new Vector<Behavior>();
    private Behavior         behavior  = null;

    /** physical model of the orb, either live or sim */

    private MotionModel model;

    // nearest mobject

    private Mobject   nearest         = null;
    private double    nearestDistance = Double.MAX_VALUE;

    /** distances to all the other orbs. Calculated once per cycle. */

    private double [] distances;

    // misc globals

    protected Swarm              swarm = null;

    // construct an orb

    public Orb(Swarm swarm, MotionModel model, int id)
    {
      super(ORB_DIAMETER);
      this.model = model;
      this.swarm = swarm;
      this.distances = new double[6]; // how to get swarm size here?
      this.id = id;
      randomizePos();
    }
    // randomize position of orb

    public void randomizePos()
    {
      Rectangle2D.Double arena = swarm.getArena();
      // keep the initial positions within a smaller bounding box
      double boundX = Math.min(10., arena.getWidth());
      double boundY = Math.min(10., arena.getHeight());
      setPosition(arena.getX() + RND.nextDouble() * boundX,
      arena.getY() + RND.nextDouble() * boundY);
      //setPosition(swarm.getCenter());
    }
    // position setter

    void setPosition(double x, double y)
    {
      super.setPosition(x, y);
      model.setPosition(getX(), getY());

      // record our history

      history.add(this);
      history.removeOld();
    }

    void setOrbColor(Color val)
    {
      this.orbColor = val;
    }

    public Color getOrbColor()
    {
      if (this.orbColor == null)
      {
        return ORB_CLR;
      }
      else
      {
        return this.orbColor;
      }
    }

    /** Return the current orbs motion model */

    public MotionModel getModel()
    {
      return model;
    }
    // get swarm

    public Swarm getSwarm()
    {
      return swarm;
    }
    // get orb id

    public int getId()
    {
      return id;
    }
    // handle message

    public void handleMessage(String message)
    {
      System.out.println("Message: " + message);
    }
    // add a behavior

    public void add(Behavior behavior)
    {
      behavior.setOrb(this);
      behaviors.add(behavior);
      this.behavior = behavior;
    }
    // select next behavior

    public void nextBehavior()
    {
      if (behavior != null)
      {
        behavior = behaviors.get(
          (behaviors.indexOf(behavior) + 1) %
          behaviors.size());
      }
    }
    // return current behaviors

    public Behavior getBehavior()
    {
      return behavior;
    }
    // select previous behavior

    public void previousBehavior()
    {
      if (behavior != null)
      {
        behavior = behaviors.get(
          (behaviors.indexOf(behavior)
          + behaviors.size() - 1) %
          behaviors.size());
      }
    }
    // get orb roll

    public Angle getRoll()
    {
      return model.getRoll();
    }
    // get orb pitch

    public Angle getPitch()
    {
      return model.getPitch();
    }
    // get orb yaw

    public Angle getYaw()
    {
      return model.getYaw();
    }
    // get orb yaw rate

    public Angle getYawRate()
    {
      return model.getYawRate();
    }
    // get actual current velocity

    public double getVelocity()
    {
      return model.getVelocity();
    }
    // get actual current speed

    public double getSpeed()
    {
      return model.getSpeed();
    }
    
    // handle message from orb

    public void onOrbMessage(Message message)
    {
      model.onOrbMessage(message);
    }

    // update positon

    public void update(double time)
    {
      // update the vehicle behavior

      if (behavior != null)
        behavior.update(time, model);

      // update the model

      model.update(time);

      // set location to the model location

      setPosition(model.getPosition());

      // update children

      super.update(time);

      // we no longer know what's nearest

      resetNearest();
    }
    // get nearest mobject

    public Mobject getNearest()
    {
      if (nearest == null)
        findNearest();
      return nearest;
    }
    // get distance to nearest mobject

    public double getNearestDistance()
    {
      if (nearest == null)
        findNearest();
      return nearestDistance;
    }
    // reset nearest

    public void resetNearest()
    {
      nearest = null;
      nearestDistance = Double.MAX_VALUE;
    }
    // get centroid of swarm

    public Point2D.Double getCentroid()
    {
      return swarm.getCentroid();
    }
    // check candiate for nearness

    public void findNearest()
    {
      // find nearest other orb in the swarm

      for (Mobject other: swarm)
        if (other != this)
        {
          double distance = distanceTo(other);
          if (distance < nearestDistance)
          {
            nearest = other;
            nearestDistance = distance;
          }
        }
    }

    // calculate distances to all the other orbs

    public double[] calculateDistances()
    {
      int i=0;
      for (Mobject other: swarm)
      {
        if (other instanceof Orb)
        {
          if (other != this)
          {
            double distance = distanceTo(other);
            distances[i] = distance;
          }

          else
          {
            distances[i] = 0.d;
          }
          i++;
        }
      }
      return distances;
    }

    // return the distances array

    public double[] getDistances()
    {
      return distances;
    }

    // paint phantom version of this mobject onto the graphics area
    
    public void paint(Phantom phantom, Graphics2D g)
    {
      isPhantom = true;
      super.paint(phantom, g);
      isPhantom = false;
    }

    // paint this object onto a graphics area

    public void paint(Graphics2D g)
    {
      super.paint(g);

      // record old transform and scale to orb.

      AffineTransform old = g.getTransform();
      g.scale(ORB_DIAMETER, ORB_DIAMETER);

      // draw the command path

      paintPath(g);

      // draw orb history

      history.paint(g);

      //make the orb the center of the world

      g.translate(getX(), getY());

      // draw orb shape

      setColor(g, isSelected() ? SEL_ORB_CLR : getOrbColor()); // was: ORB_CLR
      g.fill(shape);

      // if fancy orb is to be drawn

      if (drawFancyOrb)
      {
        // draw orb shadow

        setColor(g, g.getColor().darker());
        g.fill(shadow);

        // draw orb frame

        setColor(g, ORB_FRAME_CLR);
        g.fill(rotateAboutCenter(createOrbFrameShape(), -getYaw().as(HEADING)));

        // draw vector line

        g.setStroke(vectorStroke);
        setColor(g, VECTOR_CRL);
        g.draw(
          rotate(
            scale(
              vectorLine,
              model.getVelocity(),
              model.getVelocity()),
            -model.getDirection().as(HEADING)));
      }

      // setup for drawing text

      double txX = -ORB_DIAMETER / 2;
      double txY = ORB_DIAMETER / 2;
      g.setColor(TEXT_CLR);

      // if this is not a phantom just draw teh orb id

      if (!isPhantom)
      {
        g.setFont(ORB_FONT);
        drawText(g, txX, txY, "" + getId());
      }

      // otherwise show lots of data

      else
      {
         g.setFont(PHANTOM_ORB_FONT);
        double dTxY = g.getFontMetrics().getStringBounds("W", g).getHeight();
        txY -= dTxY;
        drawText(g, txX, txY, "  ID: " + getId());
        txY -= dTxY;
        drawText(g, txX, txY, "EAST: " + SwarmCon.UtmFmt.format(getX()));
        txY -= dTxY;
        drawText(g, txX, txY, "NORH: " + SwarmCon.UtmFmt.format(getY()));
        txY -= dTxY;
        drawText(g, txX, txY, " YAW: " + (int)(getYaw().as(HEADING)));
        txY -= dTxY;
      }

      // restore old transform

      g.setTransform(old);
    }

    private static final Ellipse2D.Double bigDot = 
      new Ellipse2D.Double(-.3, -.3, .6, .6);
    private static final Ellipse2D.Double smallDot = 
      new Ellipse2D.Double(-.1, -.1, .2, .2);

    /** Paint the current active path */

    public void paintPath(Graphics2D g)
    {
      SmoothPath sp = model.getActivePath();
      
      if (sp == null)
        return;

      g.setColor(new Color(255, 0, 0, 16));
      for (Waypoint wp: sp)
      {
        AffineTransform t = g.getTransform();
        g.translate(wp.getX(), wp.getY());
        g.fill(bigDot);
        g.setTransform(t);
      }

      g.setColor(Color.BLACK);
      for (Target target: sp.getTargets())
      {
        AffineTransform t = g.getTransform();
        g.translate(target.getX(), target.getY());
        g.fill(smallDot);
        g.setTransform(t);
      }
    }

    // draw text at a given location

    public void drawText(Graphics2D g, double x, double y, String text)
    {
      drawText(g, new Point(x, y), text);
    }

    // draw text at a given location

    public void drawText(Graphics2D g, Point2D point, String text)
    {
      AffineTransform old = g.getTransform();
      g.setTransform(new AffineTransform());
      g.setFont(g.getFont().deriveFont(
        old.getScaleInstance(old.getScaleX(),
        -old.getScaleY())));

      //(float)(g.getFont().getSize() * old.getScaleX())));
      Point2D n = old.transform(point, new Point2D.Double());
      g.drawString(text, (int)n.getX(), (int)n.getY());
      g.setTransform(old);
    }

    // create orb shape

    public Shape createOrbShape()
    {
      return CIRCLE;
    }

    // create orb shadow shape

    public Shape createOrbShadowShape()
    {
      Area shadow = new Area(CIRCLE);
      shadow.subtract(
        translate(new Area(CIRCLE), -0.06, 0.06));
      return new GeneralPath(shadow);
    }

    // create orb shadow shape

    public Shape createOrbFlairShape()
    {
      return translate(scale(CIRCLE, .10, .10), -0.25, 0.25);
    }

    // create orb shape

    public Shape createOrbFrameShape()
    {
      GeneralPath arcs = new GeneralPath();

      // add a ring around the cirlce

      double strokeWidth = ORB_DIAMETER / 32;
      Stroke s = new BasicStroke((float)strokeWidth,
      BasicStroke.CAP_ROUND,
      BasicStroke.JOIN_ROUND);
      Shape arc = new Arc2D.Double(-0.5, -0.5, 1, 1, 0, 360,
      Arc2D.Double.OPEN);
      arcs.append(s.createStrokedShape(arc), true);

      // add the equator

      double width = sin(model.getRoll().as(RADIANS));
      arc = new Arc2D.Double(
        -abs(width / 2),
        -0.5,
        abs(width),
        1,
        width > 0 ? 270 : 90, 180,
        Arc2D.Double.OPEN);
      arcs.append(s.createStrokedShape(arc), true);

      // add longitude lines

      strokeWidth = ORB_DIAMETER / 128;
      s = new BasicStroke(
        (float)strokeWidth,
        BasicStroke.CAP_ROUND,
        BasicStroke.JOIN_ROUND);

      double pitch = model.getPitch().as(DEGREES);
      for (int i = 0; i < ORB_SPAR_COUNT; ++i)
      {
        width = sin(toRadians(pitch));
        arc = new Arc2D.Double(
          -0.5,
          -abs(width / 2),
          1,
          abs(width),
          pitch > 180 && pitch < 270 ||
          pitch < 90 ? 180 : 0, 180,
          Arc2D.Double.OPEN);
        arcs.append(s.createStrokedShape(arc), true);
        pitch = (pitch + 180 / ORB_SPAR_COUNT) % 360;
      }
      // return frame a composition of arcs

      return arcs;
    }

    /** Object used to store historical information about the orb. */

    class HistoryElement implements Delayed
    {
        /** position of orb */

        public Point position;

        /** the time at which this history element was recorded */

        private long inceptTime;

        /** Construct a history object.
         *
         * @param position the position of this orb at this time
         */

        public HistoryElement(Orb orb)
        {
          inceptTime = currentTimeMillis();
          position = orb.getPosition();
        }
        
        /** Get the remaining delay for this element.
         *
         * @param unit the unit of time which this will report remaing
         * delay in.
         *
         * @return the remaining delay.
         */
        
        public long getDelay(TimeUnit unit)
        {
          return unit.convert(
            historyLength - (currentTimeMillis() - inceptTime),
            TimeUnit.MILLISECONDS);
        }

        /** Compare two history elements for sorting. */

        public int compareTo(Delayed o)
        {
          HistoryElement other = (HistoryElement)o;
          if (inceptTime < other.inceptTime)
            return -1;
          if (inceptTime > other.inceptTime)
            return 1;
          return 0;
        }
    }

    /** A storage receptical for orb history. */

    public class HistoryQueue extends DelayQueue<HistoryElement>
    {
      /** Add the orb as it is at this moment to the  history.
       *
       * @param orb the orb state to be added to the history
       */

      public void add(Orb orb)
      {
        add(new HistoryElement(orb));
      }

      /** Remove all timed out elements from the queue. */

      public void removeOld()
      {
        while (poll() != null)
          ;
      }
 
      /** Paint this history. */

      public void paint(Graphics2D g)
      {
        // draw orb history

        Color oc = getOrbColor();
        Color historyColor = new Color(0, 255, 0, 4);
        //oc.getRed(), oc.getGreen(), oc.getBlue(), 4);
        g.setColor(historyColor);
        for (HistoryElement he: history)
        {
          g.translate(he.position.getX(), he.position.getY());
          g.fill(shape);
          g.translate(-he.position.getX(), -he.position.getY());
        }
      }
    };
}

