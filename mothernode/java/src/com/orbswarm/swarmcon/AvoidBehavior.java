package com.orbswarm.swarmcon;

import org.trebor.util.Angle;

import static com.orbswarm.swarmcon.SwarmCon.*;
import static java.lang.Math.*;
import static org.trebor.util.Angle.Type.*;

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

      public void update(double time, MotionModel model)
      {
         // first compute what the other behavior would do

         other.update(time, model);

         // nearest is not so near, we're done

         if (orb.getNearestDistance() > SAFE_DISTANCE)
            return;

         // compute distance to nearest orb

         double nDist = orb.getNearestDistance();

         // if inside critical distance take over completely
         
         if (orb.getNearestDistance() < CRITICAL_DISTANCE)
         {
           model.setTargetYaw(new Angle(orb.headingTo(orb.getNearest()), DEGREES));
           model.setDistanceError(SAFE_DISTANCE - orb.getNearestDistance());
           return;
         }
      }
      // overide to string

      public String toString()
      {
         return other.toString() + " and " + super.toString();
      }
}
