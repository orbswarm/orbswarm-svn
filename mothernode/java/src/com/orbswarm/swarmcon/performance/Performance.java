package com.orbswarm.swarmcon.performance;

import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;

public class Performance implements IPerformance
{
  private static final Logger log = Logger.getLogger(Performance.class);
  
  public static double NOT_RUNNING = Long.MIN_VALUE;
  private final SortedSet<IEvent> mEvents;
  private double mStartTime = NOT_RUNNING;
  private Iterator<IEvent> mEventItr;
  private IEvent mCurrentEvent;
  
  public Performance()
  {
    mEvents = new TreeSet<IEvent>();
  }
  
  public void add(IEvent event)
  {
    log.debug(String.format("adding %s @ %s", event.hashCode(), event.getExecuteTime()));
    log.debug(String.format("size: ", mEvents.size()));
    mEvents.add(event);
  }

  public boolean remove(IEvent event)
  {
    return mEvents.remove(event.getExecuteTime());
  }

  public IEvent getEvent(boolean block)
  {
    if (!isRunning())
      return null;

    // while the current event is not ready, and should block, sleep a little

    while(mCurrentEvent.getExecuteTime() < getCurrentTime() && block)
      try
      {
        Thread.sleep((long)(mCurrentEvent.getExecuteTime() - getCurrentTime()) * 1000l);
      }
      catch (InterruptedException e)
      {
        return null;
      }
    
    // if the current element is STILL not ready, return null;
    
    if (mCurrentEvent.getExecuteTime() < getCurrentTime())
      return null;
    
    // return the current event;
    
    IEvent event = mCurrentEvent;
    updateCurrentEvent();
    return event;
  }

  private void updateCurrentEvent()
  {
    if (mEventItr.hasNext())
    {
      mCurrentEvent = mEventItr.next();
    }
    else
    {
      mCurrentEvent = null;
      mStartTime = NOT_RUNNING;
    }
  }
  
  public void start()
  {
    mEventItr = mEvents.iterator();
    mStartTime = System.currentTimeMillis();
    updateCurrentEvent();
  }

  public void reset()
  {
    mStartTime = NOT_RUNNING;
  }

  public double getDuration()
  {
    return mEvents.last().getExecuteTime();
  }

  public double getCurrentTime()
  {
    return (mStartTime == NOT_RUNNING)
      ? NOT_RUNNING
      : System.currentTimeMillis() / 1000d - mStartTime;
  }

  public boolean isRunning()
  {
    return mStartTime == NOT_RUNNING
      ? false
      : getCurrentTime() > getDuration();
  }

  public Rectangle2D getBounds2D()
  {
    return new Rectangle2D.Double();
  }

  public boolean isSelected()
  {
    return false;
  }

  public void setSelected(boolean selected)
  {
    throw new UnsupportedOperationException();
  }

  public void setSuppressed(boolean suppress)
  {
    throw new UnsupportedOperationException();
  }
  
  public IPerformance clone() throws CloneNotSupportedException
  {
    throw new CloneNotSupportedException();
  }

  public Set<IEvent> getEvents()
  {
    return mEvents; //Collections.unmodifiableSet(mEvents);
  }
}