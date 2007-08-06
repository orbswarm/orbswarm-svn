package com.orbswarm.swarmcomposer.composer;

import java.util.ArrayList;

/**
 * Strategy for randonly selecting sounds from a set.
 *
 * @author Simran Gleason
 */

public class RandomStrategy implements Strategy {
    /**
     * Select a sound from a given set.
     */
    public Sound select(Set set) {
        ArrayList sounds = set.getSounds();
        int up = sounds.size();
        if (up > 0) {
            int selection = (int)(up * Math.random());
            return (Sound)sounds.get(selection);
        } else {
            return null;
        }
    }

    public String getName() {
        return Strategy.RANDOM;
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