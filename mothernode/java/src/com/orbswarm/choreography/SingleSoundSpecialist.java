package com.orbswarm.choreography;

import com.orbswarm.swarmcomposer.composer.Sound;

import java.util.Properties;

public class SingleSoundSpecialist extends AbstractSpecialist {
    private boolean enabled = true;
    private Sound sound = null;
    
    public void setup(OrbControl orbControl, Properties initialProperties, int[] orbs) {
        super.setup(orbControl, initialProperties, orbs);
    }

    public void setProperty(String name, String val) {
        super.setProperty(name, val);
        System.out.println("SingleSoundSpecialist: setProperty(" + name + ") = " + val);
        if (name.equalsIgnoreCase("soundfile")) {
            System.out.println("SingleSound: looking up soundfile: " + val);
            sound = orbControl.lookupSound(val);
            System.out.println("             ==> " + sound + " duration: " + (sound == null ? -1.f : sound.getDuration()));
            if (sound != null) {
                setDuration(sound.getDuration());
            }
        }
    }
    
    public void start() {
        if (enabled) {
            String soundFilePath = getProperty("soundfile", null);
            System.out.println("SSS:start. soundFilePath: " + soundFilePath);
            if (soundFilePath != null) {
                long durationMS = 0;
                for(int i=0; i < orbs.length; i++) {
                    int orbNum = orbs[i];
                    if (sound != null) {
                        float durationSec = orbControl.playSound(orbNum, sound);
                        durationMS = (int)(1000 * durationSec);
                    }
                }
                delayedBroadcastCommandCompleted(durationMS, "start", orbs, sound.getName());
            }
        }
    }

    public void stop() {
        if (sound != null) {
            for(int i=0; i < orbs.length; i++) {
                int orbNum = orbs[i];
                if (sound != null) {
                    orbControl.stopSound(orbNum);
                }
            }
        }
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
