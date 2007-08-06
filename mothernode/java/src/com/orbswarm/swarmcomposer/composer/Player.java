package com.orbswarm.swarmcomposer.composer;



/**
 * @author Simran Gleason
 */
public interface  Player {
    public boolean play(Sound sound);
    public void stop();
    public void pause(boolean paused);
    //    public void addPlayerListener(PlayerListener ear);
    public boolean isPaused();
    public boolean isPlaying();
}
