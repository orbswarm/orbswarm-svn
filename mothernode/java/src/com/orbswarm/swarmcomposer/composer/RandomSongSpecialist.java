package com.orbswarm.swarmcomposer.composer;

import com.orbswarm.choreography.Orb;
import com.orbswarm.choreography.OrbControl;
import com.orbswarm.choreography.Specialist;
import com.orbswarm.choreography.AbstractSpecialist;
import com.orbswarm.choreography.Swarm;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;

public  class RandomSongSpecialist extends AbstractSpecialist {
        private boolean enabled = true;
    
    public void setup(OrbControl orbControl, Properties initialProperties) {
        super.setup(orbControl, initialProperties);
    }
    
    public void start() {
        if (enabled) {
            int orbNum = getIntProperty("orb",   -1);
            int hue    = getIntProperty("hue",    0);
            int sat    = getIntProperty("sat",   80);
            int val    = getIntProperty("val",   80);
            int time   = getIntProperty("time", 400);
        }
    }

    public void stop() {
        // N/A
    }

    public void enable(boolean value) {
        enabled = value;
    }

    public void orbState(Swarm orbSwarm) {
        // N/A
    }

    public void command(String command, int orb, String property) {
        // handle volume and playsong commands. 
    }
}
