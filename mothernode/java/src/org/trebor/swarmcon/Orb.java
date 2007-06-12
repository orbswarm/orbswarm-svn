package org.trebor.swarmcon;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.Vector;

import static java.lang.System.*;
import static java.awt.Color.*;
import static java.lang.Math.*;
import static org.trebor.swarmcon.SwarmCon.*;

   // abstract orb
      
public class Orb extends Mobject
{
         /** identifier counter */

      static int nextId = 0;
      
         /** orb identifer */

      private int id;

         /** shape of the orb */

      public static final Shape shape = 
         scale(createOrbShape(), ORB_DIAMETER, ORB_DIAMETER);

         /** library of behaviors available to the orb */

      private Vector<Behavior> behaviors = new Vector<Behavior>();
      private Behavior         behavior  = null;

         /** physical model of the orb, either live or sim */

      private MotionModel model;

         // nearest mobject

      private Mobject   nearest         = null;
      private double    nearestDistance = Double.MAX_VALUE;

         // misc globals

      protected Swarm              swarm = null;

         // construct an orb

      public Orb(Swarm swarm, MotionModel model)
      {
         super(ORB_DIAMETER);
         this.model = model;
         this.swarm = swarm;
         id = nextId++;
         randomizePos();
         model.setPosition(getX(), getY());
      }
         // randomize position of orb

      public void randomizePos()
      {
         Rectangle2D.Double arena = swarm.getArena();
         setPosition(arena.getX() + RND.nextDouble() * arena.getWidth(),
                     arena.getY() + RND.nextDouble() * arena.getHeight());
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
         // get orb heading

      public double getHeading()
      {
         return model.getHeading();
      }
         // get actual current velocity

      public double getVelocity()
      {
         return model.getVelocity();
      }
         /** Get the motion model assocated with this orb.
          *
          * @returns this orbs motion model
          */

      public MotionModel getModel()
      {
         return model;
      }
         // update positon

      public void update(double time)
      {
            // update the vehicle behavior

         if (behavior != null)
            behavior.update(time);

            // we no longer know what's nearest

         resetNearest();

            // update children

         super.update(time);

            // update the model

         model.update(time);

            // set location to the model location

         setPosition(model.getPosition());
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
         // paint this object onto a graphics area

      public void paint(Graphics2D g)
      {
         setColor(g, isSelected() ? SEL_ORB_CLR : ORB_CLR);
         g.fill(translate(rotateAboutCenter(shape, getHeading()),
                          getX(), getY()));

         Shape line = new Line2D.Double(0, 0, 0, -1);
         line = scale(line, getModel().getVelocity(), 
                      getModel().getVelocity());
         line = rotate(line, getModel().getTargetHeading());
         g.setStroke(new BasicStroke((float)(ORB_DIAMETER / 8),
                                     BasicStroke.CAP_ROUND,
                                     BasicStroke.JOIN_ROUND));
         setColor(g, VECTOR_CRL);
         g.draw(translate(line, getX(), getY()));
         super.paint(g);
      }
}
