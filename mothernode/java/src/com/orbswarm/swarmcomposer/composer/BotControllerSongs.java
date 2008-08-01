package com.orbswarm.swarmcomposer.composer;

import com.orbswarm.swarmcon.IOrbControl;
import com.orbswarm.swarmcomposer.sound.SimpleJavaPlayer;

import com.orbswarm.swarmcomposer.color.BotColorListener;
import com.orbswarm.swarmcomposer.color.ColorScheme;
import com.orbswarm.swarmcomposer.color.ColorSchemeListener;
import com.orbswarm.swarmcomposer.color.HSV;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.*;

import com.orbswarm.swarmcomposer.sound.SimpleJavaPlayer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.*;

/**
 * @author Simran Gleason
 */
public class BotControllerSongs extends BotController implements NeighborListener, SwarmListener
{
    private ArrayList orbsongs;
    private ArrayList playList; // songs
    private int songTime = 0;
    private IOrbControl orbControl;

    public BotControllerSongs(int numbots, String basePath, IOrbControl orbControl)
    {
      super(numbots, basePath, orbControl);
      orbsongs = new ArrayList(); // songs
      playList = new ArrayList(); // songs
      for (int i=0; i < numbots; i++)
      {
        Bot bot = (Bot)bots.get(i);
        //bot.setDefaultPlayer(new SimpleJavaPlayer(i));  // songs
      }
    }

    public void setSongTime(int val)
    {
      this.songTime = val;
    }

    public int getSongTime()
    {
      return this.songTime;
    }

    public void readSongFile(String songFile) throws IOException
    {
      List songs = Bot.readSongFile(songFile, basePath);
      for (Iterator it = songs.iterator(); it.hasNext(); )
      {
        Song song = (Song)it.next();
        playList.add(song);
        for (Iterator botit = bots.iterator(); botit.hasNext(); )
        {
          Bot bot = (Bot)botit.next();
          // Note: going to need to clone the song here.
          bot.addSong(song);
        }
      }
    }

    public Song chooseRandomSong()
    {
      int n = playList.size();
      return (Song)playList.get((int)(n * Math.random()));
    }

    public void randomizePlaylist()
    {
    }


    public void resetSong(Song song)
    {
      // not sure about when they should turn off gossip & when they should
      // turn it on, to get the startup sequence right...
      for (Iterator it = bots.iterator(); it.hasNext();)
      {
        Bot bot = (Bot)it.next();
        //bot.refrainFromGossip(true); //TODO
      }
      for (Iterator it = bots.iterator(); it.hasNext();)
      {
        Bot bot = (Bot)it.next();
        //bot.refrainFromGossip(false);
        bot.loadSong(song);  // TODO (also, this doesn't really start it)
      }
    }

    //
    // is this an inline loop, or its own thread?
    //
    public void startControllerThread()
    {
      super.startControllerThread();
      for (Iterator sit=playList.iterator(); sit.hasNext(); )
      {
        Song song = (Song)sit.next();
        if (song instanceof MultiChannelComposition)
        {
          playMultiChannelComposition((MultiChannelComposition)song);
        }
        else
        {
          System.out.println("Starting Song: " + song.getName());
          for (Iterator it = bots.iterator(); it.hasNext(); )
          {
            Bot bot = (Bot)it.next();
            bot.setSongTime(songTime);
            bot.loadSong(song);
          }

          startBots();
          for (Iterator it = bots.iterator(); it.hasNext(); )
          {
            Bot bot = (Bot)it.next();
            Thread pt = bot.getPlayerThread();
            if (pt != null)
            {
              try
              {
                System.out.println("BC::Waiting for bot thread(" + bot.getName() + ")");
                // later: take into account waiting for not longer than song time.
                pt.join();
                System.out.println("BC::FINISHED bot thread(" + bot.getName() + ")");
              }
              catch (InterruptedException ex)
              {
                System.out.println("BotControllerSongs got interrupted exception waiting for bot threads. ");
              }
            }
          }
        }
        System.out.println("=================");
        System.out.println("= song ended    =");
        System.out.println("=================");
        try
        {
          Thread.currentThread().sleep(5000);
        }
        catch (Exception ex)
        {
        }
      }
    }

    public void stopControllerThread()
    {
      System.out.println("BotControllerSongs.stopControllerThread()");
      super.stopControllerThread();
      stopBots();
    }


    public void playMultiChannelComposition(MultiChannelComposition song)
    {
      ArrayList threads = new ArrayList();
      for (int i=0; i < song.numTracks(); i++)
      {
        String trackPath = song.getTrackPath(i);
        SoundFilePlayer player = new SimpleJavaPlayer(i);
        Thread playThread = new TrackPlayThread(player, trackPath);
        threads.add(playThread);
        playThread.start();
      }
      System.out.println("Now wait for them all...");
      for (Iterator it = threads.iterator(); it.hasNext(); )
      {
        Thread t = (Thread)it.next();
        try
        {
          t.join();
        }
        catch (Exception ex)
        {
        }
      }
    }

    class TrackPlayThread extends Thread
    {
        private SoundFilePlayer player;
        private String trackPath;
        public TrackPlayThread(SoundFilePlayer player, String trackPath)
        {
          this.player = player;
          this.trackPath = trackPath;
        }
        public void run()
        {
          player.playFile(trackPath);
        }
    }

    public void playSongs()
    {
      playSongs(this.orbsongs);
    }

    public void playSongs(List orbsongs)
    {
      for (Iterator it=orbsongs.iterator(); it.hasNext(); )
      {
        String songFile = (String)it.next();
        playSong(songFile, songTime);
      }
    }

    public void playSong(String songFile, int songTime)
    {
      songFile = songFile + ".orbc";
      System.out.println("BotController: reading song file: " + songFile);
      setSongTime(songTime);
      try
      {
        this.readSongFile(songFile);
      }
      catch (Exception ex)
      {
        System.out.println("BotController.main() caught exception. " + ex);
        ex.printStackTrace();
      }
      Bot bot0 = (Bot) this.bots.get(0);
      System.out.println(bot0.toString());
      System.out.println("----------------");

      this.startControlling();
    }

    public static void main(String[] args)
    {
      int numbots = 6;
      if (args.length > 0)
      {
        BotControllerSongs botctl = new BotControllerSongs(numbots, "/orbsounds/songs", null);
        botctl.handleArgs(args);
        botctl.playSongs();
      }
    }

    public void handleArgs(String[] args)
    {
      int songTime = 0;
      int i=0;
      while (i < args.length)
      {
        if (args[i].equalsIgnoreCase("--songtime"))
        {
          i++;
          songTime = Integer.parseInt(args[i]);
          i++;
        }
        else
        {
          orbsongs.add(args[i]);
          System.out.println("BC:: song: " + args[i]);
          i++;
        }
      }
      this.setSongTime(songTime);
    }


}
