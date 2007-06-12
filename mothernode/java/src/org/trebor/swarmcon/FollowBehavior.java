package org.trebor.swarmcon;

import static org.trebor.swarmcon.SwarmCon.*;

public class FollowBehavior extends Behavior
{
         // target to follow

      Mobject target;

         // create a follow behavior

      public FollowBehavior(Mobject target)
      {
         super("Follow");
         this.target = target;
      }
         // update

      public void update(double time)
      {
         orb.getModel().setHeadingDistance(
            orb.headingTo(target),
            SAFE_DISTANCE - orb.distanceTo(target));

         setTargetHeading(orb.headingTo(target));
         setTargetDistance(SAFE_DISTANCE, orb.distanceTo(target));
      }
}
