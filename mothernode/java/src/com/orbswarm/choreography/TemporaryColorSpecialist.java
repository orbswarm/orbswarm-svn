package com.orbswarm.choreography;

import com.orbswarm.swarmcomposer.color.HSV;

import com.orbswarm.swarmcon.IOrbControl;

import java.util.Properties;
/**
 * A Color Specialist that temporarily switches an orb to a
 * particular color, then switches it back when it's done.
 * This can be very useful for "inside" region events.
 *
 * Properties: Color: <color spec>
 *    -- specify the color the same way as any other Color specialist
 * fadeTime: <seconds>
 *    -- number of seconds to fade TO the target color
 *       default:
 * fadeBackTime: <seconds>
 *    -- number of seconds to fade back to the target color
 *       default: half the fadeTime.
 */
public class TemporaryColorSpecialist extends AbstractSpecialist implements ColorSpecialist
{
    private boolean enabled = true;
    private HSV color = null;
    private int fadeTimeMS;
    private int fadeOutTimeMS = -1;
    private HSV[] rememberTheColors = null;
    private Swarm swarm = null;

    public void setup(IOrbControl orbControl, Properties initialProperties, int[] orbs)
    {
      super.setup(orbControl, initialProperties, orbs);

        System.out.print("[[[[ TemporaryColor.  <setup> orbs: {");
        for(int i=0; i < orbs.length; i++) {
            System.out.print(i + ", ");
        }
        System.out.println("} ]]]]");
    }

    public void setColor(HSV color, float fadeTimeSec)
    {
      this.color = color;
      setDuration(fadeTimeSec);
      this.fadeTimeMS = (int)(fadeTimeSec * 1000);
    }

    public void start()
    {
        /* debug
        System.out.print("[[[[ TemporaryColor.  <start> orbs: {");
        for(int i=0; i < orbs.length; i++) {
            System.out.print(orbs[i] + ", ");
        }
        System.out.println("} ]]]]");
        */
      rememberTheColors = new HSV[orbs.length];
      for (int i=0; i < orbs.length; i++)
      {
        rememberTheColors[i] = orbControl.getOrbColor(orbs[i]);
        //System.out.println("TCS. ENTER. " + this + " rememberTheColors[" + i + ": o" + orbs[i] + "] <== " + rememberTheColors[i].rgbString());
      }
      if (enabled && color != null)
      {
        if (fadeTimeMS < 0)
        {
          fadeTimeMS = 40;
        }
        for (int i=0; i < orbs.length; i++)
        {
          if (orbs[i] >= 0)
          {
            orbControl.orbColor(orbs[i], color, fadeTimeMS);
          }
        }
        broadcastCommandCompleted("start", orbs, null);
      }
    }

    public void stop()
    {
      // hmmm: does the specialist get cloned for each orb start?
      //       because we might not be remembering the correct colors
      //       if more than one are changing at a time...
      if (rememberTheColors != null)
      {
        float fadeOutTimeSec = getFloatProperty("fadeOutTime", -1.f);
        if (fadeOutTimeSec == -1.f)
        {
          fadeOutTimeMS = fadeTimeMS / 2;
        }
        else
        {
          fadeOutTimeMS = (int)(fadeOutTimeSec * 1000);
        }
        for (int i=0; i < orbs.length; i++)
        {
          //System.out.println("TCS.  EXIT" + this + "  rememberTheColors[" + i + ": o" + orbs[i] + "] ==> " + rememberTheColors[i].rgbString());
          orbControl.orbColor(orbs[i], rememberTheColors[i], fadeOutTimeMS);
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
      // doesn't really do anything.
    }

}
