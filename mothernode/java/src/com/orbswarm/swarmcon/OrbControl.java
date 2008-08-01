package com.orbswarm.swarmcon;

import com.orbswarm.choreography.Point;
import com.orbswarm.swarmcon.IOrbControl;

import com.orbswarm.swarmcomposer.color.HSV;
import com.orbswarm.swarmcomposer.composer.Sound;
import com.orbswarm.swarmcomposer.composer.SoundFilePlayer;
import com.orbswarm.swarmcomposer.sound.SimpleJavaPlayer;
import com.orbswarm.swarmcomposer.util.TokenReader;

import org.trebor.util.JarTools;

import java.awt.Color;
import java.util.HashMap;

public class OrbControl implements IOrbControl
{
    private SwarmCon swarmCon;
    private SoundFilePlayer[] soundFilePlayers;
    private static HashMap soundCatalog;
    private OrbIo orbIo;
    private HSV[] orbColors;
    private boolean[] orbEnabledMap;

    // TODO: hook these up to toggles somehow... (or keep in SwarmCon?)
    //
    private boolean sendCommandsToOrbs = true;
    private boolean simulateColors = true;
    private boolean simulateSounds = true;

    public OrbControl(SwarmCon swarmCon,
    boolean sendCommandsToOrbs,
    boolean simulateColors, boolean simulateSounds)
    {
      this.swarmCon = swarmCon;
      this.orbIo = swarmCon.getOrbIo();
      this.sendCommandsToOrbs = sendCommandsToOrbs;
      this.simulateColors = simulateColors;
      this.simulateSounds = simulateSounds;
      setupSoundCatalog();
      setupSoundPlayers(6); // TODO: generalize
      orbColors = new HSV[6];
      for (int i=0; i < 6; i++)
      {
        orbColors[i] = null;
      }
      setupOrbEnabledMap();
    }
    public void setupOrbEnabledMap()
    {
      orbEnabledMap = new boolean[6];
      for (int i=0; i < 6; i++)
      {
        orbEnabledMap[i] = true;
      }
      orbEnabledMap[2] = true;
      orbEnabledMap[3] = true;  // TODO: read this from a file!!!
    }
    public boolean isEnabled(int orbNum)
    {
      boolean enabled =  (orbNum < orbEnabledMap.length && orbEnabledMap[orbNum]);
      //System.out.println("isEnabled[" + orbNum + "] = " + enabled);
      return enabled;
    }

    private void setupSoundCatalog()
    {
      soundCatalog = new HashMap();
      // FIXME: this should be done after swarmcon reads its properties.
      String soundCatalogsProp = swarmCon.getProperty(
        "swarmcon.sound.soundCatalogs",
        SwarmCon.RESOURCES_PATH + "/songs/sounds.catalog");
      String[] soundCatalogs = soundCatalogsProp.trim().split(" ");
      for (int i=0; i < soundCatalogs.length; i++ )
      {
        if (soundCatalogs[i].length() > 0)
        {
          readSoundCatalog(soundCatalogs[i]);
        }
      }
      String errataCatalog = swarmCon.getProperty(
        "swarmcon.sound.errataCatalog",
        SwarmCon.RESOURCES_PATH + "/songs/errata.catalog");
      readSoundCatalog(errataCatalog);
    }

    public SwarmCon getSwarmCon()
    {
      return swarmCon;
    }

    public void setOrbIo(OrbIo orbIo)
    {
      this.orbIo = orbIo;
    }

    public void setupSoundPlayers(int n)
    {
      soundFilePlayers = new SoundFilePlayer[n];
      for (int i=0; i < n; i++)
      {
        soundFilePlayers[i] = new SimpleJavaPlayer(i);
      }
    }

