package com.orbswarm.swarmcomposer.composer;

import java.io.File;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;


/**
 * @author Simran Gleason
 */
public class Sound {
    public static final int UNKNOWN = -1;
    public static final int MP3 = 0;
    public static final int AIF = 1;
    public static final int WAV = 2;
    public static final int AU  = 3;

    // marker sound indicating end of song. 
    public static final Sound END_SONG = new Sound();
    public static final Sound SET_CHANGE = new Sound();
    public static final Sound LAYER_CHANGE = new Sound();
    
    protected String  filename;
    protected String  fullPath;
    protected boolean hasAbsolutePath = false;
    protected int     type;
    protected String  extension = "";
    protected Set     set;
    protected String  pcmHash = null;
    protected String  mp3Hash = null;
    protected float   duration = 0.f; // seconds

    protected Sound() {
    }
    
    public Sound(Set set, String filename) {
        this.set = set;
        setFileName(filename);
    }

    public Sound(String filePath, float duration, String pcmHash, String mp3Hash) {
        setFileName(filePath);
        setDuration(duration);
        setPCMHash(pcmHash);
        setMP3Hash(mp3Hash);
    }
        
    public void setFileName(String filename) {
        this.filename = filename;
        if (this.filename.startsWith("/")) {
            hasAbsolutePath = true;
            fullPath = filename;
            this.filename = new File(filename).getName();
        } else {
            if (set != null) {
                fullPath = set.getFullPath() + File.separatorChar + filename;
            } else {
                fullPath = filename;
            }
        }
        extension = getExtension(filename);
        //System.out.println("Sound("+(set == null?"":set.getName())+", " + filename + "). ext: " + extension);
        type = UNKNOWN;
        if (extension.equalsIgnoreCase("mp3")) {
            type = MP3;
        } else if (extension.equalsIgnoreCase("aif")) {
            type = AIF;
        } else if (extension.equalsIgnoreCase("aiff")) {
            type =  AIF;
        } else if (extension.equalsIgnoreCase("au")) {
            type = AU;
        } else if (extension.equalsIgnoreCase("wav")) {
            type = WAV;
        }
        //System.out.println("New Sound: " + filename + " ext: " + extension + " type: " + type);
    }
    
    public boolean hasAbsolutePath() {
        return hasAbsolutePath;
    }

    public String getPath() {
        return getFullPath();
    }
    
    public String getFullPath() {
        if (fullPath == null) {
            if (hasAbsolutePath) {
                fullPath = filename;
            } else {
                fullPath = set.getFullPath() + File.separatorChar + filename;
            }
        }
        return  fullPath;
    }

    public String getName() {
        return filename;
    }

    public int getType() {
        return type;
    }

    public float getDuration() {
        return this.duration;
    }

    public void setDuration(float val) {
        this.duration = val;
    }

    public String getPCMHash() {
        return this.pcmHash;
    }

    public void setPCMHash(String val) {
        this.pcmHash = val;
    }

    public String getMP3Hash() {
        return this.mp3Hash;
    }

    public void setMP3Hash(String val) {
        this.mp3Hash = val;
    }
    

    public static String getExtension(File file) {
        return getExtension(file.getName());
    }
    
    public static String getExtension(String filename) {
        if (filename == null) {
            return null;
        }
        
        int lastdot = filename.lastIndexOf('.');
        if (lastdot == -1) {
            return null;
        }

        return filename.substring(lastdot + 1); // ? + 1?
    }

    public static String typeString(int type) {
        switch (type) {
        case UNKNOWN:
            return "unknown";
        case MP3:
            return "mp3";
        case AIF:
            return "aif";
        case AU:
            return "au";
        case WAV:
            return "wav";
        }
        return "NOT_KNOWN";
    }
    
    public String toString() {
        return "Sound(" + filename + " : " + typeString(getType()) + ")";
    }

    public void calculateHash(String songHash) {
        calculateHash(songHash, 0);
    }
    // offset allows us to try to hash again if there is a conflict. 
    public void calculateHash(String songHash, int offset) {
        String fullPath = getFullPath();
        int pathHashCode = (int)(Math.abs(fullPath.hashCode())) - offset;
        String pathHashCodeStr = "" + pathHashCode;
        // the MP3 file needs to be 8 chars + ".mp3"
        // so we take the last 8 chars of the pathHashCode, and append extension
        int hlen = pathHashCodeStr.length();
        if (hlen > 8) {
            pathHashCodeStr = pathHashCodeStr.substring(hlen - 8);
        }            
        String soundHash =  pathHashCodeStr + "." + (extension == null ? "" : extension);
        this.mp3Hash = pathHashCodeStr + ".mp3";
        // TODO: if the sound's extension is "mp3" there shouldn't be a pcm hash.
        if (type == MP3) {
            this.pcmHash = null;
        } else {
            this.pcmHash = soundHash;
        }
        // The offset allows us to
        // check to see if there is a conflict with an existing sound file, and if
        // so, subtract 1 from the hash until there isn't anymore.
    }

    public float findSoundDuration() {
        //System.out.println("FindSoundDuration(" + getName() + "). duration: " + duration);
        if (duration == 0.f) {
            // note: don't know whether to use the path or the hashed file.
            //       for now, we're only using this method from the SongCompiler, so
            //       we're trying to create the hash & find the length from the file at the
            //       path.
            File soundFile = new File(getFullPath());
            try {
                AudioFileFormat audioFileFormat = AudioSystem.getAudioFileFormat(soundFile);
                AudioFormat format = audioFileFormat.getFormat();
                int frames = audioFileFormat.getFrameLength();
                float frameRate = format.getFrameRate();
                duration = (frames / frameRate);
                //System.out.println("Sound(" + getName() + ") frames: " + frames + " rate: " + frameRate + " 1/rate: " + 1.0f/frameRate + " duration: " + duration);
            } catch (Exception ex) {
                System.out.println("Caught exception getting duration of sound file " + soundFile);
                ex.printStackTrace();
            }
        }
        return duration;
    }

    public void write(StringBuffer buf, String indent) {
        write(buf, indent, Bot.SOUND, Bot.END_SOUND);
    }

    protected void write(StringBuffer buf, String indent, String startTag, String endTag) {
        String indent0 = indent;
        buf.append(indent0);
        buf.append(startTag);
        buf.append(' ');
        buf.append(filename);
        buf.append("    ");
        //indent += "    ";
        indent = "";
        if (hasAbsolutePath) {
            Bot.writeAttribute_nlf(buf, indent, Bot.PATH, getPath());
            buf.append(' ');
        }
        if (duration != 0) {
            Bot.writeAttribute_nlf(buf, indent, Bot.DURATION, duration);
            buf.append(' ');
        }
        if (pcmHash != null) {
            Bot.writeAttribute_nlf(buf, indent, Bot.HASH, pcmHash);
            buf.append(' ');
        }
        if (mp3Hash != null) {
            Bot.writeAttribute_nlf(buf, indent, Bot.MP3_HASH, mp3Hash);
            buf.append(' ');
        }
        buf.append(endTag);
        buf.append('\n');
    }
            
}