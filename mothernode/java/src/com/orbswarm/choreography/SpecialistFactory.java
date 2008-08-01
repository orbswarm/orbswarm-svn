package com.orbswarm.choreography;

import java.util.Properties;

import com.orbswarm.swarmcon.IOrbControl;

public class SpecialistFactory
{
    public static Specialist createSpecialist(String className,
    IOrbControl orbControl,
    Properties initialProperties,
    int[] orbs)
    {
      Specialist specialist = null;
      try
      {
        Class specClass = Class.forName(className);
        specialist = (Specialist) specClass.newInstance();
        specialist.setup(orbControl, initialProperties, orbs);
      }
      catch (Exception ex)
      {
        System.out.println("SpecialistFactory got exception creating Specialist " + className);
        ex.printStackTrace();
      }
      return specialist;
    }
}


