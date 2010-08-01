package com.orbswarm.swarmcon.behavior;

import org.trebor.util.Angle;

import com.orbswarm.swarmcon.mobject.IMobject;
import com.orbswarm.swarmcon.model.MotionModel;

import static org.trebor.util.Angle.Type.HEADING;

public class FollowBehavior extends Behavior
{
  // target to follow

  IMobject target;

  // create a follow behavior

  public FollowBehavior(IMobject target)
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
