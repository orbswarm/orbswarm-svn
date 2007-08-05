package com.orbswarm.swarmcon;

import java.util.Vector;
import java.awt.geom.Point2D;

/** A collections of Mobjects. */


public class Mobjects extends Vector<Mobject>
{
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
}
