package com.orbswarm.choreography;

import java.awt.Color;
import com.orbswarm.swarmcon.Point;

/**
 * Abstraction of an Orb, giving the information needed by Specialist
 * objects.
 */

public interface Orb {

    public int getId();

    /**
     * Distances to all the other orbs, including self
     * (obviously distance to self will be 0.), so that
     * the index of the distances array and the orbnum are the same.
     */
    public double[] getDistances();

    public Color getOrbColor();

    /**
     * Do we need methods for orb position and such?
     *  (yes)
     */
    public Point getPosition();

}
