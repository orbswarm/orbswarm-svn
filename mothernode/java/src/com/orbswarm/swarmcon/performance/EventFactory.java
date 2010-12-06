package com.orbswarm.swarmcon.performance;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.trebor.util.PathTool;
import org.trebor.util.Rate;

import com.orbswarm.swarmcon.orb.IOrb;
import com.orbswarm.swarmcon.path.IBlockPath;
import com.orbswarm.swarmcon.path.IDance;
import com.orbswarm.swarmcon.path.IMarker;
import com.orbswarm.swarmcon.path.SyncAction;

public class EventFactory
{
  private static Logger log = Logger.getLogger(EventFactory.class);

  private static final double END = Double.MIN_VALUE;
  private static final double ASAP= Double.MIN_VALUE;

  private static class PathProgress
  {
    private double mDistance;
    private double mTime;

    private final Collection<IEvent> mEvents;
    private final PathTool mPathTool;
    private final Rate mRate;
    private final IOrb mOrb;

    public PathProgress(Collection<IEvent> events, PathTool pathTool,
      Rate rate, IOrb orb)
    {
      mDistance = 0;
      mTime = 0;
      mEvents = events;
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
          mPathTool.getPathPoint(getDistance()), mRate.getVelocity());
      update(timeStep);
      return event;
    }

    void accumulatePositionPoints(double timeStep, double endPoint)
    {
      while (getDistance() <= endPoint && !stuck())
        mEvents.add(computePositonEventAndUpdate(timeStep));
    }
    
    boolean stuck()
    {
      return 0 == mRate.getTarget() && 0 == mRate.getVelocity();
    }
  }

  public static Collection<IEvent> create(IBlockPath path, IOrb orb, Rate velocityRate,
    double timeStep)
  {
    Collection<IEvent> events = new Vector<IEvent>();
    
    // create the path progress instance

    PathProgress pp =
      new PathProgress(events, path.getPathTool(), velocityRate, orb);

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
    
    return events;
  }

  public static Collection<IEvent> create(IDance dance, List<IOrb> orbs,
    List<Rate> velocityRates, double timeStep)
  {
    Collection<IEvent> events = new Vector<IEvent>();

    // extract and sort the markers from the dance

    List<IMarker> markers = new Vector<IMarker>();
    markers.addAll(dance.getMarkers());
    Collections.sort(markers);

    // extract the paths from the dance

    List<IBlockPath> paths = dance.getPaths();

    // create all the path progress objects

    Map<IBlockPath, PathProgress> pps =
      new HashMap<IBlockPath, PathProgress>();
    for (int i = 0; i < paths.size(); ++i)
      pps.put(paths.get(i), new PathProgress(events, paths.get(i)
        .getPathTool(), velocityRates.get(i), orbs.get(i)));

    // establish the first set of markers to adjust for

    Iterator<IMarker> markerItr = dance.getMarkers().iterator();
    
    // while there are markers

    while (markerItr.hasNext())
    {
      // walk down the chain of
      
      IMarker marker = markerItr.next();
      SyncAction sync = marker.getSyncAction();
      
      // if this marker has no synch, remove it from the
      
      // establish the time to get to the first marker
      
      double travelTime = 0;
      
      // 
      
      while (null != sync)
      {
        
        
        IMarker syncTo = sync.getSyncTo();
        
      }
    }

    return events;
  }
  
  public static List<Map<IBlockPath, Double>> extractPan(IDance dance)
  {
    List<Map<IBlockPath, Double>> plan =
      new Vector<Map<IBlockPath, Double>>();
  
    // establish the first set of markers to adjust for

    Iterator<IMarker> markerItr = dance.getMarkers().iterator();
    
    // process all the makers

    while (markerItr.hasNext())
    {
      // walk down the chain of
      
      IMarker marker = markerItr.next();
      SyncAction sync = marker.getSyncAction();
      
      // establish the time to get to this marker
      
      while (null != sync)
      {
        IMarker syncTo = sync.getSyncTo();
      }
    }
    return plan;
  }
}
