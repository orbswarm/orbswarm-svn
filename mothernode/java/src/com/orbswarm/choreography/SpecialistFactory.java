package com.orbswarm.choreography;

import java.util.Properties;

public class SpecialistFactory {
    public static Specialist createSpecialist(String className,
                                              OrbControl orbControl,
                                              Properties initialProperties) {
        Specialist specialist = null;
        try {
            Class specClass = Class.forName(className);
            specialist = (Specialist) specClass.newInstance();
            specialist.setup(orbControl, initialProperties);
        } catch (Exception ex) {
            System.out.println("SpecialistFactory got exception creating Specialist " + className);
            ex.printStackTrace();
        }
        return specialist;
    }
}

            
