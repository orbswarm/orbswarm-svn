package com.orbswarm.choreography.timeline;

import com.orbswarm.swarmcon.IOrbControl;
import com.orbswarm.choreography.ColorSpecialist;
import com.orbswarm.choreography.Specialist;
import com.orbswarm.swarmcomposer.util.TokenReader;
import com.orbswarm.swarmcomposer.color.HSV;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Enumeration;

import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 *  Representation of an event that is triggered when an orb gets in proximity.
 */
public class ProximityEvent extends Event
{
    /** minimum distance to trigger the event **/
    private double triggerDistance = 1.0;

    /** event cannot be retriggered unless the orbs get farther than this apart. */
    private double resetDistance = 1.5;

    /** the event that actually got triggered (a copy of the original) **/
    private Event triggeredEvent = null;

    private boolean triggered = false;
    private int locusOrb = -1;
    private int encroachingOrb = -1;

    public ProximityEvent(Timeline timeline,
    Event parent,
    int locusOrb,
    int encroachingOrb,
    Event triggeringEvent)
    {
      super(timeline, parent);
      this.locusOrb = locusOrb;
      this.encroachingOrb = encroachingOrb;
      if (triggeringEvent != null)
      {
        triggeringEvent.copyAttributes(this);
      }
    }

    public boolean isTriggered()
    {
      return this.triggered;
    }

    public void setTriggered(boolean val)
    {
      this.triggered = val;
    }

    public void setTriggerDistance(double val)
    {
      this.triggerDistance = val;
    }

    public double getTriggerDistance()
    {
      return this.triggerDistance;
    }

    public double getPersonalSpaceRadius()
    {
      return getTriggerDistance();
    }

    public void setResetDistance(double val)
    {
      this.resetDistance = val;
    }

    public double getResetDistance()
    {
      return this.resetDistance;
    }

    public ProximityEvent copy()
    {
      ProximityEvent copy = new ProximityEvent(timeline, parent, locusOrb, encroachingOrb, null);
      copyAttributes(copy);
      return copy;
    }

    protected void copyAttributes(ProximityEvent copy)
    {
      super.copyAttributes(copy);
      copy.triggerDistance = this.triggerDistance;
      copy.resetDistance = this.resetDistance;
    }

    public int getLocusOrb()
    {
      return this.locusOrb;
    }

    public int getEncroachingOrb()
    {
      return this.encroachingOrb;
    }

    public void setTriggeredEvent(Event val)
    {
      this.triggeredEvent = val;
    }

    public Event getTriggeredEvent()
    {
      return this.triggeredEvent;
    }

    public String toString()
    {
      return "PROX{ o" + locusOrb + " -> o" + encroachingOrb + "}";
    }

}

