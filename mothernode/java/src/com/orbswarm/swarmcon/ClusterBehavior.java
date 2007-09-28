package com.orbswarm.swarmcon;

import static com.orbswarm.swarmcon.SwarmCon.*;

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

         model.setTargetYaw(orb.headingTo(orb.getCentroid()));
         model.setDistanceError(distance - orb.getNearestDistance());
      }
}
