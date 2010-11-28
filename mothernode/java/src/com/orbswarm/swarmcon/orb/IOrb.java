package com.orbswarm.swarmcon.orb;

import java.awt.Color;

import org.trebor.util.Angle;

import com.orbswarm.swarmcon.behavior.Behavior;
import com.orbswarm.swarmcon.io.Message;
import com.orbswarm.swarmcon.model.IMotionModel;
import com.orbswarm.swarmcon.orb.Orb.HistoryQueue;
import com.orbswarm.swarmcon.view.IPositionable;
import com.orbswarm.swarmcon.view.IRenderable;

public interface IOrb extends IRenderable, IPositionable
{
  public Color getOrbColor();

  /** Return the current orbs motion model */

  public IMotionModel getModel();

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

  public HistoryQueue getHistory();
  
  public void update(double time);
}