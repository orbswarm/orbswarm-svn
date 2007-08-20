package com.orbswarm.swarmcomposer.composer;

import com.orbswarm.swarmcon.OrbControlImpl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;


/**
 * @author Simran Gleason
 */
public class Set {
    protected String name;
    protected String path;
    protected ArrayList sounds;
    // ?? protected Metadata md;
    protected Strategy strategy = null;
    protected Layer layer;
    protected boolean hasAbsolutePath = false;

    public Set(String name, Layer layer) {
        this.layer = layer;
        this.name = name;
        this.sounds = new ArrayList();
    }

    public String getName() {
        return this.name;
    }

    public void setName(String val) {
        this.name = val;
    }

    public void setPath(String val) {
        this.path = val;
        if (this.path.startsWith("/")) {
            hasAbsolutePath = true;
        }
    }

    public String getPath() {
        if (path == null) {
            return name;
        }
        return path;
    }

    public String getBasePath() {
        return layer.getBasePath();
    }
    
    public String getFullPath() {
        if (hasAbsolutePath) {
            return path;
        }
        return layer.getFullPath() + File.separatorChar + getPath();
    }
    
    public Strategy getStrategy() {
        if (strategy != null) {
            return strategy;
        }
        return layer.getStrategy();
    }

    public void setStrategy(Strategy strategy) {
        this.strategy = strategy;
    }

    public void setStrategy(String strategyName) {
        this.strategy = StrategyFactory.createStrategy(strategyName);
    }

    public ArrayList getSounds() {
        return sounds;
    }

    public Sound getSound(int nth) {
        return (Sound)sounds.get(nth);
    }

    public void addSound(Sound sound) {
        sounds.add(sound);
    }

    public Sound findSound(String name) {
        for(Iterator it = sounds.iterator(); it.hasNext(); ) {
            Sound sound = (Sound)it.next();
            if (sound.getName().equalsIgnoreCase(name)) {
                return sound;
            }
        }
        return null;
    }

    /**
     * Read sound files from the set's directory.
     */
    public void readSounds() throws IOException {
        if (getBasePath() == null) {
            return;
        }
        String setpath = getFullPath();
        System.out.println("Set " + getName() + ": READING sounds from " + setpath);
        File setDir = new File(setpath);
        if (!setDir.isDirectory()) {
            System.out.println("Set " + getName() + ": ERROR. set path is not a directory. " + setpath);
            throw new IOException("Set " + getName() + ": ERROR. set path is not a directory. " + setpath);
        }
        File[] soundFiles = setDir.listFiles();
        for(int i=0; i < soundFiles.length; i++) {
            File soundFile = soundFiles[i];
            if (soundFileIsValid(soundFile)) {
                Sound sound = OrbControlImpl.staticLookupSound(soundFile.getAbsolutePath());
                if (sound == null) {
                    sound = new Sound(this, soundFile.getName());
                }
                //System.out.println("   SET: looked up SOUND(" + soundFile.getAbsolutePath() +  "): " + sound);
                addSound(sound);
            }
        }
    }

    public boolean soundFileIsValid(File soundFile) {
        String soundFileName = soundFile.getName();
        if (soundFileName.startsWith(".")) {
            return false;
        }
        if (soundFileName.endsWith(".aif") ||
            soundFileName.endsWith(".aiff") ||
            soundFileName.endsWith(".wav") ||
            soundFileName.endsWith(".mp3")) {
            return true;
        }
        return false;
    }
    
    public String toString() {
        StringBuffer buf = new StringBuffer();
        toString(buf);
        return(buf.toString());
    }

    public void toString(StringBuffer buf) {
        buf.append("Set(" + name + ":" + sounds.size() + ") : {");
        /*
	for(Iterator it = sounds.iterator(); it.hasNext(); ) {
	    Sound sound = (Sound)it.next();
	    sound.toString(buf);
	    if (it.hasNext()) {
		buf.append(", ");
	    }
	}
         */
        buf.append("}");
    }

    public void calculateHashes(String songHash) {
        for(Iterator it = sounds.iterator(); it.hasNext(); ) {
            Sound sound = (Sound)it.next();
            sound.calculateHash(songHash);
        }
    }

    public void findSoundDurations() {
        for(Iterator it = sounds.iterator(); it.hasNext(); ) {
            Sound sound = (Sound)it.next();
            sound.findSoundDuration();
        }
    }

    public void write(StringBuffer buf, String indent) {
        String indent0 = indent;
        buf.append(indent0);
        buf.append(Bot.SET);
        buf.append(' ');
        buf.append(getName());
        buf.append('\n');
        indent += "    ";
        if (hasAbsolutePath) {
            Bot.writeAttribute(buf, indent, Bot.PATH, getPath());
            buf.append(' ');
        }
        if (strategy != null) {
            Bot.writeAttribute(buf, indent, Bot.STRATEGY, strategy.getName());
            buf.append(' ');
        }
        for(Iterator it = sounds.iterator(); it.hasNext(); ) {
            Sound sound = (Sound)it.next();
            sound.write(buf, indent);
        }
        buf.append(indent0);
        buf.append(Bot.END_SET);
        buf.append('\n');
    }

}

