package com.orbswarm.swarmcon.behavior;

import com.orbswarm.swarmcon.model.IMotionModel;

// avoid wraps around another behavior and adds obstacle
// avoidance when useful

public class AvoidBehavior extends Behavior
{
  // wrapped behavior

  Behavior other;

  // create a follow behavior

  public AvoidBehavior(Behavior other)
  {
    super("Avoid");
    this.other = other;
  }

  // update

  public void update(double time, IMotionModel model)
  {
    throw new UnsupportedOperationException();
//    
//    // first compute what the other behavior would do
//
//    other.update(time, model);
//
//    // nearest is not so near, we're done
//
//    if (orb.getNearestDistance() > SAFE_DISTANCE)
//      return;
//
//    // if inside critical distance take over completely
//
//    if (orb.getNearestDistance() < CRITICAL_DISTANCE)
//    {
//      model.setTargetYaw(new Angle(orb.getPosition(), orb.getNearest()
//        .getPosition()));
//      return;
//    }
  }

  // override to string

  public String toString()
  {
    return other.toString() + " and " + super.toString();
  }
}
