package com.orbswarm.swarmcon;

import static com.orbswarm.swarmcon.SwarmCon.*;

public class RandomBehavior extends Behavior
{
      public RandomBehavior()
      {
         super("Random");
      }

      public void update(double time, MotionModel model)
      {
         model.setYawDistance(
            RND.nextInt(360),
            RND.nextDouble() * 2 * SAFE_DISTANCE);
      }
}
