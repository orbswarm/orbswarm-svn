package com.orbswarm.swarmcon;

import org.trebor.util.Angle;

import static com.orbswarm.swarmcon.SwarmCon.*;
import static java.lang.Math.*;
import static org.trebor.util.Angle.Type.*;

public class ClusterBehavior extends Behavior
{
    double distance;
    public ClusterBehavior()
    {
      super("Cluster");
      
      // give each orb a different standoff distance so they
      // don't all jam eachother up (kinda hacky)
      
      distance = SAFE_DISTANCE + RND.nextDouble() * SAFE_DISTANCE / 2;
    }

    // update
    
    public void update(double time, MotionModel model)
    {
      // head towards centroid
      
      model.setTargetYaw(new Angle(orb.headingTo(orb.getCentroid()), DEGREES));
      model.setDistanceError(distance - orb.getNearestDistance());
    }
}
