package com.orbswarm.swarmcomposer.swarmulator;

import java.util.ArrayList;
import java.util.Iterator;

public class Segments implements Trajectory {
    protected ArrayList segments;
    protected Trajectory current = null;
    protected int currentIndex = -1;
    protected int stepNumber;
    
    public Segments() {
	segments = new ArrayList();
	current = null;
	currentIndex = -1;
	stepNumber = -1;
    }

    public void addSegment(Trajectory segment) {
	segments.add(segment);
	if (segments.size() == 1) {
	    current = segment;
	    currentIndex = 0;
	}
    }

    public void reset() {
	if (segments.size() > 0) {
	    current = getSegment(0);
	    current.reset();
	    currentIndex = 0;
	    stepNumber = 0;
	} else {
	    current = null;
	    currentIndex = -1;
	    stepNumber = -1;
	}
    }

    public boolean hasNextStep() {
	//System.out.println("Segments.hjasNext() current: " + current);
	if (current == null) {
	    return false;
	}
	if (current.hasNextStep()) {
	    return true;
	}
	Trajectory next = getSegment(currentIndex + 1);
	// note: can't handle skipping across null segments. 
	if (next != null && next.hasNextStep()) {
	    return true;
	}
	return false;
    }

    public Vect nextStep() {
	stepNumber ++;
	if (current == null) {
	    return null;
	}
	if (current.hasNextStep()) {
	    return current.nextStep();
	}
	Trajectory next = getSegment(currentIndex + 1);
	// note: can't handle skipping across null segments. 
	if (next != null && next.hasNextStep()) {
	    current = next;
	    current.reset();
	    return current.nextStep();
	}
	return null;
    }

    protected Trajectory getSegment(int nth) {
	if (segments.size() > nth) {
	    return (Trajectory)segments.get(nth);
	}
	return null;
    }

    public int getStepNumber() {
	return this.stepNumber;
    }

    public void toString(StringBuffer buf) {
	buf.append("SEG{");
	for(Iterator it = segments.iterator(); it.hasNext(); ) {
	    buf.append(it.next().toString());
	    if (it.hasNext()) {
		buf.append(", ");
	    }
	}
	buf.append("}");

    }

    public String toString() {
	StringBuffer buf = new StringBuffer();
	toString(buf);
	return buf.toString();
    }
        
}