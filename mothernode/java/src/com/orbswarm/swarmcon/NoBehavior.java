package com.orbswarm.swarmcon;

import org.trebor.util.Angle;

import static org.trebor.util.Angle.Type.*;

public class NoBehavior extends Behavior
{
    public NoBehavior()
    {
      super("Do Nothing");
    }
    
    public void update(double time, MotionModel model)
    {
      model.setTargetRollRate(new Angle());
      model.setTargetPitchRate(new Angle());
      //model.setRoll(-10);
      //model.setTargetPitchRate(new Angle(1, DEGREE_RATE));
      //model.settPitchRate(1);
    }
}
