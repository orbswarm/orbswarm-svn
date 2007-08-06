package com.orbswarm.swarmcomposer.composer;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;


/**
 * @author Simran Gleason
 */
public class MultiChannelComposition extends Song {
    protected ArrayList tracks; // List of Sound

    public MultiChannelComposition(String name, String basePath) {
        super(name, basePath);
        tracks = new ArrayList();
    }

    public int numTracks() {
        return tracks.size();
    }
    
    public ArrayList getTracks() {
        return tracks;
    }

    public String getTrackName(int i) {
        Track track = (Track) tracks.get(i);
        return track.getName();
    }

    public String getTrackPath(int i) {
        Track track = (Track) tracks.get(i);
        return track.getFullPath();
    }

    public Track getTrack(int i) {
        Track track = (Track) tracks.get(i);
        return track;
    }

    public void addTrack(String trackName) {
        addTrack(new Track(this, trackName));
    }

    public void addTrack(Track track) {
        tracks.add(track);
    }

    public void calculateHashes() {
        String songHash = getName().hashCode() + "";
        System.out.println("Song(" + getName() + ") HASH:["+ songHash + "]");

        for(Iterator it = tracks.iterator(); it.hasNext(); ) {
            Track track = (Track)it.next();
            track.calculateHash(songHash);
        }
    }

    public void findSoundDurations() {
        for(Iterator it = tracks.iterator(); it.hasNext(); ) {
            Track track = (Track)it.next();
            track.findSoundDuration();
        }
    }

    public void write(StringBuffer buf, String indent) {
        String indent0 = indent;
        buf.append(indent0);
        buf.append(Bot.SONG);
        buf.append(' ');
        buf.append(Bot.MULTI_CHANNEL_COMPOSITION);
        buf.append(' ');
        buf.append(getName());
        buf.append('\n');
        indent += "    ";
        if (hasAbsolutePath) {
            Bot.writeAttribute(buf, indent, Bot.PATH, getPath());
            buf.append(' ');
        }
        Bot.writeAttribute(buf, indent, Bot.COMPILED, "" + isCompiled());
        buf.append(' ');
        for(Iterator it= tracks.iterator(); it.hasNext() ; ) {
            writeTrack(buf, indent, (Track)it.next());
        }
        buf.append(indent0);
        buf.append(Bot.END_SONG);
        buf.append('\n');
    }

    public void writeTrack(StringBuffer buf, String indent, Sound track) {
        track.write(buf, indent);
    }
        

    
}

