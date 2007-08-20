package com.orbswarm.swarmcon;

import com.orbswarm.choreography.Point;
import com.orbswarm.choreography.OrbControl;

import com.orbswarm.swarmcomposer.color.HSV;
import com.orbswarm.swarmcomposer.composer.Sound;
import com.orbswarm.swarmcomposer.composer.SoundFilePlayer;
import com.orbswarm.swarmcomposer.sound.SimpleJavaPlayer;
import com.orbswarm.swarmcomposer.util.TokenReader;

import java.awt.Color;
import java.util.HashMap;

public class OrbControlImpl implements OrbControl {
    private SwarmCon swarmCon;
    private SoundFilePlayer[] soundFilePlayers;
    private static HashMap soundCatalog;
    private OrbIo orbIo;

    // TODO: hook these up to toggles somehow... (or keep in SwarmCon?)
    //
    private boolean sendCommandsToOrbs = true;
    private boolean simulateColors = true;
    private boolean simulateSounds = true;

    public OrbControlImpl(SwarmCon swarmCon,
                          boolean sendCommandsToOrbs,
                          boolean simulateColors, boolean simulateSounds) {
        this.swarmCon = swarmCon;
        this.orbIo = swarmCon.getOrbIo();
        this.sendCommandsToOrbs = sendCommandsToOrbs;
        this.simulateColors = simulateColors;
        this.simulateSounds = simulateSounds;
        setupSoundPlayers(6); // TODO: generalize
    }
    static {
        readSoundCatalog();
    }
    
    public SwarmCon getSwarmCon() {
        return swarmCon;
    }
    
    public void setupSoundPlayers(int n) {
        soundFilePlayers = new SoundFilePlayer[n];
        for(int i=0; i < n; i++) {
            soundFilePlayers[i] = new SimpleJavaPlayer(i);
        }
    }

    private static void readSoundCatalog() {
        soundCatalog = new HashMap();
        try {
            TokenReader reader = new TokenReader("resources/songs/sounds.catalog");
            String path = reader.readToken();
            while (path != null) {
                float duration = reader.readFloat();
                String pcmHash = reader.readToken();
                if (pcmHash.equals("-")) {
                    pcmHash = null;
                }
                String mp3Hash = reader.readToken();
                Sound sound = new Sound(path, duration, pcmHash, mp3Hash);
                soundCatalog.put(path, sound);
                path = reader.readToken();
            }
        } catch (Exception ex) {
            System.out.println("OrbControlImpl caught exception reading sound catalog. ");
            ex.printStackTrace();
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
        float dur = sound.getDuration();
        if (simulateSounds) {
            System.out.println("ORI: playsound(" + orbNum + ", " + sound + ") dur:" + dur);
            SoundFilePlayer player = getSoundPlayer(orbNum);
            playOnThread(player, sound);
        }
        if (sendCommandsToOrbs && orbIo != null) {
            String mp3Hash = sound.getMP3Hash();
            StringBuffer buf = new StringBuffer();
            buf.append("<M1 VPF ");
            buf.append(mp3Hash);
            buf.append(">");
            String orbCmd = wrapOrbCommand(orbNum, buf.toString());
            orbIo.send(orbCmd);
        }
        return dur;
    }


    private void playOnThread(SoundFilePlayer player, Sound sound) {
        final Sound _sound = sound;
        final SoundFilePlayer _player = player;
        
        new Thread() {
            public void run()  {
                System.out.println("  Playing sound" + _sound + " on thread. " + this);
                _player.play(_sound);
            }
        }.start();
    }
    
    private SoundFilePlayer getSoundPlayer(int orbNum) {
        // TODO: decide if we're in production or simulation.
        return soundFilePlayers[orbNum];
    }
    
    public  Sound lookupSound(String soundFilePath) {
        return staticLookupSound(soundFilePath);
    }
    // OUCHY! Hacque hacque HACQUE!!!
    public static Sound staticLookupSound(String soundFilePath) {
        return (Sound)soundCatalog.get(soundFilePath);
    }
    
    public void stopSound(int orbNum) {
        if (simulateSounds) {
            SoundFilePlayer player = getSoundPlayer(orbNum);
            //System.out.println("OCI: stopSound(orb:" + orbNum + ") player: " + player);
            if (player != null) {
                player.stop();
            }
        }
        if (sendCommandsToOrbs && orbIo != null) {
            StringBuffer buf = new StringBuffer();
            buf.append("<M1 VST>");
            String orbCmd = wrapOrbCommand(orbNum, buf.toString());
            orbIo.send(orbCmd);
        }
    
    }
    
    public void volume(int orbNum, int volume) {
        if (sendCommandsToOrbs && orbIo != null) {
            StringBuffer buf = new StringBuffer();
            buf.append("<M1 VWR ");
            buf.append(volume);
            buf.append(">");
            String orbCmd = wrapOrbCommand(orbNum, buf.toString());
            orbIo.send(orbCmd);
        }
    }

    // only one Light control method implemented
    public void orbColor(int orbNum, int hue, int sat, int val, int timeMS) {
        System.out.println("SwarmCon:OrbControlImpl orbColor(orb: " + orbNum + "HSV: [" + hue + ", " + sat + ", " + val + "]@" + timeMS + ")");
        if (simulateColors) {
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
                        fadeColor(orb, prevHSV, hsv, _timeMS, 20);
                    }
                }.start();
            }
        }
        if (sendCommandsToOrbs && orbIo != null) {
            // TODO: send color command out on OrbIO, or give it to model, or something.
            // TODO: one board or two (later -- we get two light commands per orb)
            // fade:  <LH64><LS200><LV220><LT2200> to set h,s,v,time on all boards
            //        <LF> to do the fade  <L0F> to fade the first, <L1F> the second board
            String boardAddress = "";  // later: possibly independent board controls
            StringBuffer buf = new StringBuffer();
            // question: do we send all the commands in one string, or one at a time?
            // answer: yes, we can send them all on one string
            buf.append("<L" + boardAddress + "H" + hue + ">");
            buf.append("<L" + boardAddress + "S" + sat + ">");
            buf.append("<L" + boardAddress + "V" + val + ">");
            buf.append("<L" + boardAddress + "T" + timeMS + ">");
            buf.append("<LF>");
            String orbCmd = wrapOrbCommand(orbNum, buf.toString());
            orbIo.send(orbCmd);
        }
    }

    public String wrapOrbCommand(int orbNum, String message) {
        StringBuffer buf = new StringBuffer();
        buf.append("{Orb");
        buf.append(60 + orbNum); // this is an IP Addr?
        buf.append(" ");
        buf.append(message);
        buf.append("}");
        return buf.toString();
    }

    // simulate the color fading behaviour on an orb. 
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
        swarmCon.repaint(); // todo: only if swarmcon not running?
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