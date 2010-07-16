package com.orbswarm.swarmcon;

import org.trebor.util.Angle;

import static org.trebor.util.Angle.Type.HEADING;

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
      model.setTargetYaw(new Angle(orb.headingTo(target), HEADING));
    }
}
