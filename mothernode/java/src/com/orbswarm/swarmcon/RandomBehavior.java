package com.orbswarm.swarmcon;

import org.trebor.util.Angle;

import static com.orbswarm.swarmcon.SwarmCon.*;
import static org.trebor.util.Angle.Type.*;


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
