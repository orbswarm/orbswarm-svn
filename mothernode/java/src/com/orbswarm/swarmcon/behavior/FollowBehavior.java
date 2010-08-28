package com.orbswarm.swarmcon.behavior;

import org.trebor.util.Angle;

import com.orbswarm.swarmcon.model.IMotionModel;
import com.orbswarm.swarmcon.view.IRenderable;

public class FollowBehavior extends Behavior
{
  // target to follow

  IRenderable target;

  // create a follow behavior

  public FollowBehavior(IRenderable target)
  {
    super("Follow");
    this.target = target;
  }

  // update

  public void update(double time, IMotionModel model)
  {
    model.setTargetYaw(new Angle(orb.getPosition(), target.getPosition()));
  }
}
