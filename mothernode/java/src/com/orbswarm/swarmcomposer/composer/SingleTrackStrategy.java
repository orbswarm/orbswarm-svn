package com.orbswarm.swarmcomposer.composer;

import java.util.ArrayList;

/**
 * Strategy for sets that contain a single track, and play it once. 
 *
 * @author Simran Gleason
 */

public class SingleTrackStrategy implements Strategy {
    private int time;
    private int startTime;
    private boolean onceHappened = false;

    /**
     * Select a sound from a given set.
     */
    public Sound select(Set set) {
        System.out.println("ST:: select. onceHappened = " + onceHappened);
        if (onceHappened) {
            return Sound.END_SONG;
        }
        onceHappened = true;
        ArrayList sounds = set.getSounds();
        if (sounds.size() >= 1) {
            return (Sound)sounds.get(0);
        }
        return null;
    }

    public String getName() {
        return "SingleTrack";
    }

    /**
     * indicate that a new song is starting
     */
    public void startSong(Song song) {
    }

    /**
     * indicate that a new layer has been chosen
     *  (do we need this?)
     */
    public void layerChosen(Layer layer) {
        // there's no memory for a completely random choice, so we don't care when a new layer is chosen.
    }

    /**
     * indicate that a new set has been chosen
     *  (do we need this?)
     */
    public void setChosen(Set set) {
        // there's no memory for a completely random choice, so we don't care when a new set is chosen.
    }

}