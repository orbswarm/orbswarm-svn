package com.orbswarm.swarmcon.behavior;

import java.awt.geom.Point2D;
import java.util.List;

import org.trebor.util.Angle;

import com.orbswarm.swarmcon.model.IMotionModel;
import com.orbswarm.swarmcon.orb.IOrb;

public class ClusterBehavior extends Behavior
{
  @SuppressWarnings("unused")
  private final List<IOrb> mSwarm;

  public ClusterBehavior(List<IOrb> swarm)
  {
    super("Cluster");
    mSwarm = swarm;
  }

  // update

  public void update(double time, IMotionModel model)
  {
    Point2D center = new Point2D.Double();
    for (IOrb orb: mSwarm)
      center.setLocation(center.getX() + orb.getX(), center.getY() + orb.getY());
    center.setLocation(center.getX() / mSwarm.size(), center.getY() / mSwarm.size());

    model.setTargetYaw(new Angle(orb.getPosition(), center));
  }
}
