package com.orbswarm.swarmcomposer.swarmulator;

/**
 * @author Simran Gleason
 */
public class LineSegment extends Segment {
    protected double theta;
    protected double distance;
    protected double timeInSteps;
    protected double deltaR;
    protected Vect v;

    /**
     * LineSegment implements a constant velocity over a given number of steps.
     */
    public LineSegment(double theta, double distance, double timeInSteps) {
	super();
	this.timeInSteps = timeInSteps;
	this.theta = theta;
	this.distance = distance;
	this.deltaR = distance / timeInSteps;
	v = Vect.createPolarDegrees(deltaR, theta);
    }
    public void reset() {
	super.reset();
	v = Vect.createPolarDegrees(deltaR, theta);
    }
    public boolean hasNextStep() {
	//System.out.println("LineSegment.hsNxt(): stepNumber: " + stepNumber + " timeInSteps: " + timeInSteps + " deltaR: " + deltaR + " theta: " + theta + " ==> " + (stepNumber < timeInSteps));
	return stepNumber < timeInSteps;
    }
    public Vect nextStep() {
	stepNumber++;
	return v;
    }

    public String toString() {
	return "Line[th: " + theta + ", " + distance + " in " + timeInSteps + "]";
    }
}