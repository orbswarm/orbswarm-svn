package com.orbswarm.swarmcon.util;

import static com.orbswarm.swarmcon.util.Constants.DEFAULT_PROPERTIES_FILE;
import static com.orbswarm.swarmcon.util.Constants.PROPERTIES_FILE_LOCATION;

import java.io.File;
import java.util.Collections;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.trebor.util.JarTools;
import org.trebor.util.properties.IProperty;
import org.trebor.util.properties.PropertySet;
import org.trebor.util.properties.FlagProperty;
import org.trebor.util.properties.LongProperty;
import org.trebor.util.properties.StringProperty;
import org.trebor.util.properties.BooleanProperty;
import org.trebor.util.properties.IntegerProperty;

public class SwarmProperties extends PropertySet
{
  private static Logger log = Logger.getLogger(SwarmProperties.class);

  /*
   * The following are values specified in the properties file. The default
   * values here will be overwritten by the values in the properties file
   * or command line values, if either exist.
   */

  private static IProperty<?>[] mPropertyList =
  {
    // help

    new FlagProperty("help", 'h', false, "should help text be displayed"),

    // orbs

    new BooleanProperty("swarmcon.orb0.enabled", false, "is orb #0 enabled"),
    new BooleanProperty("swarmcon.orb1.enabled", false, "is orb #1 enabled"),
    new BooleanProperty("swarmcon.orb2.enabled", false, "is orb #2 enabled"),
    new BooleanProperty("swarmcon.orb3.enabled", false, "is orb #3 enabled"),
    new BooleanProperty("swarmcon.orb4.enabled", false, "is orb #4 enabled"),
    new BooleanProperty("swarmcon.orb5.enabled", false, "is orb #5 enabled"),

    // motion control

    new IntegerProperty("swarmcon.motion.powerRange", 50,
      "dynamic range of power commands"),
    new IntegerProperty("swarmcon.motion.steeringRange", 100,
      "dynamic range of steering commands"),

    // sound

    new StringProperty("swarmcon.sound.soundCatalogs",
      "resources/songs/sounds.catalog", "location of sounds"),
    new StringProperty("swarmcon.sound.errataCatalog",
      "resources/songs/errata.catalog", "location of sound errata"),

    // live commanding of orbs

    new StringProperty(
      "swarmcon.comm.serialPort",
      "simulation",
      "serial port to send command too, either \"simulation\" or something like one"
        + " of these: /dev/tty.PL2303-0000101D, /dev/tty.PL2303-0000201A,"
        + " /dev/tty.Bluetooth-PDA-Sync"),
    new BooleanProperty("swarmcon.comm.sendCommandsToOrbs", true,
      "should commands be sent to orbs"),
    new BooleanProperty("swarmcon.comm.multipleMotionCommands", false,
      "sould multiple motion commands be sent"),
    new BooleanProperty("swarmcon.comm.multipleSoundCommands", false,
      "sould multiple motion commands be sent"),
    new LongProperty("swarmcon.comm.commandRefreshDelay", 200,
      "delay between commands set to orbs in milliseconds"),
    new LongProperty("swarmcon.comm.positionPollPeriod", 200,
      "the period between position requests in milliseconds"),
  };

  public SwarmProperties(String[] args)
  {
    // define properties
    
    for (IProperty<?> property : mPropertyList)
      add(property);
    
    // establish property values
    
    readProperties(args);
  }
  
  private void readProperties(String[] args)
  {
    try
    {

      // if the properties file does not exist, copy a fresh one out
      // of the jar

      File propFile = new File(PROPERTIES_FILE_LOCATION);
      if (!propFile.exists())
        JarTools.copyResource(DEFAULT_PROPERTIES_FILE, propFile);

      // identify where the properties file lives

      log.debug("Properties file location: " + PROPERTIES_FILE_LOCATION);

      // read in the properties

      applyPropertyFile(propFile);

      // override properties with command line values

      applyArgs(args);

      StringBuffer argsString = new StringBuffer();
      for (String arg : args)
        argsString.append(" " + arg);
      log.debug("args:" + argsString);

      // if help requested print help text

      if (getFlag("help"))
      {
        System.out.println(toHelpString());
        System.exit(0);
      }
      
      // output properties so people know what they got

      logProperties();
    }
    catch (Exception e)
    {
      log
        .error("SwarmCon() caught exception reading properties. Using defaults.");
      e.printStackTrace();
    }
  }
  
  private void logProperties()
  {
    log
      .debug("-------------------- SwarmCon Properties --------------------");
    Vector<String> names = new Vector<String>(getNames());
    Collections.sort(names);
    for (String name : names)
      log.debug(name + " = " + getValueAsString(name));
    log
      .debug("-------------------------------------------------------------");
  }
}

