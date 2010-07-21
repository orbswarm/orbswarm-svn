package com.orbswarm.swarmcon.orb;

import java.util.Vector;
import java.awt.geom.Point2D;



/** A collections of Mobjects. */


@SuppressWarnings("serial")
public class Mobjects extends Vector<AMobject>
{
   /** Find nearest   mobject to point.
    *
    * @param point the selection point
    * @return nearest matching object or null if none
    */
   
   public AMobject findSelected(Point2D.Double point)
   {
      double distance;
      double bestDistance = Double.MAX_VALUE;
      AMobject bestMoblect = null;

      for (AMobject mobject: this)
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
