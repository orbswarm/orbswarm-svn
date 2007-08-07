package com.orbswarm.swarmcomposer.swarmulator;

import com.orbswarm.choreography.Swarm;
import com.orbswarm.choreography.Orb;

public class SwarmulatorSwarmImpl implements Swarm {
    private int id=0;
    private int numOrbs;
    private SwarmulatorOrbImpl[] orbs;

    public SwarmulatorSwarmImpl(int numOrbs) {
        this.numOrbs = numOrbs;
        orbs = new SwarmulatorOrbImpl[numOrbs];
        for(int i=0; i < numOrbs; i++) {
            orbs[i] = new SwarmulatorOrbImpl(i, numOrbs);
        }
    }

    public int getNumOrbs() {
        return numOrbs;
    }

    public Orb getOrb(int numOrb) {
        return orbs[numOrb];
    }
}
