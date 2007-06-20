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

      private Shape shape;

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
      }
         // randomize position of orb

      public void randomizePos()
      {
         Rectangle2D.Double arena = swarm.getArena();
         setPosition(arena.getX() + RND.nextDouble() * arena.getWidth(),
                     arena.getY() + RND.nextDouble() * arena.getHeight());
            //setPosition(swarm.getCenter());
      }
         // position setter

      void setPosition(double x, double y)
      {
         super.setPosition(x, y);
         model.setPosition(getX(), getY());
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

      public double getRoll()
      {
         return model.getRoll();
      }
         // get orb pitch

      public double getPitch()
      {
         return model.getPitch();
      }
         // get orb yaw

      public double getYaw()
      {
         return model.getYaw();
      }
         // get orb yaw rate

      public double getYawRate()
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
         // paint this object onto a graphics area

      public void paint(Graphics2D g)
      {
         shape = scale(createOrbShape(), ORB_DIAMETER, ORB_DIAMETER);
         setColor(g, isSelected() ? SEL_ORB_CLR : ORB_CLR);
         g.fill(translate(rotateAboutCenter(shape, -getYaw()),
                          getX(), getY()));
         Shape shadow = scale(createOrbShadowShape(), 
                             ORB_DIAMETER, ORB_DIAMETER);
         setColor(g, g.getColor().darker());
         g.fill(translate(shadow, getX(), getY()));
         
         Shape frame = scale(createOrbFrameShape(), 
                             ORB_DIAMETER, ORB_DIAMETER);
         setColor(g, ORB_FRAME_CLR);
         g.fill(translate(rotateAboutCenter(frame, -getYaw()),
                          getX(), getY()));

         Shape line = new Line2D.Double(0, 0, 0, -1);
         line = scale(line, model.getVelocity(),
                      model.getVelocity());
         line = rotate(line, 180 - model.getDirection());
         g.setStroke(new BasicStroke((float)(ORB_DIAMETER / 8),
                                     BasicStroke.CAP_ROUND,
                                     BasicStroke.JOIN_ROUND));
         setColor(g, VECTOR_CRL);
         g.draw(translate(line, getX(), getY()));
         super.paint(g);

         g.setFont(ORB_FONT);
         g.setColor(TEXT_CLR);
         drawText(g, getX() - ORB_DIAMETER / 2,
                     getY() + ORB_DIAMETER / 2, "" + getId());
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
         return createCirlcle();
      }
         // create orb shadow shape

      public Shape createOrbShadowShape()
      {
         Area shadow = new Area(createCirlcle());
         Area hole = new Area(createCirlcle());
         shadow.subtract(translate(hole, -0.06, 0.06));
         return shadow;
      }
         // create orb shadow shape

      public Shape createOrbFlairShape()
      {
         return translate(scale(createCirlcle(), .10, .10), -0.25, 0.25);
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

         double width = sin(toRadians(model.getRoll()));
         arc = new Arc2D.Double(-abs(width / 2),
                                -0.5,
                                abs(width),
                                1,
                                width > 0 ? 270 : 90, 180,
                                Arc2D.Double.OPEN);
         arcs.append(s.createStrokedShape(arc), true);

            // add longitude lines

         strokeWidth = ORB_DIAMETER / 128;
         s = new BasicStroke((float)strokeWidth,
                             BasicStroke.CAP_ROUND,
                             BasicStroke.JOIN_ROUND);

         double pitch = (360 + model.getPitch()) % 360;
         for (int i = 0; i < ORB_SPAR_COUNT; ++i)
         {
            width = sin(toRadians(pitch));
            arc = new Arc2D.Double(-0.5,
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
}
