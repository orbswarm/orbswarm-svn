package com.orbswarm.swarmcon.orb;

import java.awt.Color;
import java.awt.geom.Point2D;

import org.trebor.util.Angle;

import com.orbswarm.swarmcon.behavior.Behavior;
import com.orbswarm.swarmcon.io.Message;
import com.orbswarm.swarmcon.model.IMotionModel;
import com.orbswarm.swarmcon.orb.Orb.HistoryQueue;
import com.orbswarm.swarmcon.view.IRenderable;

public interface IOrb extends IRenderable
{
// heading getter

Angle getHeading();

// position getter

Point2D getPosition();

// get x position

double getX();

// get y position

double getY();

// position setter

void setPosition(Point2D position);

// position setter

void setPosition(double x, double y);

// set the heading

void setHeading(Angle heading);


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