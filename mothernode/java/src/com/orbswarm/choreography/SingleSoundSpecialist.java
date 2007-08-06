package com.orbswarm.choreography;

import java.util.Properties;

public class SingleSoundSpecialist extends AbstractSpecialist {
    private boolean enabled = true;
    
    public void setup(OrbControl orbControl, Properties initialProperties) {
        super.setup(orbControl, initialProperties);
    }
    
    public void start() {
        if (enabled) {
            String soundFilePath = getProperty("soundFile", null);
            int orbNum = getIntProperty("orb", -1);
            if (soundFilePath != null) {
                float durationSec = orbControl.playSoundFile(orbNum, soundFilePath);
                int durationMS = (int)(1000 * durationSec);
                delayedBroadcastCommandCompleted(durationMS, "start", orbNum, soundFilePath);
            }
        }
    }

    public void stop() {
        int orbNum = getIntProperty("orb", -1);
        orbControl.stopSound(orbNum);
    }

    public void enable(boolean value) {
        enabled = value;
    }

    public void orbState(Swarm orbSwarm) {
        // N/A
    }

    public void command(String command, int orb, String property) {
        if (command.equals("volume")) {
            try {
                int vol = Integer.parseInt(property);
                orbControl.volume(orb, vol);
            } catch (Exception ex) {
            }
        }
    }
    
}
