package com.orbswarm.swarmcon.orb;

import java.awt.Color;
import java.awt.geom.Rectangle2D;

import org.trebor.util.Angle;

import com.orbswarm.swarmcon.behavior.Behavior;
import com.orbswarm.swarmcon.io.Message;
import com.orbswarm.swarmcon.mobject.IMobject;
import com.orbswarm.swarmcon.model.MotionModel;
import com.orbswarm.swarmcon.orb.Orb.HistoryQueue;

public interface IOrb extends IMobject
{
  public Color getOrbColor();

  /** Return the current orbs motion model */

  public MotionModel getModel();

  // get swarm

  public Swarm getSwarm();

  // get orb id

  public int getId();

  // handle message

  public void handleMessage(String message);

  // add a behavior

  public void add(Behavior behavior);

  // select next behavior

  public void nextBehavior();

  // return current behaviors

  public Behavior getBehavior();

  // select previous behavior

  public void previousBehavior();

  // get orb roll

  public Angle getRoll();

  // get orb pitch

  public Angle getPitch();

  // get orb yaw

  public Angle getYaw();

  // get orb yaw rate

  public Angle getYawRate();

  // get actual current velocity

  public double getVelocity();

  // get actual current speed

  public double getSpeed();

  public void onOrbMessage(Message message);

  // get nearest mobject

  public IMobject getNearest();

  // get distance to nearest mobject

  public double getNearestDistance();

  // reset nearest

  public void resetNearest();

  // check candidate for nearness

  public void findNearest();

  public double[] calculateDistances();

  public double[] getDistances();

  public HistoryQueue getHistory();
}