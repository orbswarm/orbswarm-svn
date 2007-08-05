package com.orbswarm.choreography;

import com.orbswarm.swarmcom.WayPoint;

public interface OrbControl {
    //
    // Sound control methods
    //
    public void playSoundFile(int orb, String soundFilePath);
    public void stopSound();

    

    //
    // Light control methods
    //
    public void orbColor(int orb, int hue, int sat, int val, int time);
    public void orbColorFade(int orb,
                             int hue1, int sat1, int val1,
                             int hue2, int sat2, int val2,
                             int time);
    //
    // Motion methods
    //
    public void followPath(WayPoint[] wayPoints);
    public void stopOrb(int orb);
    
    //
    // SoundFile -> sound hash mapping.
    //
    public void addSoundFileMapping(String soundFilePath, String soundFileHash);
    public void getSoundFileHash(String soundFilePath);
    public List getSoundFileMappingKeys();
}

