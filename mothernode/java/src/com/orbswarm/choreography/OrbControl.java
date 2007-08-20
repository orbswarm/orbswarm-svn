package com.orbswarm.choreography;

import java.util.List;
import com.orbswarm.swarmcomposer.color.HSV;
import com.orbswarm.swarmcomposer.composer.Sound;

public interface OrbControl {
    //
    // Sound control methods
    //
    /** play a sound file given the path name. returns the duration of the
        sound file to be played (float seconds).
        Does not block to wait for sound to finish playing.
    */
    public float playSoundFile(int orb, String soundFilePath);
    public float playSound(int orb, Sound sound);
    public void stopSound(int orb);
    public void volume(int orb, int volume);
    /** the OrbControl is responsible for maintaining the soundFilePath -> Sound mapping,
     *  which gives us the hashkey, mp3hashkey, and duration information about sounds.
     */
    public Sound lookupSound(String soundFilePath);

    //
    // Light control methods
    //
    public void orbColor(int orb, HSV color, int fadeTimeMS);
    public void orbColorFade(int orb,
                             HSV color1,
                             HSV color2,
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

