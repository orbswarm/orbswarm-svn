package com.orbswarm.swarmcon;

import org.trebor.util.Angle;

import static com.orbswarm.swarmcon.SwarmCon.SAFE_DISTANCE;
import static com.orbswarm.swarmcon.SwarmCon.RND;
import static org.trebor.util.Angle.Type.DEGREES;

public class ClusterBehavior extends Behavior
{
    double distance;
    public ClusterBehavior()
    {
      super("Cluster");
      
      // give each orb a different stand-off distance so they
      // don't all jam each other up (kinda hacky)
      
      distance = SAFE_DISTANCE + RND.nextDouble() * SAFE_DISTANCE / 2;
    }

    // update
    
    public void update(double time, MotionModel model)
    {
      // head towards centroid
      
      model.setTargetYaw(new Angle(orb.headingTo(orb.getCentroid()), DEGREES));
    }
}
