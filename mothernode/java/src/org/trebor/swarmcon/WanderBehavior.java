package org.trebor.swarmcon;

import static org.trebor.swarmcon.SwarmCon.*;
import static java.lang.Math.*;

public class WanderBehavior extends Behavior
{
      double totalTime;
      double adjust;

         // create a Wander behavior

      public WanderBehavior()
      {
         super("Wander");
         adjust = RND.nextDouble() * .5;
      }
         // update

      public void update(double time, MotionModel model)
      {
         totalTime += time;
         double tr = adjust * sin(totalTime / 2) + .5;
         double tp = adjust * sin(totalTime / 3) + .5;
         model.setTargetRates(tr, tp);
      }
}