    // TODO: read catalogFile as resrouce rather than file.
    private static void readSoundCatalog(String catalogFile)
    {
      try
      {
        TokenReader reader = new TokenReader(
          JarTools.getResourceAsStream(catalogFile));
        String path = reader.readToken();
        while (path != null)
        {
          float duration = reader.readFloat();
          String pcmHash = reader.readToken();
          if (pcmHash.equals("-"))
          {
            pcmHash = null;
          }
          String mp3Hash = reader.readToken();
          Sound sound = new Sound(path, duration, pcmHash, mp3Hash);
          soundCatalog.put(path, sound);
          path = reader.readToken();
        }
      }
      catch (Exception ex)
      {
        System.out.println(
          "OrbControl caught exception reading sound catalog: " + catalogFile);
        ex.printStackTrace();
      }
    }

    //
    // Implementation of methods from com.orbswarm.choreography.OrbControl
    //
    public IOrbControl getOrbControl()
    {
      return (IOrbControl)this;
    }

    // sound control methods not implemented.
    // return length of sound in MS
    public float playSoundFile(int orbNum, String soundFilePath)
    {
      Sound sound = lookupSound(soundFilePath);
      return playSound(orbNum, sound);
    }

    public float playSound(int orbNum, Sound sound)
    {
      float dur = sound.getDuration();
      if (simulateSounds)
      {
        System.out.println("ORI: playsound(" + orbNum + ", " + sound + ") dur:" + dur);
        SoundFilePlayer player = getSoundPlayer(orbNum);
        playOnThread(player, sound);
      }
      if (sendCommandsToOrbs && orbIo != null  && isEnabled(orbNum))
      {
        String mp3Hash = sound.getMP3Hash();
        StringBuffer buf = new StringBuffer();
        buf.append("<M1 VPF ");
        buf.append(mp3Hash);
        buf.append(">");
        String orbCmd = wrapOrbCommand(orbNum, buf.toString());
        // Note: not all the events are getting through, so
        // sending the sounds a few times to make sure
        // seems like a reasonable hack to me.
        orbIo.send(orbCmd);
        if (swarmCon.multipleSoundCommands)
        {
          orbIo.send(orbCmd);
          orbIo.send(orbCmd);
          orbIo.send(orbCmd);
        }
      }
      return dur;
    }


    private void playOnThread(SoundFilePlayer player, Sound sound)
    {
      final Sound _sound = sound;
      final SoundFilePlayer _player = player;

      new Thread()
      {
          public void run()
          {
            System.out.println("  Playing sound" + _sound + " on thread. " + this);
            _player.play(_sound);
          }
      }
        .start();
    }

    private SoundFilePlayer getSoundPlayer(int orbNum)
    {
      // TODO: decide if we're in production or simulation.
      return soundFilePlayers[orbNum];
    }

    public  Sound lookupSound(String soundFilePath)
    {
      return staticLookupSound(soundFilePath);
    }

    //
    // Right now we use a static catalog to find the sound files.
    // In the future, perhaps we can read a directory full of sound files
    // to figure out what's there.
    //
    // A plus for the catalog: the lengths and locations of the sound files are
    // known -- i.e. figured out offline.
    // A minus is that the catalog creation needs to be done before we cna do anything).
    // (see com.orbswarm.swarmcomposer.composer.SongCompiler)
    //
    public static Sound staticLookupSound(String soundFilePath)
    {
      Sound sound = (Sound)soundCatalog.get(soundFilePath);
      if (sound == null)
      {
        // return a sound that just gives back the soundFilePath.
        // This is an optimization that allows us to simply put a sound file
        // on the usb key and address it by name in the timeline.
        sound = new Sound(soundFilePath, 0.f, soundFilePath, soundFilePath);
      }
      return sound;
    }

    boolean sendStopFile = true;
    boolean sendStopCommand = false;
    public void setSendStopFile(boolean val)
    {
      sendStopFile = val;
    }
    public boolean getSendStopFile()
    {
      return sendStopFile;
    }

    public void setSendStopCommand(boolean val)
    {
      sendStopCommand = val;
    }
    public boolean getSendStopCommand()
    {
      return sendStopCommand;
    }

    // TODO: good defaults mechanism.
    private int defaultSoundVolume = 100;  // what is acceptable range of values?
    public int getDefaultSoundVolume()
    {
      return defaultSoundVolume;
    }

