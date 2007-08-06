package com.orbswarm.swarmcomposer.swarmulator;

/**
 * @author Simran Gleason
 */
public interface Trajectory {
    /**
     * Trajectory interface
     */
    public boolean hasNextStep();
    public Vect nextStep();
    public void reset();
    public int getStepNumber();
}