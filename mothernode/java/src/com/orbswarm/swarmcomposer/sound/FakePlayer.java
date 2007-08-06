package com.orbswarm.swarmcomposer.sound;

import com.orbswarm.swarmcomposer.composer.Sound;
import com.orbswarm.swarmcomposer.composer.SoundFilePlayer;


/**
 * @author Simran Gleason
 */
public class FakePlayer extends SoundFilePlayer {
    private int channel;
    private boolean paused;
    private Sound currentSound = null;
    
    
    public FakePlayer(int channel) {
	this.channel = channel;
    }
    public boolean play(Sound sound) {
	currentSound = sound;
	String indent = "  ";
	for(int i=0; i < channel; i++) {
	    System.out.print(indent);
	}
	int playtime = 5 + (int)(25 * Math.random());
	System.out.println(channel + ":: " + sound.getName() + "[" + playtime + "]");
	try {
	    Thread.sleep(playtime * 1000);
	} catch (Exception ex) {
	}
	return true;
    }
    
    public void stop() {
	currentSound = null;
    }

    public void pause(boolean paused) {
	this.paused = paused;
    }

    //    public void addPlayerListener(PlayerListener ear);
    public boolean isPaused() {
	return paused;
    }
    
    public boolean isPlaying() {
	return currentSound != null;
    }
}
