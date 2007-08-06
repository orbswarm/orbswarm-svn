package com.orbswarm.swarmcomposer.swarmulator;

/**
 * @author Simran Gleason
 */
public abstract class Segment implements Trajectory {
    protected int stepNumber = 0;
    public Segment() {
	stepNumber = 0;
    }
    public void reset() {
	stepNumber = 0;
    }
    public int getStepNumber() {
	return this.stepNumber;
    }
}
    
