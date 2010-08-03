package com.orbswarm.swarmcon.behavior;

import org.trebor.util.Angle;

import com.orbswarm.swarmcon.model.MotionModel;
import com.orbswarm.swarmcon.orb.Swarm;

public class ClusterBehavior extends Behavior
{
  private final Swarm mSwarm;

  public ClusterBehavior(Swarm swarm)
  {
    super("Cluster");
    mSwarm = swarm;
  }

  // update

  public void update(double time, MotionModel model)
  {
    model.setTargetYaw(new Angle(orb.getPosition(), mSwarm.getPosition()));
  }
}
