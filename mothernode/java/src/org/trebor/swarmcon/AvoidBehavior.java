package org.trebor.swarmcon;

import static org.trebor.swarmcon.SwarmCon.*;
import static java.lang.Math.*;

         // avoid wraps around another behavior and adds obsticle
         // avoidance when usefull

public class AvoidBehavior extends Behavior
{
         // wrapped behavior

      Behavior other;

         // create a follow behavior

      public AvoidBehavior(Behavior other)
      {
         super("Avoid");
         this.other = other;
      }
         // update

      public void update(double time)
      {
            // first compute what the other behavior would do

         other.update(time);
         cloneValues(other);

            // nearest is not so near, we're done

         if (orb.getNearestDistance() > SAFE_DISTANCE)
            return;

            // compute distance to nearest orb

         double nDist = orb.getNearestDistance();

            // set our distance to that of nearest

         setTargetDistance(SAFE_DISTANCE, nDist);

            // if inside critical distance take over completely
         
         if (orb.getNearestDistance() < CRITICAL_DISTANCE)
         {
            setTargetHeading(orb.headingTo(orb.getNearest()));
            return;
         }
            // otherwise average two headings weigted by how 
            // close we are to critical distance

         double weight = (nDist - CRITICAL_DISTANCE) / 
            (SAFE_DISTANCE - CRITICAL_DISTANCE);

         setTargetHeading(
            ((other.getTargetHeading() * (1 - weight) + 
              orb.headingTo(orb.getNearest()) * weight +
              360) % 360) / 2);
      }
         // overide to string

      public String toString()
      {
         return other.toString() + " and Avoid";
      }
}
