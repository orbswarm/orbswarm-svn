package org.trebor.swarmcon;

import static org.trebor.swarmcon.SwarmCon.*;

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

      public void update(double time)
      {
            // head towards centroid

         setTargetHeading(orb.headingTo(orb.getCentroid()));

            // keep nearest guy at safe distance

         setTargetDistance(distance, orb.getNearestDistance());
      }
}
