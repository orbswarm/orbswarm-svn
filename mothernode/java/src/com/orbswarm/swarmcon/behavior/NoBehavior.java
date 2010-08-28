package com.orbswarm.swarmcon.behavior;

import com.orbswarm.swarmcon.model.IMotionModel;

public class NoBehavior extends Behavior
{
  public NoBehavior()
  {
    super("Do Nothing");
  }

  public void update(double time, IMotionModel model)
  {
    // model.setTargetRollRate(new Angle());
    // model.setTargetPitchRate(new Angle());
    // model.setRoll(-10);
    // model.setTargetPitchRate(new Angle(1, DEGREE_RATE));
    // model.settPitchRate(1);
  }
}