    public void stopSound(int orbNum)
    {
      if (simulateSounds)
      {
        SoundFilePlayer player = getSoundPlayer(orbNum);
        //System.out.println("OCI: stopSound(orb:" + orbNum + ") player: " + player);
        if (player != null)
        {
          player.stop();
        }
      }
      if (sendCommandsToOrbs && orbIo != null  && isEnabled(orbNum))
      {
        if (sendStopCommand)
        {
          String stopCommand = "<M1 VST>";
          String orbCmd = wrapOrbCommand(orbNum, stopCommand);
          orbIo.send(orbCmd);
        }
        //
        // Because the stop commands aren't working too well,
        //  we can optionally play a
        // 10ms long blank sound to make sure it stops.
        //
        if (sendStopFile)
        {
          String stopSoundCmd = wrapOrbCommand(orbNum, "<M1 VPF Stop.mp3>");
          orbIo.send(stopSoundCmd);
        }
      }

    }

    // TODO: some kind of master volume facility.
    public void volume(int orbNum, int volume)
    {
      // volume goes from 1 to 100.
      // need to write register 0xB, values range from 0-FF
      // left byte, right byte.
      //  (attenuation value: FF = no volume)
      //  e.g. <M1 VWR B FF00> turns off left channel; full volume on right.
      if (sendCommandsToOrbs && orbIo != null  && isEnabled(orbNum))
      {
        StringBuffer buf = new StringBuffer();
        buf.append("<M1 VWR B ");
        float atten = (100.f - volume) / 100.f;
        int atten256 = (int)(256.f * atten);
        byte attenByte = (byte)atten256; // (byte)(0x00FF & atten256);
        buf.append(attenByte);
        buf.append(attenByte);
        buf.append(">");
        System.out.println("Vol: " + volume + " atten: " + atten  + " attenByte: " + attenByte + " buf: " + buf);
        String orbCmd = wrapOrbCommand(orbNum, buf.toString());
        orbIo.send(orbCmd);
      }
    }

    public HSV getOrbColor(int orbNum)
    {
      return orbColors[orbNum];
    }

    // only one Light control method implemented
    public void orbColor(int orbNum, HSV hsvColor, int timeMS)
    {
      if (simulateColors)
      {
        final Orb orb = (Orb)swarmCon.swarm.getOrb(orbNum);
        Color prevOrbColor = orb.getOrbColor();
        final HSV prevHSV = HSV.fromColor(prevOrbColor);
        if (timeMS <= 0)
        {
          Color color = hsvColor.toColor();
          orbColors[orbNum] = hsvColor;
          orb.setOrbColor(color);
        }
        else
        {
          final int _timeMS = timeMS;
          final HSV _hsvColor = hsvColor;
          final int _orbNum = orbNum;
          new Thread()
          {
              public void run()
              {
                boolean sendFadesToOrbs = swarmCon.steppedColorFades; // need this until Jon implements on-board fades.
                fadeColor(_orbNum, orb, prevHSV, _hsvColor, _timeMS, 300, sendFadesToOrbs);
              }
          }
            .start();
        }
      }
      else
      {
        orbColors[orbNum] = hsvColor;
      }

      // if we're already sending the stepped fade commands, we don't want to send the fully timed one.
      if (!swarmCon.steppedColorFades &&
      sendCommandsToOrbs && orbIo != null && isEnabled(orbNum))
      {
        // TODO: send color command out on OrbIO, or give it to model, or something.
        // TODO: one board or two (later -- we get two light commands per orb)
        // fade:  <LR64><LG200><LB220><LT2200> to set {r, g, b, time} on all boards
        //        <LF> to do the fade  <L0F> to fade the first, <L1F> the second board
        int timeTics = timeMS * 180 / 1000;
        String boardAddress = "";  // later: possibly independent board controls
        sendLightCommand(orbNum, boardAddress, hsvColor, timeTics);
      }
      else
      {
        //System.out.println("sendCommandsToOrbs: " + sendCommandsToOrbs + " orbIo: " + orbIo);
      }
    }

