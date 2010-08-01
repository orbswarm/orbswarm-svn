package com.orbswarm.swarmcon.behavior;

import org.trebor.util.Angle;

import com.orbswarm.swarmcon.model.MotionModel;
import com.orbswarm.swarmcon.vobject.IVobject;

import static org.trebor.util.Angle.Type.HEADING;

public class FollowBehavior extends Behavior
{
  // target to follow

  IVobject target;

  // create a follow behavior

  public FollowBehavior(IVobject target)
  {
    super("Follow");
    this.target = target;
  }

  // update

  public void update(double time, MotionModel model)
  {
    model.setTargetYaw(new Angle(orb.headingTo(target), HEADING));
  }
}
