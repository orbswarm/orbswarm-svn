package com.orbswarm.swarmcomposer.composer;

/**
 * Strategy for selection of sounds from a set.
 *
 * @author Simran Gleason
 */

public interface Strategy {
    //
    // predefined strategies.
    //
    public static final String RANDOM = "random";

    /**
     * Return the name of the strategy. 
     */
    public String getName();
    
    /**
     * Select a sound from a given set.
     */
    public Sound select(Set set);

    /**
     * indicate that a new song is starting
     */
    public void startSong(Song song);

    /**
     * indicate that a new layer has been chosen
     *  (do we need this?)
     */
    public void layerChosen(Layer layer);

    /**
     * indicate that a new set has been chosen
     *  (do we need this?)
     */
    public void setChosen(Set set);

}