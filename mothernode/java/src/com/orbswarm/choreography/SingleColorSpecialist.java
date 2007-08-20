package com.orbswarm.choreography;

import com.orbswarm.swarmcomposer.color.HSV;

import java.util.Properties;

public class SingleColorSpecialist extends AbstractSpecialist implements ColorSpecialist {
    private boolean enabled = true;
    private HSV color = null;
    private int fadeTimeMS;
    
    public void setup(OrbControl orbControl, Properties initialProperties, int[] orbs) {
        super.setup(orbControl, initialProperties, orbs);
    }

    public void setColor(HSV color, float fadeTimeSec) {
        this.color = color;
        setDuration(fadeTimeSec);
        this.fadeTimeMS = (int)(fadeTimeSec * 1000);
    }
    
    public void start() {
        if (enabled && color != null) {
            if (fadeTimeMS < 0) {
                fadeTimeMS = 40;
            }
            int hue    = (int)(color.getHue() * 255);
            int sat    = (int)(color.getSat() * 255);
            int val    = (int)(color.getVal() * 255);
            for(int i=0; i < orbs.length; i++) {
                if (orbs[i] >= 0) {
                    orbControl.orbColor(orbs[i], hue, sat, val, fadeTimeMS);
                }
            }
            broadcastCommandCompleted("start", orbs, null);
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
