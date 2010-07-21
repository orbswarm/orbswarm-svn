package com.orbswarm.swarmcon.orb;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import java.util.Vector;
import java.util.concurrent.Delayed;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.TimeUnit;

import com.orbswarm.swarmcon.behavior.Behavior;
import com.orbswarm.swarmcon.io.Message;
import com.orbswarm.swarmcon.io.OrbIo.IOrbListener;
import com.orbswarm.swarmcon.model.MotionModel;
import com.orbswarm.swarmcon.path.Point;
import com.orbswarm.swarmcon.path.SmoothPath;
import com.orbswarm.swarmcon.path.Target;
import com.orbswarm.swarmcon.path.Waypoint;

import org.trebor.util.Angle;

import static java.lang.System.currentTimeMillis;
import static com.orbswarm.swarmcon.SwarmCon.*;

import org.apache.log4j.Logger;

/** Representation of an  orb. */

public class Orb extends Mobject
  implements IOrbListener, IOrb
{
    private static Logger log = Logger.getLogger(Orb.class);

    /** orb identifer */

    private int id;

    /** color of this orb. */

    private Color orbColor = ORB_CLR;

    /** The length of the displayed history in milliseconds.  This is
     * NOT properly factored. */

    public static long historyLength = 60000;

    /** history of were this orb has been */

    private HistoryQueue history = new HistoryQueue();

    /** library of behaviors available to the orb */

    private Vector<Behavior> behaviors = new Vector<Behavior>();
    private Behavior         behavior  = null;

    /** physical model of the orb, either live or sim */

    private MotionModel model;

    // nearest mobject

    private IMobject   nearest         = null;
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

    /* (non-Javadoc)
     * @see com.orbswarm.swarmcon.orb.IOrbx#randomizePos()
     */
    public void randomizePos()
    {
      Rectangle2D.Double arena = swarm.getArena();
      // keep the initial positions within a smaller bounding box
      double boundX = Math.min(10., arena.getWidth());
      double boundY = Math.min(10., arena.getHeight());
      setPosition(
        arena.getX() + RND.nextDouble() * boundX,
        arena.getY() + RND.nextDouble() * boundY);
    }
    // position setter

    /* (non-Javadoc)
     * @see com.orbswarm.swarmcon.orb.IOrbx#setPosition(double, double)
     */
    public void setPosition(double x, double y)
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

    /* (non-Javadoc)
     * @see com.orbswarm.swarmcon.orb.IOrbx#getOrbColor()
     */
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

    /* (non-Javadoc)
     * @see com.orbswarm.swarmcon.orb.IOrbx#getModel()
     */

    public MotionModel getModel()
    {
      return model;
    }
    // get swarm

    /* (non-Javadoc)
     * @see com.orbswarm.swarmcon.orb.IOrbx#getSwarm()
     */
    public Swarm getSwarm()
    {
      return swarm;
    }
    // get orb id

    /* (non-Javadoc)
     * @see com.orbswarm.swarmcon.orb.IOrbx#getId()
     */
    public int getId()
    {
      return id;
    }
    // handle message

    /* (non-Javadoc)
     * @see com.orbswarm.swarmcon.orb.IOrbx#handleMessage(java.lang.String)
     */
    public void handleMessage(String message)
    {
      log.debug("Message: " + message);
    }
    // add a behavior

    /* (non-Javadoc)
     * @see com.orbswarm.swarmcon.orb.IOrbx#add(com.orbswarm.swarmcon.orb.Behavior)
     */
    public void add(Behavior behavior)
    {
      behavior.setOrb(this);
      behaviors.add(behavior);
      this.behavior = behavior;
    }
    // select next behavior

    /* (non-Javadoc)
     * @see com.orbswarm.swarmcon.orb.IOrbx#nextBehavior()
     */
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

    /* (non-Javadoc)
     * @see com.orbswarm.swarmcon.orb.IOrbx#getBehavior()
     */
    public Behavior getBehavior()
    {
      return behavior;
    }
    // select previous behavior

    /* (non-Javadoc)
     * @see com.orbswarm.swarmcon.orb.IOrbx#previousBehavior()
     */
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

    /* (non-Javadoc)
     * @see com.orbswarm.swarmcon.orb.IOrbx#getRoll()
     */
    public Angle getRoll()
    {
      return model.getRoll();
    }
    // get orb pitch

    /* (non-Javadoc)
     * @see com.orbswarm.swarmcon.orb.IOrbx#getPitch()
     */
    public Angle getPitch()
    {
      return model.getPitch();
    }
    // get orb yaw

    /* (non-Javadoc)
     * @see com.orbswarm.swarmcon.orb.IOrbx#getYaw()
     */
    public Angle getYaw()
    {
      return model.getYaw();
    }
    // get orb yaw rate

    /* (non-Javadoc)
     * @see com.orbswarm.swarmcon.orb.IOrbx#getYawRate()
     */
    public Angle getYawRate()
    {
      return model.getYawRate();
    }
    // get actual current velocity

    /* (non-Javadoc)
     * @see com.orbswarm.swarmcon.orb.IOrbx#getVelocity()
     */
    public double getVelocity()
    {
      return model.getVelocity();
    }
    // get actual current speed

    /* (non-Javadoc)
     * @see com.orbswarm.swarmcon.orb.IOrbx#getSpeed()
     */
    public double getSpeed()
    {
      return model.getSpeed();
    }
    
    // handle message from orb

    /* (non-Javadoc)
     * @see com.orbswarm.swarmcon.orb.IOrbx#onOrbMessage(com.orbswarm.swarmcon.io.Message)
     */
    
    public void onOrbMessage(Message message)
    {
      model.onOrbMessage(message);
    }

  // update positon

  /*
   * (non-Javadoc)
   * @see com.orbswarm.swarmcon.orb.IOrbx#update(double)
   */
    
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

    /* (non-Javadoc)
     * @see com.orbswarm.swarmcon.orb.IOrbx#getNearest()
     */
    public IMobject getNearest()
    {
      if (nearest == null)
        findNearest();
      return nearest;
    }
    // get distance to nearest mobject

    /* (non-Javadoc)
     * @see com.orbswarm.swarmcon.orb.IOrbx#getNearestDistance()
     */
    public double getNearestDistance()
    {
      if (nearest == null)
        findNearest();
      return nearestDistance;
    }
    // reset nearest

    /* (non-Javadoc)
     * @see com.orbswarm.swarmcon.orb.IOrbx#resetNearest()
     */
    public void resetNearest()
    {
      nearest = null;
      nearestDistance = Double.MAX_VALUE;
    }
    // get centroid of swarm

    /* (non-Javadoc)
     * @see com.orbswarm.swarmcon.orb.IOrbx#getCentroid()
     */
    public Point2D.Double getCentroid()
    {
      return swarm.getCentroid();
    }
    // check candidate for nearness

    /* (non-Javadoc)
     * @see com.orbswarm.swarmcon.orb.IOrbx#findNearest()
     */
    public void findNearest()
    {
      // find nearest other orb in the swarm

      for (IMobject other: swarm)
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

    /* (non-Javadoc)
     * @see com.orbswarm.swarmcon.orb.IOrbx#calculateDistances()
     */
    public double[] calculateDistances()
    {
      int i=0;
      for (IMobject other: swarm)
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

    /* (non-Javadoc)
     * @see com.orbswarm.swarmcon.orb.IOrbx#getDistances()
     */
    public double[] getDistances()
    {
      return distances;
    }

    // paint phantom version of this mobject onto the graphics area
    
    /* (non-Javadoc)
     * @see com.orbswarm.swarmcon.orb.IOrbx#paint(com.orbswarm.swarmcon.view.Phantom, java.awt.Graphics2D)
     */
    public void paint(Phantom phantom, Graphics2D g)
    {
    }

    // paint this object onto a graphics area

    /* (non-Javadoc)
     * @see com.orbswarm.swarmcon.orb.IOrbx#paint(java.awt.Graphics2D)
     */
    public void paint(Graphics2D g)
    {
    }

    private static final Ellipse2D.Double bigDot = 
      new Ellipse2D.Double(-.3, -.3, .6, .6);
    private static final Ellipse2D.Double smallDot = 
      new Ellipse2D.Double(-.1, -.1, .2, .2);
    private static final Color SmoothPathColor = new 
      Color(255, 0, 0, 16);
    private static final Color CurrentWaypointColor = new 
      Color(255, 0, 0, 128);

    /* (non-Javadoc)
     * @see com.orbswarm.swarmcon.orb.IOrbx#paintPath(java.awt.Graphics2D)
     */

    public void paintPath(Graphics2D g)
    {
      SmoothPath sp = model.getActivePath();
      
      if (sp == null)
        return;

      // draw the path

      for (Waypoint wp: sp)
      {
        g.setColor(sp.getCurrentWaypoint() == wp
          ? CurrentWaypointColor
          : SmoothPathColor);

        AffineTransform t = g.getTransform();
        g.translate(wp.getX(), wp.getY());
        g.fill(bigDot);
        g.setTransform(t);


//         g.setColor(controlPointColor);
//         g.setStroke(vectorStroke);
//         g.draw(new Line2D.Double(
//           wp, wp.getYaw().cartesian(5, wp)));
      }

      // draw the target points

      g.setColor(Color.BLACK);
      for (Target target: sp.getTargets())
      {
        AffineTransform t = g.getTransform();
        g.translate(target.getX(), target.getY());
        g.fill(smallDot);
        g.setTransform(t);
      }

//       // draw the control points at lines

//       g.setColor(controlPointColor);
//       g.setStroke(vectorStroke);
//       for (CubicCurve2D.Double curve: sp.getCurves())
//       {
//         g.draw(new Line2D.Double(curve.getP1(), curve.getCtrlP1()));
//         g.draw(new Line2D.Double(curve.getP2(), curve.getCtrlP2()));
//       }
    }

    // draw text at a given location

    /* (non-Javadoc)
     * @see com.orbswarm.swarmcon.orb.IOrbx#drawText(java.awt.Graphics2D, double, double, java.lang.String)
     */
    public void drawText(Graphics2D g, double x, double y, String text)
    {
      drawText(g, new Point(x, y), text);
    }

  // draw text at a given location

  /* (non-Javadoc)
   * @see com.orbswarm.swarmcon.orb.IOrbx#drawText(java.awt.Graphics2D, java.awt.geom.Point2D, java.lang.String)
   */
  public void drawText(Graphics2D g, Point2D point, String text)
  {
    AffineTransform old = g.getTransform();
    g.setTransform(new AffineTransform());
    g.setFont(g.getFont().deriveFont(
      AffineTransform.getScaleInstance(old.getScaleX(), -old.getScaleY())));

    // (float)(g.getFont().getSize() * old.getScaleX())));
    Point2D n = old.transform(point, new Point2D.Double());
    g.drawString(text, (int)n.getX(), (int)n.getY());
    g.setTransform(old);
  }

    /** Object used to store historical information about the orb. */

    public class HistoryElement implements Delayed
    {
        /** position of orb */

        public Point position;

        /** the velocity of the orb */

        public double velocity;

        /** the time at which this history element was recorded */

        private long inceptTime;

        /** Construct a history object.
         *
         * @param position the position of this orb at this time
         */

        public HistoryElement(IOrb orb)
        {
          inceptTime = currentTimeMillis();
          position = orb.getPosition();
          velocity = orb.getVelocity();
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

        /** Get the incept time of this element
         *
         * @return incept time of this element in milliseconds
         */

        public long getInceptTime()
        {
          return inceptTime;
        }


        /** Get the position of this element
         *
         * @return positon of the orb
         */
        
        public Point getPosition()
        {
          return position;
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
      HistoryElement last = null;

      /** Add the orb as it is at this moment to the  history.
       *
       * @param orb the orb state to be added to the history
       */

      public void add(IOrb orb)
      {
        if (last == null || (
          (currentTimeMillis() - last.getInceptTime()) > 100 && 
          !last.getPosition().equals(getPosition())))
        {
          last = new HistoryElement(orb);
          add(last);
        }
      }

      /** Remove all timed out elements from the queue. */

      public void removeOld()
      {
        while (poll() != null)
          ;
      }
     }

    public HistoryQueue getHistory()
    {
      return history;
    };
}

