package com.orbswarm.swarmcon.orb;

import org.apache.log4j.Logger;

import com.orbswarm.swarmcon.SwarmCon;
import com.orbswarm.swarmcon.io.OrbIo;
import com.orbswarm.swarmcon.path.Path;
import com.orbswarm.swarmcon.path.SmoothPath;
import com.orbswarm.swarmcon.path.Target;
import com.orbswarm.swarmcon.view.HSV;

public class OrbControl implements IOrbControl
{
  @SuppressWarnings("unused")
  private static Logger log = Logger.getLogger(OrbControl.class);

  private SwarmCon swarmCon;
  private OrbIo orbIo;

  public OrbControl(SwarmCon swarmCon, boolean sendCommandsToOrbs,
    boolean simulateColors, boolean simulateSounds)
  {
    this.swarmCon = swarmCon;
    this.orbIo = swarmCon.getOrbIo();
  }

  public SwarmCon getSwarmCon()
  {
    return swarmCon;
  }

  public void setOrbIo(OrbIo orbIo)
  {
    this.orbIo = orbIo;
  }

  //
  // Implementation of methods from com.orbswarm.choreography.OrbControl
  //
  public IOrbControl getOrbControl()
  {
    return (IOrbControl)this;
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

  public void sendLightCommand(int orbNum, String boardAddress, HSV hsvColor,
    int timeTics)
  {
    sendLightingCommand(orbNum, boardAddress, "R" + hsvColor.getRed());
    sendLightingCommand(orbNum, boardAddress, "G" + hsvColor.getGreen());
    sendLightingCommand(orbNum, boardAddress, "B" + hsvColor.getBlue());
    sendLightingCommand(orbNum, boardAddress, "T" + timeTics);
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

  // NOTE: will sleep the thread while following the path.
  // call from within a thread to call this asynchronously.
  public SmoothPath followPath(int orbNum, Path path)
  {
    IOrb orb = swarmCon.getOrb(orbNum);
    SmoothPath travel = orb.getModel().setTargetPath(path);
    long travelTimeMs = (long)(travel.getDuration() * 1000.);
    try
    {
      Thread.sleep((long)travelTimeMs);
    }
    catch (Exception ex)
    {
    }
    return travel;
  }

  public SmoothPath gotoTarget(int orbNum, Target target)
  {
    IOrb orb = swarmCon.getOrb(orbNum);
    SmoothPath poised = orb.getModel().setTargetPosition(target);
    long poiseTimeMs = (long)(poised.getDuration() * 1000.);
    try
    {
      Thread.sleep((long)poiseTimeMs);
    }
    catch (Exception ex)
    {
    }
    return poised;
  }

  public void stopOrb(int orbNum)
  {
    if (orbIo != null)
      orbIo.powerOrb(orbNum, 0);
  }
}
