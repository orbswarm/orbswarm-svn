package com.orbswarm.choreography;

public interface SpecialistFactory {
    public static Specialist createSpecialist(String className,
                                              OrbControl orbControl,
                                              Properties initialProperties) {
        Specialist Specialist = null;
        try {
            Class specClass = Class.forName(className);
            specialist = (Specialist) specClass.instantiate();
            specialist.setup(orbControl, initialProperties);
        } catch (Exception ex) {
            System.out.println("SpecialistFactory got exception creating Specialist " + classname);
            ex.printStackTrace();
        }
        return specialist;
    }
}

            
