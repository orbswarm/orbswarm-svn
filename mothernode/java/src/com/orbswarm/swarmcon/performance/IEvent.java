package com.orbswarm.swarmcon.performance;

/**
 * An {@link IEvent} represents any event which might transpire
 * during a performance.  These are intended to be 
 * 
 * @author trebor
 */

public interface IEvent extends Comparable<IEvent> 
{
  /**
   * Return the execution time of this event. This time is store in
   * milliseconds were zero is meant to be the start of the performance.
   * 
   * @return execution time of event
   */
  
  double getExecuteTime();
}
