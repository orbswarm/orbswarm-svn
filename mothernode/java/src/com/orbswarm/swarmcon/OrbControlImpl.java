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
    private HSV[] orbColors;
    private boolean[] orbEnabledMap;

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
        orbColors = new HSV[6];
        for(int i=0; i < 6; i++) {
            orbColors[i] = null;
        }
        setupOrbEnabledMap();
    }
    public void setupOrbEnabledMap() {
        orbEnabledMap = new boolean[6];
        for(int i=0; i < 6; i++) {
            orbEnabledMap[i] = true;
        }
        orbEnabledMap[2] = false;
        orbEnabledMap[3] = false;  // TODO: read this from a file!!!
    }
    public boolean isEnabled(int orbNum) {
        boolean enabled =  (orbNum < orbEnabledMap.length && orbEnabledMap[orbNum]);
        //System.out.println("isEnabled[" + orbNum + "] = " + enabled);
        return enabled;
    }

    static {
        soundCatalog = new HashMap();
        readSoundCatalog("resources/songs/sounds.catalog");
        readSoundCatalog("resources/songs/errata.catalog");
    }
    
    public SwarmCon getSwarmCon() {
        return swarmCon;
    }

    public void setOrbIo(OrbIo orbIo) {
        this.orbIo = orbIo;
    }
    
    public void setupSoundPlayers(int n) {
        soundFilePlayers = new SoundFilePlayer[n];
        for(int i=0; i < n; i++) {
            soundFilePlayers[i] = new SimpleJavaPlayer(i);
        }
    }

    private static void readSoundCatalog(String catalogFile) {
        try {
            TokenReader reader = new TokenReader(catalogFile);
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
            System.out.println("OrbControlImpl caught exception reading sound catalog: " + catalogFile);
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
        if (sendCommandsToOrbs && orbIo != null  && isEnabled(orbNum)) {
            String mp3Hash = sound.getMP3Hash();
            StringBuffer buf = new StringBuffer();
            buf.append("<M1 VPF ");
            buf.append(mp3Hash);
            buf.append(">");
            String orbCmd = wrapOrbCommand(orbNum, buf.toString());
            // Note: not all the events are getting through, so 
            // sending the sounds a few times to make sure
            // seems like a reasonable hack to me. 
            orbIo.send(orbCmd);
            orbIo.send(orbCmd);
            orbIo.send(orbCmd);
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

    boolean sendStopFile = true;
    boolean sendStopCommand = false;
    public void setSendStopFile(boolean val) {
        sendStopFile = val;
    }
    public boolean getSendStopFile() {
        return sendStopFile;
    }

    public void setSendStopCommand(boolean val) {
        sendStopCommand = val;
    }
    public boolean getSendStopCommand() {
        return sendStopCommand;
    }

    // TODO: good defaults mechanism.
    private int defaultSoundVolume = 100;  // what is acceptable range of values?
    public int getDefaultSoundVolume() {
        return defaultSoundVolume;
    }

    public void stopSound(int orbNum) {
        if (simulateSounds) {
            SoundFilePlayer player = getSoundPlayer(orbNum);
            //System.out.println("OCI: stopSound(orb:" + orbNum + ") player: " + player);
            if (player != null) {
                player.stop();
            }
        }
        if (sendCommandsToOrbs && orbIo != null  && isEnabled(orbNum)) {
            if (sendStopCommand) {
                String stopCommand = "<M1 VST>";
                String orbCmd = wrapOrbCommand(orbNum, stopCommand);
                orbIo.send(orbCmd);
            }
            //
            // Because the stop commands aren't working too well,
            //  we can optionally play a
            // 10ms long blank sound to make sure it stops.
            //
            if (sendStopFile) {
                String stopSoundCmd = wrapOrbCommand(orbNum, "<M1 VPF Stop.mp3>");
                orbIo.send(stopSoundCmd);
            }
        }
    
    }

    // TODO: some kind of master volume facility. 
    public void volume(int orbNum, int volume) {
        // volume goes from 1 to 100.
        // need to write register 0xB, values range from 0-FF
        // left byte, right byte. 
        //  (attenuation value: FF = no volume)
        //  e.g. <M1 VWR B FF00> turns off left channel; full volume on right.
        if (sendCommandsToOrbs && orbIo != null  && isEnabled(orbNum)) {
            StringBuffer buf = new StringBuffer();
            buf.append("<M1 VWR B ");
            float atten = (100.f - volume) / 100.f;
            int atten256 = (int)(256.f * atten);
            byte attenByte = (byte)atten256; // (byte)(0x00FF & atten256);
            buf.append(attenByte);
            buf.append(attenByte);
            buf.append(">");
            System.out.println("Vol: " + volume + " atten: " + atten  + " attenByte: " + attenByte + " buf: " + buf);
            String orbCmd = wrapOrbCommand(orbNum, buf.toString());
            orbIo.send(orbCmd);
        }
    }

    public HSV getOrbColor(int orbNum) {
        return orbColors[orbNum];
    }

    // only one Light control method implemented
    public void orbColor(int orbNum, HSV hsvColor, int timeMS) {
        //System.out.println("SwarmCon:OrbControlImpl orbColor(orb: " + orbNum + "HSV: " + hsvColor + " time:" + timeMS + ")");
        if (simulateColors) {
            final Orb orb = (Orb)swarmCon.swarm.getOrb(orbNum);
            Color prevOrbColor = orb.getOrbColor();
            final HSV prevHSV = HSV.fromColor(prevOrbColor);
            if (timeMS <= 0) {
                Color color = hsvColor.toColor();
                orbColors[orbNum] = hsvColor;
                orb.setOrbColor(color);
            } else {
                final int _timeMS = timeMS;
                final HSV _hsvColor = hsvColor;
                final int _orbNum = orbNum;
                new Thread() {
                    public void run()  {
                        boolean sendFadesToOrbs = true; // need this until Jon implements on-board fades. 
                        fadeColor(_orbNum, orb, prevHSV, _hsvColor, _timeMS, 300, sendFadesToOrbs);
                    }
                }.start();
            }
        } else {
            orbColors[orbNum] = hsvColor;
        }
        
        if (sendCommandsToOrbs && orbIo != null && isEnabled(orbNum)) {
            // TODO: send color command out on OrbIO, or give it to model, or something.
            // TODO: one board or two (later -- we get two light commands per orb)
            // fade:  <LH64><LS200><LV220><LT2200> to set {h, s, v, time} on all boards (OBSOLETE)
            // fade:  <LR64><LG200><LB220><LT2200> to set {r, g, b, time} on all boards
            //        <LF> to do the fade  <L0F> to fade the first, <L1F> the second board
            String boardAddress = " ";  // later: possibly independent board controls
            //StringBuffer buf = new StringBuffer();
            // question: do we send all the commands in one string, or one at a time?
            // answer: yes, we can send them all on one string
            /* there's been a change: now we send colors as RGB...
               buf.append("<L" + boardAddress + "H" + hue + ">");
               buf.append("<L" + boardAddress + "S" + sat + ">");
               buf.append("<L" + boardAddress + "V" + val + ">");
            */
            sendLightCommand(orbNum, boardAddress, hsvColor, timeMS);
        } else {
            //System.out.println("sendCommandsToOrbs: " + sendCommandsToOrbs + " orbIo: " + orbIo);
        }
    }

    public void sendLightCommand(int orbNum, String boardAddress, HSV hsvColor, int timeMS) {
        sendLightingCommand(orbNum, boardAddress,  "R" + hsvColor.getRed());
        sendLightingCommand(orbNum, boardAddress,  "G" + hsvColor.getGreen());
        sendLightingCommand(orbNum, boardAddress,  "B" + hsvColor.getBlue());
        sendLightingCommand(orbNum, boardAddress,  "T" + timeMS);
        if (orbIo != null) {
            orbIo.send(wrapOrbCommand(orbNum, "<L F>"));
        }
    }

    // lighting commands need to be sent individually
    public void sendLightingCommand(int orbNum, String boardAddress, String cmd) {
        if (orbIo != null) {
            orbIo.send(wrapOrbCommand(orbNum, "<L" + boardAddress + cmd + ">"));
        }
    }
        
    public String wrapOrbCommand(int orbNum, String message) {
        StringBuffer buf = new StringBuffer();
        // e.g. {60 <LG200>}
        buf.append("{");
        buf.append(60 + orbNum); // this is an IP Addr?
        buf.append(" ");
        buf.append(message);
        buf.append("}");
        return buf.toString();
    }

    // simulate the color fading behaviour on an orb. 
    public void fadeColor(int orbNum, Orb orb, HSV prev, HSV target, int timeMS, int slewMS, boolean sendFadesToOrbs) {
        //System.out.println(" fade color. target: " + target + " timeMS: " + timeMS);
        int steps = timeMS / slewMS;
        if (steps == 0) {
            steps = 1;
        }
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
            HSV stepColorHSV = new HSV(h1, s1, v1);
            orbColors[orbNum] = stepColorHSV;
                
            Color stepColor = stepColorHSV.toColor();
            if (sendFadesToOrbs && orbIo != null && isEnabled(orbNum)) {
                String boardAddress = " "; // TODO: refactor this.
                sendLightCommand(orbNum, boardAddress, stepColorHSV, 0);
                //System.out.println("        FadeColor step: " + stepColorHSV);

            }
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
                             HSV color1, HSV color2,
                             int time) {}

    //
    // Motion methods
    //
    public void followPath(com.orbswarm.choreography.Point[] wayPoints) {}
    public void stopOrb(int orbNum) {
        orbIo.powerOrb(orbNum, 0);
    }
    
    //
    // SoundFile -> sound hash mapping.
    //
    public void   addSoundFileMapping(String soundFilePath, String soundFileHash) {}
    public String getSoundFileHash(String soundFilePath) {return null;}
    public java.util.List   getSoundFileMappingKeys() {return null;}
}