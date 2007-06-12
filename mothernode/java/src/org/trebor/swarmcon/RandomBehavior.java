package org.trebor.swarmcon;

import static org.trebor.swarmcon.SwarmCon.*;

public class RandomBehavior extends Behavior
{
      public RandomBehavior()
      {
         super("Random");
      }
      public void update(double time)
      {
         orb.getModel().setHeadingDistance(
            RND.nextInt(360),
            RND.nextDouble() * 2 * SAFE_DISTANCE);

         setTargetDistance(SAFE_DISTANCE, RND.nextDouble() * 2 * SAFE_DISTANCE);
         setTargetHeading(RND.nextInt(360));
      }
}
