package com.orbswarm.swarmcomposer.swarmulator;

/**
 * @author Simran Gleason
 */
public class ArcSegment extends Segment {
    protected double currentTheta;
    protected double arcTheta;
    protected double theta0;
    protected double thetaN;
    protected double deltaTheta;
    protected double timeInSteps;
    protected double deltaR;
    protected Vect v;
    protected double radius;

    /**
     * LineSegment implements a constant velocity over a given number of steps.
     */
    public ArcSegment(double theta0, double thetaN, double radius, double timeInSteps) {
	super();
	this.timeInSteps = timeInSteps;
	this.theta0 = theta0;
	this.currentTheta = theta0;
	this.thetaN = thetaN;
	this.arcTheta = thetaN - theta0;
	this.deltaTheta = arcTheta / timeInSteps;
	this.radius = radius;
	double arcLength = arcLength(deltaTheta, radius);
	this.deltaR = arcLength / timeInSteps;
	v = Vect.createPolarDegrees(deltaR, currentTheta);
    }
    public void reset() {
	super.reset();
	currentTheta = theta0;
	v = Vect.createPolarDegrees(deltaR, currentTheta);
    }
    public boolean hasNextStep() {
	return stepNumber < timeInSteps;
    }
    public Vect nextStep() {
	stepNumber++;
	currentTheta += deltaTheta;
	v = Vect.createPolarDegrees(deltaR, currentTheta);
	return v;
    }

    public double arcLength(double thetaDegrees, double radius) {
	double thetaRadians = Math.toRadians(thetaDegrees);
	// 2 pi * r * theta / 2 pi;
	return thetaRadians * radius;
    }

    public String toString() {
	return "Arc[th: " + theta0 + " - " + thetaN + ", " + radius + " in " + timeInSteps + "]";
    }

}