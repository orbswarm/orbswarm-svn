package com.orbswarm.swarmcon.behavior;

import org.trebor.util.Angle;

import com.orbswarm.swarmcon.model.MotionModel;

import static com.orbswarm.swarmcon.Constants.RND;
import static org.trebor.util.Angle.Type.HEADING;


public class RandomBehavior extends Behavior
{
    public RandomBehavior()
    {
      super("Random");
    }
    
    public void update(double time, MotionModel model)
    {
      model.setTargetYaw(new Angle((double)RND.nextInt(360), HEADING));
    }
}
