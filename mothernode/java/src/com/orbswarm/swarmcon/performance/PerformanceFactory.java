package com.orbswarm.swarmcon.performance;

import org.trebor.util.PathTool;
import org.trebor.util.Rate;

import com.orbswarm.swarmcon.orb.IOrb;
import com.orbswarm.swarmcon.path.IBlockPath;

public class PerformanceFactory
{
  /** Velocity rate profile to follow */

  // private static Rate velocityRate = new Rate("Velocity", 0, 1.0, 0.08);
  public static double SECONDS_BETWEEN_PATH_POINTS = 0.5;

  private static class PathProgress
  {
    private double mDistance;
    private double mTime;

    private final IPerformance mPerformance;
    private final PathTool mPathTool;
    private final Rate mRate;
    private final IOrb mOrb;

    public PathProgress(IPerformance performance, PathTool pathTool,
      Rate rate, IOrb orb)
    {
      mDistance = 0;
      mTime = 0;
      mPerformance = performance;
      mPathTool = pathTool;
      mRate = rate;
      mOrb = orb;
    }

    double getDistance()
    {
      return mDistance;
    }

    double getTime()
    {
      return mTime;
    }

    void update(double timeStep)
    {
      mRate.update(timeStep);
      mTime += timeStep;
      mDistance += mRate.getVelocity() * timeStep;
    }

    IEvent computePositonEventAndUpdate(double timeStep)
    {
      IEvent event =
        new PositionEvent(getTime(), mOrb,
          mPathTool.getPathPoint(getDistance()));
      update(timeStep);
      return event;
    }

    void accumulatePositionPoints(double timeStep, double endPoint)
    {
      while (getDistance() <= endPoint)
        mPerformance.add(computePositonEventAndUpdate(timeStep));
    }
  }

  public static IPerformance create(IBlockPath path, IOrb orb, Rate velocityRate,
    double timeStep)
  {
    // create the performance

    IPerformance performance = new Performance();

    append(performance, path, orb, velocityRate, timeStep);
    
    return performance;
  }

  public static void append(IPerformance performance, IBlockPath path, IOrb orb, Rate velocityRate,
    double timeStep)
  {
    // create the path progress instance

    PathProgress pp =
      new PathProgress(performance, path.getPathTool(), velocityRate, orb);

    // establish the total length of the path

    double length = path.getLength();

    // assume zero orb velocity at the start of the path
    
    velocityRate.setVelocity(0);
    
    // identify how long it will take to get to max cruise speed

    double distanceToCruise = velocityRate.distanceTo(velocityRate.getMax());

    // compute the maximum achievable speed in the given distance (assuming
    // the need to slow to 0 at the end)

    double maxSpeed = (distanceToCruise * 2 > length)
      ? velocityRate.velocityIn(length / 2)
      : velocityRate.getMax();

    // accelerate to maximum achievable speed and cruise until the orb
    // needs to slow to a stop

    double acclerateDistance = velocityRate.distanceTo(maxSpeed);
    velocityRate.setTarget(maxSpeed);
    pp.accumulatePositionPoints(timeStep, length - acclerateDistance);

    // decelerate to a stop

    velocityRate.setTarget(0);
    pp.accumulatePositionPoints(timeStep, length);
  }
}
