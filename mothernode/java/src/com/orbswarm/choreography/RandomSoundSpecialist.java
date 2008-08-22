package com.orbswarm.choreography;

import com.orbswarm.swarmcomposer.composer.Sound;

import com.orbswarm.swarmcon.IOrbControl;

import java.util.ArrayList;
import java.util.Properties;

/**
 * Specialist that plays a random choice out of its list of songs.
 */
public class RandomSoundSpecialist extends AbstractSpecialist
{
    private boolean enabled = true;
    private Sound sound = null;
    private ArrayList sounds = new ArrayList();
    private float duration = 0.f;

    public void setup(IOrbControl orbControl, Properties initialProperties, int[] orbs)
    {
      super.setup(orbControl, initialProperties, orbs);
    }

    public void setProperty(String name, String val)
    {
      super.setProperty(name, val);
      System.out.println("RANDOMSoundSpecialist: setProperty(" + name + ") = " + val);
      if (name.startsWith("soundfile"))
      {
        System.out.println("RandomSound: looking up soundfile: " + val);
        sound = orbControl.lookupSound(val);
        System.out.println("             ==> " + sound + " duration: " + (sound == null ? -1.f : sound.getDuration()));
        if (sound != null)
        {
          // little hack here: set the duration to a little longer than the sound's
          // actual duration so the event end doesn't cut off the sound as it is
          // fading out -- in case of a bit of a timing glitch.
          float soundDurationFudgeFactor = .05f; // 20ms
          float dur = sound.getDuration() + soundDurationFudgeFactor;
          if (dur > duration) {
              duration = dur;
              setDuration(duration);
          }
          sounds.add(sound);
        }
      }
    }

    public void start()
    {
        
      if (enabled)
      {
          System.out.println("RandomSOUND:start. sounds: " + sounds.size());
        if (sounds.size() > 0)
        {
          long durationMS = 0;
          for (int i=0; i < orbs.length; i++)
          {
            int orbNum = orbs[i];
            int nth = randomRange(0, sounds.size() - 1);
            sound = (Sound)sounds.get(nth);
            float durationSec = orbControl.playSound(orbNum, sound);
            durationMS = (int)(1000 * durationSec);
          }
          delayedBroadcastCommandCompleted(durationMS, "start", orbs, sound.getName());
        }
      }
    }

    public void stop()
    {
      if (sound != null)
      {
        for (int i=0; i < orbs.length; i++)
        {
          int orbNum = orbs[i];
          if (sound != null)
          {
            // Note: we may not actually want to stop the sounds here.
            //       (unless specifically asked for. i.e. we want a distinction
            //        between a sound event playing out its full duration and a sound event
            //        that is stopped either because the timeline finished or the
            //        event was given a duration smaller than the sound's duration. )

            orbControl.stopSound(orbNum);
          }
        }
      }
    }

    public void enable(boolean value)
    {
      enabled = value;
    }

    public void orbState(Swarm orbSwarm)
    {
      // N/A
    }

    public void command(String command, int orb, String property)
    {
      if (command.equals("volume"))
      {
        try
        {
          int vol = Integer.parseInt(property);
          orbControl.volume(orb, vol);
        }
        catch (Exception ex)
        {
        }
      }
    }

    private int randomRange(int low, int high) {
        return (int)(low + (high-low) * Math.random());
    }

}
