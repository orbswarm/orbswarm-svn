package com.orbswarm.swarmcon;

import static com.orbswarm.swarmcon.SwarmCon.RND;
import static java.lang.Math.sin;

public class WanderBehavior extends Behavior
{
      double totalTime;
      double adjust;
      public double targetYawRate  = 0;
      public double targetVelocity = 0;

      // create a Wander behavior

      public WanderBehavior()
      {
         super("Wander");
         adjust = 1 + RND.nextDouble();
      }
      // update

      public void update(double time, MotionModel model)
      {
         totalTime += time;
         double tp = adjust * sin(totalTime / 3);
         model.setTargetVelocity(tp);
      }
}
