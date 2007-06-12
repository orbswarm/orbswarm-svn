package org.trebor.swarmcon;

import java.util.*;
import java.awt.*;
import java.awt.geom.*;

public class Swarm extends Mobjects
{
      // arena in which to swarm
   
   private Rectangle2D.Double arena;

      // globals

   private Point2D.Double centroid = new Point2D.Double();

      // construct a swarm

   public Swarm(Rectangle2D.Double arena)
   {
      this.arena = arena;
   }
      // randomize position of items in swarm

   public void randomize()
   {
      for (Mobject mobject: this)
         if (mobject instanceof Orb)
            ((Orb)mobject).randomizePos();
   }

      // get arena

   public Rectangle2D.Double getArena()
   {
      return arena;
   }
      /** Compute center of arena. 
       *
       * @returns center of the arena
       */

   public Point2D.Double getCenter()
   {
      return new Point2D.Double(
         arena.getX() + arena.getWidth() / 2,
         arena.getY() + arena.getHeight() / 2);
   }
      // get centroid
   
   public Point2D.Double getCentroid()
   {
      return centroid;
   }
      // select next behavoir for all orbs in swarm

   public void nextBehavior()
   {
      for (Mobject mobject: this)
         if (mobject instanceof Orb)
            ((Orb)mobject).nextBehavior();
   }
      // select previous behavoir for all orbs in swarm

   public void previousBehavior()
   {
      for (Mobject mobject: this)
         if (mobject instanceof Orb)
            ((Orb)mobject).previousBehavior();
   }
      /** Find nearest mobject to point.
       *
       * @param point the selection point
       * @return nearest matching object or null if none
       */
   
   public Mobject findSelected(Point2D.Double point)
   {
      double distance;
      double bestDistance = Double.MAX_VALUE;
      Mobject bestMoblect = null;

      for (Mobject mobject: this)
      {
         if (mobject.isSelectedBy(point))
         {
            distance = mobject.getPosition().distance(point);
            if (distance < bestDistance)
            {
               bestDistance = distance;
               bestMoblect = mobject;
            }
         }
      }
      return bestMoblect;
   }
      // update the swarm

   public void update(double time)
   {
         // establish centroid of swarm
      
      centroid.x = 0;
      centroid.y = 0;
      for (Mobject mobject: this)
      {
         centroid.x += mobject.getPosition().x;
         centroid.y += mobject.getPosition().y;
      }
      centroid.x /= size();
      centroid.y /= size();

         // update individual mobjects

      for (Mobject mobject: this)
         mobject.update(time);
   }
}

