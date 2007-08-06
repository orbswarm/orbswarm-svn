package com.orbswarm.swarmcomposer.composer;

import java.io.File;

public class Track extends Sound {
    protected MultiChannelComposition mcSong = null;
    public Track(MultiChannelComposition mcSong, String filename) {
        this.mcSong = mcSong;
        super.setFileName(filename);
    }

    public String getFullPath() {
        if (hasAbsolutePath) {
            return filename;
        }
        return mcSong.getFullPath() + File.separatorChar + filename;
    }

    public void write(StringBuffer buf, String indent) {
        write(buf, indent, Bot.TRACK, Bot.END_TRACK);
    }



}