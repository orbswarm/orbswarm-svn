package com.orbswarm.choreography;

/**
 * Abstraction of a Swarm of Orbs, giving the information needed by Specialist
 * objects.
 */

public interface Swarm {
    public Orb getOrb(int orbNum);
    public int numOrbs();
}
