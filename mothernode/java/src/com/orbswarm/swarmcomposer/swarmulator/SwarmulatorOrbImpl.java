package com.orbswarm.swarmcomposer.swarmulator;

import com.orbswarm.choreography.Orb;
import com.orbswarm.swarmcon.Point;

import java.awt.Color;

public class SwarmulatorOrbImpl implements Orb {
    private int id=0;
    private int numOrbs;
    private double[] distances;
    public SwarmulatorOrbImpl(int id, int numOrbs) {
        this.id = id;
        this.numOrbs = numOrbs;
        distances = new double[numOrbs];
    }

    public int getId() {
        return id;
    }

    public void setDistances(double[] distances) {
        this.distances = distances;
    }
    
    public void setDistances(int[] intdistances) {
        for(int i=0; i < intdistances.length; i++) {
            distances[i] = intdistances[i];
        }
    }
    
    public double[] getDistances() {
        return distances;
    }

    public Color getOrbColor() {
        return null;
    }

    /**
     * Do we need methods for orb position and such?
     *  (yes)
     */
    public Point getPosition()
    {
      return null;
    }
}
