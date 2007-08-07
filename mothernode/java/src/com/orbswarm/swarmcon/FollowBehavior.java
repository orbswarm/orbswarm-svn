package com.orbswarm.swarmcon;

import static com.orbswarm.swarmcon.SwarmCon.*;

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

      public void update(double time, MotionModel model)
      {
         model.setYawDistance(
            orb.headingTo(target),
            SAFE_DISTANCE - orb.distanceTo(target));
      }
}