    public void sendLightCommand(int orbNum, String boardAddress, HSV hsvColor, int timeTics)
    {
      sendLightingCommand(orbNum, boardAddress,  "R" + hsvColor.getRed());
      sendLightingCommand(orbNum, boardAddress,  "G" + hsvColor.getGreen());
      sendLightingCommand(orbNum, boardAddress,  "B" + hsvColor.getBlue());
      sendLightingCommand(orbNum, boardAddress,  "T" + timeTics);
      if (orbIo != null)
      {
        orbIo.send(wrapOrbCommand(orbNum, "<L" + boardAddress + "F>"));
      }
    }

    // lighting commands need to be sent individually
    public void sendLightingCommand(int orbNum, String boardAddress, String cmd)
    {
      if (orbIo != null)
      {
        orbIo.send(wrapOrbCommand(orbNum, "<L" + boardAddress + cmd + ">"));
      }
    }

    public String wrapOrbCommand(int orbNum, String message)
    {
      StringBuffer buf = new StringBuffer();
      // e.g. {60 <LG200>}
      buf.append("{");
      buf.append(60 + orbNum); // this is an IP Addr?
      buf.append(" ");
      buf.append(message);
      buf.append("}");
      return buf.toString();
    }

    // simulate the color fading behaviour on an orb.
    public void fadeColor(int orbNum, Orb orb, HSV prev, HSV target, int timeMS, int slewMS, boolean sendFadesToOrbs)
    {
      //System.out.println(" fade color. o" + orbNum + " target: " + target.rgbString() + " timeMS: " + timeMS);
      int steps = timeMS / slewMS;
      int timeTics = timeMS * 180 / 1000;
      if (steps == 0)
      {
        steps = 1;
      }
      float hue      = prev.getHue();
      float sat      = prev.getSat();
      float val      = prev.getVal();
      float hueDelta = (target.getHue() - hue) / steps;
      float satDelta = (target.getSat() - sat) / steps;
      float valDelta = (target.getVal() - val) / steps;
      for (int i=0; i < steps; i++)
      {
        float h1 = hue + i * hueDelta;
        float s1 = sat + i * satDelta;
        float v1 = val + i * valDelta;
        HSV stepColorHSV = new HSV(h1, s1, v1);
        orbColors[orbNum] = stepColorHSV;
        //System.out.println("      o" + orbNum + "    FadeColor step: " + stepColorHSV.rgbString());

        Color stepColor = stepColorHSV.toColor();
        if (sendFadesToOrbs && orbIo != null && isEnabled(orbNum))
        {
          String boardAddress = ""; // TODO: refactor this.
          sendLightCommand(orbNum, boardAddress, stepColorHSV, 0);

        }
        orb.setOrbColor(stepColor);
        try
        {
          Thread.sleep(slewMS);
        }
        catch (InterruptedException ex)
        {
        }
      }
      // finally, send the actual color we're targetting
      // (in case the steps don't line up exactly onthe slew rate boundaries)
      if (sendFadesToOrbs && orbIo != null && isEnabled(orbNum))
      {
        String boardAddress = ""; // TODO: refactor this.
        sendLightCommand(orbNum, boardAddress, target, 0);
      }
      //System.out.println(" FADE color. o" + orbNum + " FINAL target: " + target.rgbString() );
      orbColors[orbNum] = target;
      orb.setOrbColor(target.toColor());
      swarmCon.repaint(); // todo: only if swarmcon not running?
    }


    public void orbColorFade(int orb,
    HSV color1, HSV color2,
    int time) {}

    //
    // Motion methods
    //
    public void followPath(com.orbswarm.choreography.Point[] wayPoints) {}
    public void stopOrb(int orbNum)
    {
      if (orbIo != null)
        orbIo.powerOrb(orbNum, 0);
    }

    //
    // SoundFile -> sound hash mapping.
    //
    public void   addSoundFileMapping(String soundFilePath, String soundFileHash) {}
    public String getSoundFileHash(String soundFilePath)
    {
      return null;
    }
    public java.util.List   getSoundFileMappingKeys()
    {
      return null;
    }
}
