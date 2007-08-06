package com.orbswarm.swarmcomposer.composer;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author Simran Gleason
 */
public class Layer extends BeatLockable {
    protected String name;
    protected String path;
    protected ArrayList sets;
    // ?? protected Metadata md;
    protected Strategy strategy = null;
    protected Song song;
    protected boolean hasAbsolutePath = false;
    
    public Layer(String name, Song song) {
        this.song = song;
        this.name = name;
        this.sets = new ArrayList();
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
        return song.getBasePath();
    }

    public String getFullPath() {
        if (hasAbsolutePath) {
            return path;
        }
        return song.getFullPath() + File.separatorChar + getPath();
    }

    public int getBPM() {
        if (bpmSpecified) {
            return bpm;
        } else {
            return song.getBPM();
        }
    }

    public int getBeatLock() {
        if (beatLockSpecified) {
            return super.getBeatLock();
        } else {
            return song.getBeatLock();
        }
    }

    public long getMeasureLength() {
        if (beatLockSpecified) {
            return measureLength;
        } else {
            return song.getMeasureLength();
        }
    }

    public Strategy getStrategy() {
        if (strategy != null) {
            return strategy;
        }
        return song.getStrategy();
    }
    
    public void setStrategy(Strategy strategy) {
        this.strategy = strategy;
    }
    
    public void setStrategy(String strategyName) {
        this.strategy = StrategyFactory.createStrategy(strategyName);
    }

    public ArrayList getSets() {
        return sets;
    }

    public Set getSet(int nth) {
        return (Set)sets.get(nth);
    }

    public void addSet(Set set) {
        sets.add(set);
    }

    public Set findSet(String name) {
        for(Iterator it = sets.iterator(); it.hasNext(); ) {
            Set set = (Set)it.next();
            if (set.getName().equalsIgnoreCase(name)) {
                return set;
            }
        }
        return null;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        toString(buf);
        return(buf.toString());
    }
    
    public void toString(StringBuffer buf) {
        buf.append("Layer(" + name + ") : {");
        for(Iterator it = sets.iterator(); it.hasNext(); ) {
            Set set = (Set)it.next();
            set.toString(buf);
            if (it.hasNext()) {
                buf.append(", ");
            }
        }
        buf.append("}");
    }

    public void calculateHashes(String songHash) {
        for(Iterator it = sets.iterator(); it.hasNext(); ) {
            Set set = (Set)it.next();
            set.calculateHashes(songHash);
        }
    }

    public void findSoundDurations() {
        for(Iterator it = sets.iterator(); it.hasNext(); ) {
            Set set = (Set)it.next();
            set.findSoundDurations();
        }
    }

    public void write(StringBuffer buf, String indent) {
        String indent0 = indent;
        buf.append(indent0);
        buf.append(Bot.LAYER);
        buf.append(' ');
        buf.append(getName());
        buf.append('\n');
        indent += "    ";
        if (hasAbsolutePath) {
            Bot.writeAttribute(buf, indent, Bot.PATH, getPath());
            buf.append(' ');
        }
        if (bpmSpecified) {
            Bot.writeAttribute(buf, indent, Bot.BPM, getBPM());
            buf.append(' ');
        }
        if (beatLockSpecified) {
            Bot.writeAttribute(buf, indent, Bot.BEAT_LOCK, getBeatLock());
            buf.append(' ');
        }
        if (strategy != null) {
            Bot.writeAttribute(buf, indent, Bot.STRATEGY, strategy.getName());
            buf.append(' ');
        }
        for(Iterator it = sets.iterator(); it.hasNext(); ) {
            Set set = (Set)it.next();
            set.write(buf, indent);
        }
        buf.append(indent0);
        buf.append(Bot.END_LAYER);
        buf.append('\n');
    }
}

        