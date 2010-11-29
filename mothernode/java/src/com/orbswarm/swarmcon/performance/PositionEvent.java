package com.orbswarm.swarmcon.performance;

import org.trebor.util.PathTool.PathPoint;

import com.orbswarm.swarmcon.orb.IOrb;

public class PositionEvent extends AEvent
{
  private final IOrb mOrb;
  private final PathPoint mPosition;
  private final double mVelocity;
  
  public PositionEvent(double executeTime, IOrb orb, PathPoint position, double velocity)
  {
    super(executeTime);
    mOrb = orb;
    mPosition = position;
    mVelocity = velocity;
  }

  public PathPoint getPosition()
  {
    return mPosition;
  }

  public IOrb getOrb()
  {
    return mOrb;
  }

  public String toString()
  {
    return "PositionEvent [mPosition=" + mPosition + ", mOrb=" + mOrb +
      ", getExecuteTime()=" + getExecuteTime() + "]";
  }

  public double getVelocity()
  {
    return mVelocity;
  }
}
