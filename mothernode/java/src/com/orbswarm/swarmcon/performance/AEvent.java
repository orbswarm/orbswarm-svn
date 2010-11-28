package com.orbswarm.swarmcon.performance;

public abstract class AEvent implements IEvent
{
  private final double mExecuteTime;

  public AEvent(double executeTime)
  {
    mExecuteTime = executeTime;
  }

  public double getExecuteTime()
  {
    return mExecuteTime;
  }

  public int compareTo(IEvent other)
  {
    return (int)(getExecuteTime() - other.getExecuteTime());
  }
}
