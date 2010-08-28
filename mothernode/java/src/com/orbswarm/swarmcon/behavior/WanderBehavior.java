package com.orbswarm.swarmcon.behavior;

import static com.orbswarm.swarmcon.util.Constants.RND;
import static java.lang.Math.sin;

import com.orbswarm.swarmcon.model.IMotionModel;

public class WanderBehavior extends Behavior
{
  double totalTime;
  double adjust;
  public double targetYawRate = 0;
  public double targetVelocity = 0;

  // create a Wander behavior

  public WanderBehavior()
  {
    super("Wander");
    adjust = 1 + RND.nextDouble();
  }

  // update

  public void update(double time, IMotionModel model)
  {
    totalTime += time;
    double tp = adjust * sin(totalTime / 3);
    model.setTargetVelocity(tp);
  }
}
