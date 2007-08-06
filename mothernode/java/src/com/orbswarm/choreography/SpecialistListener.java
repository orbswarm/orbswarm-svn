package com.orbswarm.choreography;

public interface SpecialistListener {
    public void commandCompleted(Specialist specialist, String action, int orb, String param);
}