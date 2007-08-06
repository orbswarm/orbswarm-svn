package com.orbswarm.swarmcomposer.composer;

/**
 *
 * Represents neighboring bots.
 *
 * @author Simran Gleason
 */
public class Neighbor {
    public static final int UNSPECIFIED = -1;
    public static final int TOO_CLOSE = 0;
    public static final int CLOSE     = 1;
    public static final int MEDIUM    = 2;
    public static final int FAR       = 3;
    public static final int TOO_FAR   = 4;
    
    public int    num;
    public String name;
    public String song;
    public int    songPlayTime;
    public String layer;
    public String set;
    public String sound;
    
    // what about distance: nope, handled in separate struct. 
    public Neighbor(int num, String name,
                    String song, int songPlayTime,
                    String layer, String set, String sound) {
        this.num = num;
        this.name = name;
        this.song = song;
        this.songPlayTime = songPlayTime;
        this.layer = layer;
        this.set = set;
        this.sound = sound;
    }

    public int getNum() {
        return num;
    }
    public void setNum(int num) {
        this.num = num;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getLayer() {
        return layer;
    }
    public void setLayer(String layer) {
        this.layer = layer;
    }

    public String getSet() {
        return set;
    }
    public void setSet(String set) {
        this.set = set;
    }
    public String getSong() {
        return song;
    }
    public void setSong(String song) {
        this.song = song;
    }
    public int getSongPlayTime() {
        return songPlayTime;
    }
    public void setSongPlayTime(int songPlayTime) {
        this.songPlayTime = songPlayTime;
    }

    public String getSound() {
        return sound;
    }
    public void setSound(String sound) {
        this.sound = sound;
    }
}
