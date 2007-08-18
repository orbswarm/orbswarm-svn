package com.orbswarm.choreography;

import com.orbswarm.swarmcomposer.composer.Sound;
import java.util.Enumeration;
import java.util.Properties;

public class MultitrackSongSpecialist extends AbstractSpecialist {
    private boolean enabled = true;
    private Sound[] tracks;
    public void setup(OrbControl orbControl, Properties initialProperties, int[] orbs) {
        super.setup(orbControl, initialProperties, orbs);
        setupTracks();
    }
    
    public void start() {
        if (enabled) {
            if (tracks != null) {
                int durationMS = 0;
                for(int i=0; i < tracks.length; i++) {
                    Sound sound = tracks[i];
                    if (sound != null) {
                        float durationSec = orbControl.playSound(i, sound);
                        durationMS = (int)(1000 * durationSec);
                    }
                }
                delayedBroadcastCommandCompleted(durationMS, "start", orbs, null);
            }
        }
    }

    public void setupTracks() {
        for(Enumeration en = properties.propertyNames(); en.hasMoreElements(); ) {
            String name = (String)en.nextElement();
            String val = properties.getProperty(name);
            if (name.equalsIgnoreCase("track0")) {
                addTrack(0, val);
            } else  if (name.equalsIgnoreCase("track1")) {
                addTrack(1, val);
            } else  if (name.equalsIgnoreCase("track2")) {
                addTrack(2, val);
            } else if (name.equalsIgnoreCase("track3")) {
                addTrack(3, val);
            } else if (name.equalsIgnoreCase("track4")) {
                addTrack(4, val);
            } else if (name.equalsIgnoreCase("track5")) {
                addTrack(5, val);
            }
        } 
    }

    public void addTrack(int trackNum, String soundFilePath) {
        if (tracks == null) {
            tracks = new Sound[6];
        }
        String base = getProperty("base", null);
        if (base != null) {
            soundFilePath = base + "/" + soundFilePath;
        }
        Sound sound = orbControl.lookupSound(soundFilePath);
        tracks[trackNum] = sound;
        float soundLength = sound.getDuration();
        float dur = getDuration();
        if (soundLength > dur) {
            setDuration(soundLength);
        }
    }
    
    public void stop() {
        if (tracks != null) {
            for(int i=0; i < tracks.length; i++) {
                Sound sound = tracks[i];
                if (sound != null) {
                    orbControl.stopSound(i);
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
