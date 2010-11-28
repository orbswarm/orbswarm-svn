package com.orbswarm.swarmcon.performance;

import com.orbswarm.swarmcon.view.IRenderable;

/**
 * A performance is a collection of decoupled events which form a
 * choreographic performance. An event might be a waypoint command,
 * sound cue or lighting cue, for example. A performance can be
 * started, in which point it will feed out events as they become ready to
 * execute.
 * 
 * @author trebor
 */

public interface IPerformance extends IRenderable
{
  /**
   * Add an event to this performance. Events may be added in any order and
   * will be inserted into the performance based on execution time.
   * 
   * @param event the event to add to the performance.
   */
  
  void add(IEvent event);
  
  /**
   * Remove an event from this performance.
   * 
   * @param event the event to remove from the performance.
   * @return true if the event existed in the performance.
   */
  
  boolean remove(IEvent event);
  
  /**
   * Get the next event in the performance, if the performance has reached
   * an events execution time. If requested the call will block until the
   * next event is ready to execute, otherwise it will return null, if no
   * event is ready to execute. If asked to block, and reset
   * {@link getEvent} will return null.
   * 
   * @param block if true block until the next event is ready to execute.
   * @return an event which should be executed now, or null
   */
  
  IEvent getEvent(boolean block);
  
  /**
   * Start the performance. The performance will continue to run until the
   * time of last event in performance has been reached, or the performance
   * has been reset.
   */
  
  void start();
  
  /**
   * Stop the performance and reset the time to the beginning.
   */
  
  void reset();
  
  /**
   * Return the duration of the performance in seconds.
   * 
   * @return performance duration in seconds.
   */
  
  double getDuration();
  
  /**
   * Return the current time of the performance in seconds.
   * 
   * @return the current time of the performance in seconds.
   */
  
  double getCurrentTime();
  
  /**
   * Return true if the performance is running.
   * 
   * @return true if the performance is running.
   */
  
  boolean isRunning();
}
