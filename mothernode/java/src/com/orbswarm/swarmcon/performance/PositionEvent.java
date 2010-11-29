package com.orbswarm.swarmcon.performance;

import org.trebor.util.PathTool.PathPoint;

import com.orbswarm.swarmcon.orb.IOrb;

public class PositionEvent extends AEvent
{
  private final PathPoint mPosition;
  private final IOrb mOrb;
  
  public PositionEvent(double executeTime, IOrb orb, PathPoint position)
  {
    super(executeTime);
    mOrb = orb;
    mPosition = position;
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
}
