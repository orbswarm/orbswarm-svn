package com.orbswarm.choreography;

import java.util.List;

public interface OrbControl {
    //
    // Sound control methods
    //
    /** play a sound file given the path name. returns the duration of the
        sound file to be played.
        Does not block to wait for sound to finish playing.
    */
    public int playSoundFile(int orb, String soundFilePath);
    public void stopSound(int orb);
    public void volume(int orb, int volume);
    

    //
    // Light control methods
    //
    public void orbColor(int orb, int hue, int sat, int val, int fadeTimeMS);
    public void orbColorFade(int orb,
                             int hue1, int sat1, int val1,
                             int hue2, int sat2, int val2,
                             int fadeTimeMS);
    //
    // Motion methods
    //
    public void followPath(Point[] wayPoints);
    public void stopOrb(int orb);
    
    //
    // SoundFile -> sound hash mapping.
    //
    public void   addSoundFileMapping(String soundFilePath, String soundFileHash);
    public String getSoundFileHash(String soundFilePath);
    public List   getSoundFileMappingKeys();
}

