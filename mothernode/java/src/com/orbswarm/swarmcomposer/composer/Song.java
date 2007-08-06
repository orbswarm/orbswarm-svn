package com.orbswarm.swarmcomposer.composer;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;


/**
 * @author Simran Gleason
 */
public class Song extends BeatLockable {
    protected String    name;
    protected String    path;
    protected ArrayList layers;
    protected Matrix    matrix;
    protected Strategy  strategy;
    protected boolean   hasAbsolutePath = false;
    protected Bot       bot;
    protected String    basePath;
    protected boolean   isCompiled = false;

    public Song(String name, String basePath) {
        this.name = name;
        this.basePath = basePath;
        this.layers = new ArrayList();
        this.matrix = new Matrix();
        this.strategy = StrategyFactory.createStrategy(Strategy.RANDOM);
    }

    public void setBot(Bot bot) {
        this.bot = bot;
        this.basePath = bot.getBasePath();
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
        return this.basePath;
    }

    public String getFullPath() {
        if (hasAbsolutePath) {
            return path;
        }
        return basePath + File.separatorChar + getPath();
    }
    
    public Strategy getStrategy() {
        return strategy;
    }

    public void setStrategy(Strategy strategy) {
        this.strategy = strategy;
    }

    public boolean isCompiled() {
        return this.isCompiled;
    }
    public void setCompiled(boolean val) {
        this.isCompiled = val;
    }
    
    public void setStrategy(String strategyName) {
        this.strategy = StrategyFactory.createStrategy(strategyName);
    }

    public Matrix getMatrix() {
        return matrix;
    }

    public ArrayList getLayers() {
        return layers;
    }

    public Layer getLayer(int nth) {
        return (Layer)layers.get(nth);
    }

    public void addLayer(Layer layer) {
        layers.add(layer);
    }

    public Layer findLayer(String name) {
        for(Iterator it = layers.iterator(); it.hasNext(); ) {
            Layer layer = (Layer)it.next();
            if (layer.getName().equalsIgnoreCase(name)) {
                return layer;
            }
        }
        return null;
    }

    public int compatibility(Set setA, String setNameB) {
        return compatibility(setA.getName(), setNameB);
    }
    
    public int compatibility(String setNameA, String setNameB) {
        Compatibility compat = matrix.getCell(setNameA, setNameB);
        //System.out.println("Song:COMPAT(" + setNameA + ", " + setNameB + ")=> " + compat);
        if (compat == null) {
            // no declared incompatibility means completely orthogonal, i.e. compatible.
            return 100;
        }
        return compat.getIndex();
    }
    
    public String toString() {
        StringBuffer buf = new StringBuffer();
        toString(buf);
        return(buf.toString());
    }

    public void toString(StringBuffer buf) {
        buf.append("Song(" + name + ") : {\n");
        matrix.toString(buf);
        buf.append("\n");
        if (getStrategy() != null) {
            buf.append("Strategy: " + getStrategy().getName() + "\n");
        }
        for(Iterator it = layers.iterator(); it.hasNext(); ) {
            Layer layer = (Layer)it.next();
            buf.append("  ");
            layer.toString(buf);
            if (it.hasNext()) {
                buf.append("\n");
            }
        }
    }

    public void write(StringBuffer buf) {
        write(buf, "");
    }

    public void calculateHashes() {
        String songHash = getName().hashCode() + "";
        System.out.println("Song(" + getName() + ") HASH:["+ songHash + "]");

        for(Iterator it = layers.iterator(); it.hasNext(); ) {
            Layer layer = (Layer)it.next();
            layer.calculateHashes(songHash);
        }
        
    }

    public void findSoundDurations() {
        for(Iterator it = layers.iterator(); it.hasNext(); ) {
            Layer layer = (Layer)it.next();
            layer.findSoundDurations();
        }
    }
            
    public void write(StringBuffer buf, String indent) {
        String indent0 = indent;
        buf.append(indent0);
        buf.append(Bot.SONG);
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
        Bot.writeAttribute(buf, indent, Bot.COMPILED, "" + isCompiled());
        buf.append(' ');

        getMatrix().write(buf, indent);

        for(Iterator it = layers.iterator(); it.hasNext(); ) {
            Layer layer = (Layer)it.next();
            layer.write(buf, indent);
        }
        buf.append(indent0);
        buf.append(Bot.END_SONG);
        buf.append('\n');
    }
}

