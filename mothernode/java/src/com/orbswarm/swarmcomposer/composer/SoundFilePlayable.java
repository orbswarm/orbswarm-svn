package com.orbswarm.swarmcomposer.composer;

import java.io.File;


/**
 * @author Simran Gleason
 */
public class SoundFilePlayable implements Playable {
    private Song song;
    private Layer layer;
    private Set set;
    private Sound sound;

    private static Bot bot;
    
    public SoundFilePlayable(Song song, Layer layer, Set set, Sound sound) {
        this.song = song;
        this.layer = layer;
        this.set = set;
        this.sound = sound;
    }

    public static void setBot(Bot val) {
        bot = val;
    }

    public String getPath(String basePath) {
        if (sound.hasAbsolutePath()) {
            return sound.getPath();
        }
        return basePath + File.separatorChar + getRelPath();
    }

    public String getPath() {
        return getPath(bot.getBasePath());
    }
    
    public String getRelPath() {
        String path;
        if (sound.hasAbsolutePath()) {
            return sound.getPath();
        }
        // later: maybe some kind of path resolver that lets any level
        //        decide it wants an absolute path;
        //        Or maybe just the song & soundfile levels. 
        path = 
            song.getPath() + File.separatorChar +
            layer.getPath() + File.separatorChar +
            set.getPath() + File.separatorChar +
            sound.getPath();

        return path;
    }
	
    //public String getType(); //  not sure what''s really needed here. 
}
