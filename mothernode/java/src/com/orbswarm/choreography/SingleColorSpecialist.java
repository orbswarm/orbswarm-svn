package com.orbswarm.choreography;

import java.util.Properties;

public class SingleColorSpecialist extends AbstractSpecialist {
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
            orbControl.orbColor(orbNum, hue, sat, val, time);
            broadcastCommandCompleted("start", orbNum, null);
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
        // doesn't really do anything.
    }
    
}
