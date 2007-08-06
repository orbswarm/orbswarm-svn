package com.orbswarm.swarmcomposer.sound;

import com.orbswarm.swarmcomposer.composer.Sound;
import com.orbswarm.swarmcomposer.composer.SoundFilePlayer;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;


/**
 * @author Simran Gleason
 */
public class SimpleJavaPlayer extends SoundFilePlayer {
    private int channel;
    private boolean paused;
    private Sound currentSound = null;

    private boolean playing = false;
    
    public SimpleJavaPlayer(int channel) {
        this.channel = channel;
    }

    public boolean play(Sound sound) {
        currentSound = sound;
        String indent = "  ";
        for(int i=0; i < channel; i++) {
            System.out.print(indent);
        } 
        String soundPath = sound.getFullPath();
        System.out.println(channel + ":: " + sound.getName() + "[PLAY]  " + soundPath);
        startPlaying(soundPath, channel);
        return true;
    }

    public boolean playFile(String soundPath) {
        String indent = "  ";
        for(int i=0; i < channel; i++) {
            System.out.print(indent);
        } 
        System.out.println(channel + ":: [PLAY FILE]  " + soundPath);
        startPlaying(soundPath, channel);
        return true;
    }

    
    
    public void stop() {
        currentSound = null;
        playing = false;
    }

    public void pause(boolean paused) {
        this.paused = paused;
    }

    //    public void addPlayerListener(PlayerListener ear);
    public boolean isPaused() {
        return paused;
    }
    
    public boolean isPlaying() {
        return currentSound != null;
    }

    public  static final int EXTERNAL_BUFFER_SIZE = 128000;

    public void startPlaying(String soundFilePath, int channel) {
        AudioInputStream    audioInputStream = null;
        try {
            File soundFile = new File(soundFilePath);
            audioInputStream = AudioSystem.getAudioInputStream(soundFile);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        AudioFormat audioFormat = audioInputStream.getFormat();

        SourceDataLine line = null;
        DataLine.Info info = new DataLine.Info(SourceDataLine.class,
                                               audioFormat);
        try {
            line = (SourceDataLine) AudioSystem.getLine(info);
            line.open(audioFormat);
        } catch (LineUnavailableException e) {
            e.printStackTrace();
            return;
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        line.start();

        int nBytesRead = 0;
        byte[]  abData = new byte[EXTERNAL_BUFFER_SIZE];
        playing = true;

        while (nBytesRead != -1 && playing) {
            try {
                nBytesRead = audioInputStream.read(abData, 0, abData.length);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (nBytesRead >= 0) {
                int nBytesWritten = line.write(abData, 0, nBytesRead);
            }
        }
        line.drain();
        line.close();
    }
}
