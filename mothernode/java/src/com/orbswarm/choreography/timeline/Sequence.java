package com.orbswarm.choreography.timeline;

import com.orbswarm.choreography.ColorSpecialist;
import com.orbswarm.choreography.OrbControl;
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
 *  Representation of a sequence of timeline events. 
 *  which can have duration.
 */
public class Sequence extends Event {
    // a sequence is an event, possibly with a following event, which may be null.

    private Event first = null;
    private Sequence next = null;
    private Sequence prev = null;
    
    public Sequence(Timeline timeline, Event parent) {
        super(timeline, parent);
        this.first = null;
        this.next = null;
    }

    public Event getFirst() {
        return this.first;
    }

    public boolean hasNext() {
        return this.next != null;
    }
    
    public Sequence getNext() {
        return this.next;
    }
    
    /**
     * Append event to end of sequence -- no matter how far down this is.
     * Take into account that this might be an initially empty sequence, in which
     * case, add the first item. 
     */
    public void appendEvent(Event event) {
        if (first == null) {
            first = event;
        } else {
            if (next == null) {
                next = new Sequence(timeline, parent);
            } 
            next.appendEvent(event);
        }
    }

    public Specialist setupSpecialist(OrbControl orbControl) {
        Specialist sp = null;
        if (first != null) {
            sp = first.setupSpecialist(orbControl);
        }
        if (next != null) {
            next.setupSpecialist(orbControl);
        }
        return sp;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("SEQ[");
        Sequence n = this;
        while (n != null) {
            if (n.getFirst() == null) {
                buf.append("<>");
            } else {
                buf.append(n.getFirst().toString());
            }
            n = n.getNext();
            if (n != null) {
                buf.append(", ");
            }
        }
        buf.append("]");
        return buf.toString();
    }

    public float calculateDuration() {
        // maybe cache?
        return getFullDuration();
    }
    
    public float getFullDuration() {
        float fd = 0;
        Sequence n = this;
        while (n != null) {
            if (n.getFirst() == null) {

            } else {
                float dur = n.getFirst().calculateDuration();
                if (dur != NO_TIME) {
                    fd += dur;
                }
            }
            n = n.getNext();
        }
        return fd;
    }

    public void adjustEventTimes() {
        float runningStart = this.startTime;
        Sequence n = this;
        while (n != null) {
            Event event = n.getFirst();
            if (event == null) {

            } else {
                float dur = event.calculateDuration();
                if (dur == NO_TIME) {
                    dur = .1f;  // a minimum?
                }
                event.setStartTime(runningStart);
                event.setEndTime(runningStart + dur);
                runningStart += dur;
            }
            n = n.getNext();
        }
    }


    public float getEndTime() {
        if (this.endTime != NO_TIME) {
            return this.endTime;
        }
        float st = startTime;
        if (st == NO_TIME) {
            st = 0.f;
        }
        return st + getFullDuration();
    }

}

    