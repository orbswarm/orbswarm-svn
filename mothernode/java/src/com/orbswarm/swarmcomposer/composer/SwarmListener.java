package com.orbswarm.swarmcomposer.composer;

/**
 * @author Simran Gleason
 */
public interface SwarmListener {
    public void updateSwarmDistances(double radius, int nbeasties, int[][] distances);
}