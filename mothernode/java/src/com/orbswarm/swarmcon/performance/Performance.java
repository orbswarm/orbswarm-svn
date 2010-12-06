package com.orbswarm.swarmcon.performance;

import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.Collections;
import java.util.PriorityQueue;
import java.util.Queue;

import org.apache.log4j.Logger;

public class Performance implements IPerformance
{
  private static final Logger log = Logger.getLogger(Performance.class);
  
  public static double NOT_RUNNING = Long.MIN_VALUE;
  private final Queue<IEvent> mEvents;
  private double mStartTime = NOT_RUNNING;
  private IEvent mOldestEvent;
  
  public Performance()
  {
    mEvents = new PriorityQueue<IEvent>();
  }
  
  public void addAll(Collection<IEvent> events)
  {
    for (IEvent event: events)
      add(event);
  }
  
  public void add(IEvent event)
  {
    if (null == mOldestEvent || event.getExecuteTime() > mOldestEvent.getExecuteTime())
      mOldestEvent = event;
    
    if (!mEvents.add(event))
      log.warn("failed to add event: " + event);
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

    while(mEvents.peek().getExecuteTime() < getCurrentTime() && block)
      try
      {
        Thread.sleep((long)(mEvents.peek().getExecuteTime() - getCurrentTime()) * 1000l);
      }
      catch (InterruptedException e)
      {
        return null;
      }
    
    // if the current element is STILL not ready, return null;
    
    if (mEvents.peek().getExecuteTime() < getCurrentTime())
      return null;
    
    // return the current event;
    
    IEvent event = mEvents.poll();
    if (mEvents.size() == 0)
      mStartTime = NOT_RUNNING;
    return event;
  }

  public void start()
  {
    mStartTime = System.currentTimeMillis();
  }

  public void reset()
  {
    mStartTime = NOT_RUNNING;
  }

  public double getDuration()
  {
    return mOldestEvent.getExecuteTime();
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

  public Collection<IEvent> getEvents()
  {
    return Collections.unmodifiableCollection(mEvents);
  }
}