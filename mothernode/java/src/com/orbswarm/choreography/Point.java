package com.orbswarm.choreography;

/**
 * Abstraction of a Swarm of Orbs, giving the information needed by Specialist
 * objects.
 */

public interface Point {
    public double getX();
    public double getY();
    public void setLocation(double x, double y);
}
