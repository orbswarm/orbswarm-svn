package com.orbswarm.swarmcon.behavior;

import org.trebor.util.Angle;

import com.orbswarm.swarmcon.model.IMotionModel;

import static com.orbswarm.swarmcon.util.Constants.RND;
import static org.trebor.util.Angle.Type.HEADING;

public class RandomBehavior extends Behavior
{
  public RandomBehavior()
  {
    super("Random");
  }

  public void update(double time, IMotionModel model)
  {
    model.setTargetYaw(new Angle((double)RND.nextInt(360), HEADING));
  }
}
