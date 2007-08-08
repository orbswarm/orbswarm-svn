package com.orbswarm.swarmcon;

import com.orbswarm.choreography.Point;
import com.orbswarm.choreography.OrbControl;

import com.orbswarm.swarmcomposer.color.HSV;

import java.awt.Color;

public class OrbControlImpl implements OrbControl {
    private SwarmCon swarmCon;
    public OrbControlImpl(SwarmCon swarmCon) {
        this.swarmCon = swarmCon;
    }
    
    //
    // Implementation of methods from com.orbswarm.choreography.OrbControl
    //
    public OrbControl getOrbControl() {
        return (OrbControl)this;
    }

    // sound control methods not implemented.
    public int  playSoundFile(int orb, String soundFilePath) {return -1;}
    public void stopSound(int orb) {}
    public void volume(int orb, int volume) {}

    // only one Light control method implemented
    public void orbColor(int orbNum, int hue, int sat, int val, int time) {
        //System.out.println("SwarmCon:OrbControl orbColor(orb: " + orbNum + "HSV: [" + hue + ", " + sat + ", " + val + "])");
        float fhue = hue / 255.f;
        float fsat = sat / 255.f;
        float fval = val / 255.f;
        HSV hsv = new HSV(fhue, fsat, fval);
        // time ignored here.
        Color color = hsv.toColor();
        Orb orb = (Orb)swarmCon.swarm.getOrb(orbNum);
        orb.setOrbColor(color);
        // TODO: send color command out on OrbIO, or give it to model, or something. 
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