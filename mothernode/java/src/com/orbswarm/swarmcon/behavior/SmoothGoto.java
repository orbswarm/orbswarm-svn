package com.orbswarm.swarmcon.behavior;

import com.orbswarm.swarmcon.model.IMotionModel;

/** Command the orb to goto an absolute position in a smooth way. */

public class SmoothGoto extends Behavior
{
  // construct a behavior

  public SmoothGoto()
  {
    super("SmoothGoto");
  }

  // update the state of the orb, time (in seconds) provided

  public void update(double time, IMotionModel model)
  {
    // check for a point
  }
}
