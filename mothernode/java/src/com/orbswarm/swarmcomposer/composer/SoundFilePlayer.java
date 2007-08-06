package com.orbswarm.swarmcomposer.composer;



/**
 * @author Simran Gleason
 */
public class  SoundFilePlayer implements Player {
    protected SoundFilePlayer delegate;

    public SoundFilePlayer() {
    }
    
    public SoundFilePlayer(SoundFilePlayer delegate) {
        this.delegate = delegate;
    }

    public boolean play(Sound sound) {
        System.out.println("SoundFilePlayer: play(" + sound + ")");
        return delegate.play(sound);
    }

    public boolean playFile(String soundPath) {
        System.out.println("SoundFilePlayer: playFile(" + soundPath + ")");
        return delegate.playFile(soundPath);
    }

    public void stop() {
        delegate.stop();
    }
    
    public void pause(boolean paused) {
        delegate.pause(paused);
    }

    /*
      public void addPlayerListener(PlayerListener ear) {
      delegate.addPlayerListener(ear);
      }
    */

    public boolean isPaused() {
        return delegate.isPaused();
    }
    
    public boolean isPlaying() {
        return delegate.isPlaying();
    }
}
