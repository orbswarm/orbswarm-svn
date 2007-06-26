package org.trebor.swarmcon;

import static org.trebor.swarmcon.SwarmCon.*;
import static java.lang.Math.*;

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
         double tr = adjust * sin(totalTime / 2);
         double tp = adjust * sin(totalTime / 3);
            //model.setTargetRollPitchRates(
         model.setTargetYawRateVelocity(
            10 * SwarmCon.joystick.getX(),
            1.1 + SwarmCon.joystick.getY());
               //0,
               //- ((SwarmCon.joystick.getY())));
      }
}