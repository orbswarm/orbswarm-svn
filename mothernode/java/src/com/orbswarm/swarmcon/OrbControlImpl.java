package com.orbswarm.swarmcon;

import com.orbswarm.choreography.Point;
import com.orbswarm.choreography.OrbControl;

import com.orbswarm.swarmcomposer.color.HSV;
import com.orbswarm.swarmcomposer.composer.Sound;
import com.orbswarm.swarmcomposer.composer.SoundFilePlayer;
import com.orbswarm.swarmcomposer.sound.SimpleJavaPlayer;

import java.awt.Color;

public class OrbControlImpl implements OrbControl {
    private SwarmCon swarmCon;
    private SoundFilePlayer[] soundFilePlayers;
    
    public OrbControlImpl(SwarmCon swarmCon) {
        this.swarmCon = swarmCon;
        setupSoundPlayers(6); // TODO: generalize
    }

    public void setupSoundPlayers(int n) {
        soundFilePlayers = new SoundFilePlayer[n];
        for(int i=0; i < n; i++) {
            soundFilePlayers[i] = new SimpleJavaPlayer(i);
        }
    }
    
    //
    // Implementation of methods from com.orbswarm.choreography.OrbControl
    //
    public OrbControl getOrbControl() {
        return (OrbControl)this;
    }

    // sound control methods not implemented.
    // return length of sound in MS
    public float playSoundFile(int orbNum, String soundFilePath) {
        Sound sound = lookupSound(soundFilePath);
        return playSound(orbNum, sound);
    }
    
    public float playSound(int orbNum, Sound sound) {
        System.out.println("ORI: playsound(" + orbNum + ", " + sound + ")");
        SoundFilePlayer player = getSoundPlayer(orbNum);
        playOnThread(player, sound);
        return sound.getDuration();
    }

    private void playOnThread(SoundFilePlayer player, Sound sound) {
        final Sound _sound = sound;
        final SoundFilePlayer _player = player;
        
        new Thread() {
            public void run()  {
                System.out.println("Playing sound" + _sound + " on thread.");
                _player.play(_sound);
            }
        }.start();
    }
    
    private SoundFilePlayer getSoundPlayer(int orbNum) {
        // TODO: decide if we're in production or simulation.
        return soundFilePlayers[orbNum];
    }
    
    public Sound lookupSound(String soundFilePath) {
        //TODO: here's where we get the sound from the master soundfile -> {key, mp3key, duration} list
        Sound sound = new Sound(null, soundFilePath);
        sound.setHash("foo_hash");
        sound.setDuration(11.f);
        return sound;
    }
    
    public void stopSound(int orb) {}
    public void volume(int orb, int volume) {}

    // only one Light control method implemented
    public void orbColor(int orbNum, int hue, int sat, int val, int timeMS) {
        System.out.println("SwarmCon:OrbControl orbColor(orb: " + orbNum + "HSV: [" + hue + ", " + sat + ", " + val + "])");
        float fhue = hue / 255.f;
        float fsat = sat / 255.f;
        float fval = val / 255.f;
        final Orb orb = (Orb)swarmCon.swarm.getOrb(orbNum);
        Color prevOrbColor = orb.getOrbColor();
        final HSV prevHSV = HSV.fromColor(prevOrbColor);
        final HSV hsv = new HSV(fhue, fsat, fval);
        if (timeMS <= 0) {
            Color color = hsv.toColor();
            orb.setOrbColor(color);
        } else {
            final int _timeMS = timeMS;
            new Thread() {
                public void run()  {
                    fadeColor(orb, prevHSV, hsv, _timeMS, 100);
                }
            }.start();
        }
        // TODO: send color command out on OrbIO, or give it to model, or something. 
    }

    public void fadeColor(Orb orb, HSV prev, HSV target, int timeMS, int slewMS) {
        int steps = timeMS / slewMS;
        float hue      = prev.getHue();
        float sat      = prev.getSat();
        float val      = prev.getVal();
        float hueDelta = (target.getHue() - hue) / steps;
        float satDelta = (target.getSat() - sat) / steps;
        float valDelta = (target.getVal() - val) / steps;
        for(int i=0; i < steps; i++) {
            float h1 = hue + i * hueDelta;
            float s1 = sat + i * satDelta;
            float v1 = val + i * valDelta;
            Color stepColor = (new HSV(h1, s1, v1)).toColor();
            orb.setOrbColor(stepColor);
            try {
                Thread.sleep(slewMS);
            } catch (InterruptedException ex) {
            }
        }
        orb.setOrbColor(target.toColor());
    }
        
            
            
    
    public void orbColorFade(int orb,
                             int hue1, int sat1, int val1,
                             int hue2, int sat2, int val2,
                             int time) {}

    //
    // Motion methods
    //
    public void followPath(com.orbswarm.choreography.Point[] wayPoints) {}
    public void stopOrb(int orb) {}
    
    //
    // SoundFile -> sound hash mapping.
    //
    public void   addSoundFileMapping(String soundFilePath, String soundFileHash) {}
    public String getSoundFileHash(String soundFilePath) {return null;}
    public java.util.List   getSoundFileMappingKeys() {return null;}
}